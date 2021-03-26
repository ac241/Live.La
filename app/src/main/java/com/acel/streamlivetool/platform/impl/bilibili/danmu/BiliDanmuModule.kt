package com.acel.streamlivetool.platform.impl.bilibili.danmu

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.*
import com.acel.streamlivetool.ui.main.player.DanmuManager

/**
 * @param cookieManager -> you can use the default [AbstractPlatformImpl.cookieManager]
 */
class BiliDanmuModule(val cookieManager: CookieManager) : ReusableDanmuModule() {

    override fun generateDanmuClient(danmuManager: DanmuManager, anchor: Anchor) =
        BiliDanmuClient(cookieManager, danmuManager, anchor)

}