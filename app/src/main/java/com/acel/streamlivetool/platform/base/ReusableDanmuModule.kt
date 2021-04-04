package com.acel.streamlivetool.platform.base

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.main.player.DanmuManager

/**
 * danmu client可复用的DanmuModule
 */
abstract class ReusableDanmuModule :
    BaseDanmuModule() {

    private val reusePool = mutableListOf<ReusableDanmuClient>()

    @Synchronized
    final override fun getDanmuClient(danmuManager: DanmuManager, anchor: Anchor): DanmuClient {
        reusePool.forEach {
            if (!it.using && !it.isReleased)
                return it.apply { resetAnchor(anchor) }
        }
        val new = generateDanmuClient(danmuManager, anchor)
        reusePool.add(new)
        return new
    }

    abstract fun generateDanmuClient(
        danmuManager: DanmuManager,
        anchor: Anchor
    ): ReusableDanmuClient

}