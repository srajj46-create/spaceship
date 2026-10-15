package com.spaceshipbot

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager

class BotService : Service() {

    companion object {
        const val ACTION_STOP = "stop"
        @Volatile var running = false
    }

    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private var display: VirtualDisplay? = null
    @Volatile private var active = false
    private var realW = 0
    private var realH = 0

    override fun onBind(i: Intent?): IBinder? = null

    @Suppress("DEPRECATION")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }
        if (active) return START_NOT_STICKY

        startAsForeground()

        val code = intent?.getIntExtra("code", Activity.RESULT_CANCELED) ?: return stopAndExit()
        val data = intent.getParcelableExtra<Intent>("data") ?: return stopAndExit()
        val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = mpm.getMediaProjection(code, data)
        projection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { stopSelf() }
        }, null)

        val dm = DisplayMetrics()
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(dm)
        realW = dm.widthPixels; realH = dm.heightPixels
        val w = realW / Config.SCALE; val h = realH / Config.SCALE

        reader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2)
        display = projection?.createVirtualDisplay(
            "bot", w, h, dm.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader!!.surface, null, null
        )

        active = true; running = true
        Thread { loop(w, h) }.start()
        return START_NOT_STICKY
    }

    private fun stopAndExit(): Int { stopSelf(); return START_NOT_STICKY }

    private fun startAsForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("bot", "Bot", NotificationManager.IMPORTANCE_LOW))
        val stop = PendingIntent.getService(
            this, 0, Intent(this, BotService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(this, "bot")
            .setContentTitle("Spaceship Bot chal raha hai")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .addAction(Notification.Action.Builder(null, "STOP", stop).build())
            .build()
        if (Build.VERSION.SDK_INT >= 29)
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        else startForeground(1, n)
    }

    private fun grab(w: Int, h: Int): Bitmap? {
        val img = reader?.acquireLatestImage() ?: return null
        try {
            val pl = img.planes[0]
            val rowPad = pl.rowStride - pl.pixelStride * w
            val bmp = Bitmap.createBitmap(w + rowPad / pl.pixelStride, h, Bitmap.Config.ARGB_8888)
            bmp.copyPixelsFromBuffer(pl.buffer)
            return if (rowPad == 0) bmp else Bitmap.createBitmap(bmp, 0, 0, w, h)
        } finally { img.close() }
    }

    private fun loop(w: Int, h: Int) {
        while (active) {
            val bmp = grab(w, h)
            if (bmp == null) { Thread.sleep(20); continue }

            // Sirf tab dodge karo jab gameplay chal raha ho (ship dikh rahi ho)
            if (Detector.shipVisible(bmp)) {
                val dir = Detector.dodgeDirection(bmp)
                if (dir != 0) {
                    val fx = if (dir < 0) Config.HOLD_LEFT_X else Config.HOLD_RIGHT_X
                    BotAccessibilityService.instance?.press(fx * realW, Config.HOLD_Y * realH, Config.HOLD_MS)
                }
            }
            sleepSafe(Config.LOOP_DELAY)
        }
    }

    private fun sleepSafe(ms: Long) { try { Thread.sleep(ms) } catch (_: InterruptedException) {} }

    override fun onDestroy() {
        active = false; running = false
        display?.release(); reader?.close(); projection?.stop()
        super.onDestroy()
    }
}
