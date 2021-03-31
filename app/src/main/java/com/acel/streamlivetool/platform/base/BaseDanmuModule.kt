package com.acel.streamlivetool.platform.base

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.main.player.DanmuManager

abstract class BaseDanmuModule : DanmuModule {
    /**
     * you can use[BaseDanmuClient]
     */
    abstract override fun getDanmuClient(
        danmuManager: DanmuManager, anchor: Anchor
    ): DanmuClient


}