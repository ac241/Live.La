package com.acel.streamlivetool.platform.base

import androidx.annotation.CallSuper
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.main.player.DanmuManager

abstract class ReusableDanmuClient(
    cookieManager: CookieManager,
    danmuManager: DanmuManager,
    anchor: Anchor,
) : BaseDanmuClient(cookieManager, danmuManager, anchor) {
    var using = false

    @CallSuper
    override fun start(anchor: Anchor, danmuManager: DanmuManager) {
        super.start(anchor, danmuManager)
        using = true
    }

    @CallSuper
    override fun stop() {
        super.stop()
        using = false
    }
}