package com.spaceshipbot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val REQ = 42

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 96, 48, 48) }
        root.addView(TextView(this).apply {
            textSize = 16f
            text = "1) Accessibility me 'Spaceship Bot' ON karo.\n" +
                   "2) Start Bot dabao, screen capture allow karo.\n" +
                   "3) Game kholo, level shuru karo. Bot sirf obstacles se bachayega.\n" +
                   "   Menu, CLAIM aur ads tum khud sambhalo.\n" +
                   "Band karne ke liye notification ka STOP dabao."
        })
        root.addView(Button(this).apply {
            text = "Accessibility Settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        root.addView(Button(this).apply {
            text = "Start Bot"
            setOnClickListener {
                if (BotAccessibilityService.instance == null) {
                    Toast.makeText(this@MainActivity, "Pehle Accessibility ON karo", Toast.LENGTH_LONG).show()
                } else {
                    val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    startActivityForResult(mpm.createScreenCaptureIntent(), REQ)
                }
            }
        })
        setContentView(root)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (req == REQ && res == RESULT_OK && data != null) {
            startForegroundService(Intent(this, BotService::class.java).putExtra("code", res).putExtra("data", data))
            moveTaskToBack(true)
        }
    }
}
