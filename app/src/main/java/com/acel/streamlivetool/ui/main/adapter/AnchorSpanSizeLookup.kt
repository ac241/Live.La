package com.acel.streamlivetool.ui.main.adapter

import androidx.recyclerview.widget.GridLayoutManager

class AnchorSpanSizeLookup(val adapter: AnchorAdapter, private val manager: GridLayoutManager) :
    GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int): Int {
        return if (adapter.isFullSpan(position)) manager.spanCount else 1
    }
}