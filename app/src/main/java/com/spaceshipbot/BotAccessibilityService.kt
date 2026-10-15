package com.spaceshipbot

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class BotAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile var instance: BotAccessibilityService? = null
    }

    override fun onServiceConnected() { instance = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { instance = null; super.onDestroy() }

    /** Press-and-hold at (x, y) for ms milliseconds. ms ~ 1 = tap. */
    fun press(x: Float, y: Float, ms: Long) {
        val p = Path().apply { moveTo(x, y) }
        val g = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(p, 0, ms.coerceAtLeast(1)))
            .build()
        dispatchGesture(g, null, null)
    }
}
