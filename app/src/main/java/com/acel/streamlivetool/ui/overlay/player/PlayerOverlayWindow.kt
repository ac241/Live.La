package com.acel.streamlivetool.ui.overlay.player

import android.content.Context
import android.view.WindowManager
import com.acel.streamlivetool.ui.overlay.AbsOverlayWindow

class PlayerOverlayWindow : AbsOverlayWindow() {
    override val layoutId: Int = com.acel.streamlivetool.R.layout.layout_overlay_player
    override val widthDp: Float = 240F
    override val heightDp: Float = 135F
    override val x: Int = 200
    override val y: Int = 500

    companion object {
        val instance by lazy {
            PlayerOverlayWindow()
        }
    }

    fun changeWindowSize(context: Context, width: Float, height: Float) {
        layoutParams.width = dp2px(width).toInt()
        layoutParams.height = dp2px(height).toInt()
        if (isShown) {
            val windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.updateViewLayout(getLayout(), layoutParams)
        }
    }
}