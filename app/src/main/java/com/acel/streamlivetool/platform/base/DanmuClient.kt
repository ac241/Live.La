package com.acel.streamlivetool.platform.base

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.main.player.DanmuManager

interface DanmuClient {
    fun start(anchor: Anchor, danmuManager: DanmuManager)
    fun stop()
    fun release()
}