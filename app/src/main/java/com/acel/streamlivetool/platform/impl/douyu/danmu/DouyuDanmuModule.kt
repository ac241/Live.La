package com.acel.streamlivetool.platform.impl.douyu.danmu

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.*
import com.acel.streamlivetool.ui.main.player.DanmuManager

/**
 * @param cookieManager -> you can use the default [AbstractPlatformImpl.cookieManager]
 */
class DouyuDanmuModule(val cookieManager: CookieManager) : ReusableDanmuModule() {

    override fun generateDanmuClient(danmuManager: DanmuManager, anchor: Anchor) =
        DouyuDanmuClient(cookieManager, danmuManager, anchor)

}