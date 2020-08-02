package com.acel.streamlivetool.ui.main.adapter

interface AnchorAdapterWrapper {
    fun getLongClickPosition(): Int
    fun notifyAnchorsChange()
    fun setScrolling(boolean: Boolean)
}