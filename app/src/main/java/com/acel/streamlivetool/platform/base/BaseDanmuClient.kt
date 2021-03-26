package com.acel.streamlivetool.platform.base

import androidx.annotation.CallSuper
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.main.player.DanmuManager

abstract class BaseDanmuClient(
    var cookieManager: CookieManager?, var danmuManager: DanmuManager?, var anchor: Anchor?
) : DanmuClient {
    var isRunning = false
    var isReleased = false

    fun resetAnchor(anchor: Anchor) {
        assertRelease()
        this.anchor = anchor
    }

    private fun assertRelease() {
        if (isReleased)
            throw IllegalStateException("this DanmuClient is released")
    }

    @CallSuper
    override fun start(anchor: Anchor, danmuManager: DanmuManager) {
        isRunning = true
    }

    @CallSuper
    override fun stop() {
        isRunning = false
    }

    @CallSuper
    override fun release() {
        cookieManager = null
        danmuManager = null
        anchor = null
        isReleased = true
        isRunning = false
    }
}