package com.acel.streamlivetool.ui.overlay

import android.content.Context
import android.view.WindowManager

class PlayerOverlayWindow : AbsOverlayWindow() {
    private val sizeList =
        listOf(
            Size(240F, 135F),
            Size(320F, 180F),
            Size(400F, 225F)
        )
    private var sizeIndex = 0
    override val layoutId: Int = com.acel.streamlivetool.R.layout.layout_player_overlay
    override val widthDp: Float = sizeList[0].widthDP
    override val heightDp: Float = sizeList[0].heightDP
    override val x: Int = 200
    override val y: Int = 500

    companion object {
        val instance by lazy {
            PlayerOverlayWindow()
        }
    }

    fun changeWindowSize(context: Context) {
        if (++sizeIndex >= sizeList.size)
            sizeIndex = 0
        val size = sizeList[sizeIndex]
        layoutParams.width = dp2px(size.widthDP).toInt()
        layoutParams.height = dp2px(size.heightDP).toInt()
        if (isShown) {
            val windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.updateViewLayout(getLayout(), layoutParams)
        }
    }

    data class Size(val widthDP: Float, val heightDP: Float)
}