package com.acel.streamlivetool.ui.overlay

class ListOverlayWindow : AbsOverlayWindow() {
    override val layoutId: Int = com.acel.streamlivetool.R.layout.layout_list_overlay
    override val widthDp: Float = 220F
    override val heightDp: Float = 320F
    override val x: Int = 300
    override val y: Int = 800

    companion object {
        val instance by lazy { ListOverlayWindow() }
    }
}