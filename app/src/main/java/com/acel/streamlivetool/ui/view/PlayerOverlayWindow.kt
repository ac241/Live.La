package com.acel.streamlivetool.ui.view

class PlayerOverlayWindow : AbsOverlayWindow() {
    override val layoutId: Int = com.acel.streamlivetool.R.layout.layout_player_overlay
    override val widthDp: Float = 240F
    override val heightDp: Float = 135F
    override val x: Int = 100
    override val y: Int = 100

    companion object {
        val instance by lazy {
            PlayerOverlayWindow()
        }
    }
}