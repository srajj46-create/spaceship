package com.spaceshipbot

import android.graphics.Bitmap
import android.graphics.Color

object Detector {

    private val hsv = FloatArray(3)

    private inline fun ratio(bmp: Bitmap, r: FloatArray, test: (Float, Float, Float) -> Boolean): Float {
        val x0 = (r[0] * bmp.width).toInt(); val y0 = (r[1] * bmp.height).toInt()
        val x1 = (r[2] * bmp.width).toInt().coerceAtMost(bmp.width)
        val y1 = (r[3] * bmp.height).toInt().coerceAtMost(bmp.height)
        val w = x1 - x0; val h = y1 - y0
        if (w <= 0 || h <= 0) return 0f
        val px = IntArray(w * h)
        bmp.getPixels(px, 0, w, x0, y0, w, h)
        var n = 0
        for (c in px) { Color.colorToHSV(c, hsv); if (test(hsv[0], hsv[1], hsv[2])) n++ }
        return n.toFloat() / px.size
    }

    fun shipVisible(b: Bitmap) =
        ratio(b, Config.SHIP_RECT) { h, s, v -> h in 15f..45f && s > 0.45f && v > 0.45f } > Config.SHIP_MIN

    private fun isObstacle(h: Float, s: Float, v: Float): Boolean {
        val red = (h < 12f || h > 345f) && s > 0.5f && v > 0.25f
        val ice = h in 185f..215f && s > 0.2f && v > 0.65f
        return red || ice
    }

    /** Returns -1 = left hold, +1 = right hold, 0 = kuch mat karo (lane clear). */
    fun dodgeDirection(b: Bitmap): Int {
        val r = Config.DODGE_RECT
        val x0 = (r[0] * b.width).toInt(); val y0 = (r[1] * b.height).toInt()
        val w = (r[2] * b.width).toInt() - x0; val h = (r[3] * b.height).toInt() - y0
        val px = IntArray(w * h)
        b.getPixels(px, 0, w, x0, y0, w, h)
        var left = 0; var center = 0; var right = 0
        val lEnd = (w * 0.40f).toInt(); val cEnd = (w * 0.60f).toInt()
        for (i in px.indices) {
            Color.colorToHSV(px[i], hsv)
            if (!isObstacle(hsv[0], hsv[1], hsv[2])) continue
            val x = i % w
            if (x < lEnd) left++ else if (x < cEnd) center++ else right++
        }
        if (center < Config.OBSTACLE_MIN_PIXELS) return 0
        return if (left <= right) -1 else 1
    }
}
