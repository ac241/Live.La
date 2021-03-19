package com.acel.streamlivetool.ui.overlay.list

import com.acel.streamlivetool.bean.Anchor

class ListOverlayWindowManager {
    companion object {
        private var instance = ListOverlayWindowManager()

        @Synchronized
        fun getInstance(): ListOverlayWindowManager {
            if (instance.isReleased)
                instance = ListOverlayWindowManager()
            return instance
        }
    }

    private var isReleased = false

    private val window = ListOverlayWindow()

    fun show(anchorList: List<Anchor>) {
        window.show(anchorList)
    }

    fun release() {
        isReleased = true
        window.release()
    }
}