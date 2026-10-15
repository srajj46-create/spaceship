package com.spaceshipbot

/**
 * Saare coordinates screen ke FRACTION me hain (0.0 - 1.0), taaki alag phone par bhi chale.
 * Reference screen: 1080 x 2408.  Tune yahin se karo.
 */
object Config {
    // Hold positions
    const val HOLD_LEFT_X = 0.15f;  const val HOLD_RIGHT_X = 0.85f
    const val HOLD_Y = 0.88f

    // Detection regions (x0, y0, x1, y1)
    val SHIP_RECT = floatArrayOf(0.30f, 0.70f, 0.70f, 0.82f)   // orange ship
    val DODGE_RECT = floatArrayOf(0.00f, 0.41f, 1.00f, 0.70f)  // obstacles dekhne ka area

    // Thresholds (fraction of matching pixels)
    const val SHIP_MIN = 0.015f
    const val OBSTACLE_MIN_PIXELS = 25   // scaled frame par

    // Timing (ms)
    const val LOOP_DELAY = 40L
    const val HOLD_MS = 120L
    const val SCALE = 4                  // frame ko 1/4 size par process karte hain
}
