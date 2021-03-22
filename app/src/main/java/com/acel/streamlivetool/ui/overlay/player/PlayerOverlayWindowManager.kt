package com.acel.streamlivetool.ui.overlay.player

import com.acel.streamlivetool.bean.Anchor

class PlayerOverlayWindowManager {

    companion object {

        private var instance = PlayerOverlayWindowManager()

        @Synchronized
        fun getInstance(): PlayerOverlayWindowManager {
            if (instance.isReleased)
                instance = PlayerOverlayWindowManager()
            return instance
        }

        const val BROADCAST_ACTION = "com.acel.action.PLAYER_OVERLAY"
        const val SHOWN_KEY = "is_shown"
    }

    private var isReleased = false

    private val playerWindow = PlayerOverlayWindow()

    fun show(anchor: Anchor, list: List<Anchor>?) {
        playerWindow.play(anchor, list)
    }

    fun release() {
        isReleased = true
        playerWindow.release()
    }
}