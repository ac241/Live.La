package com.acel.streamlivetool.ui.adapter

interface AnchorAdapterWrapper {
    fun getLongClickPosition(): Int
    fun notifyAnchorsChange()
    fun setScrolling(boolean: Boolean)
}