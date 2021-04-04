package com.acel.streamlivetool.platform.impl.bilibili

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.platform.base.BaseDanmuModule
import com.acel.streamlivetool.platform.base.LoginModule
import com.acel.streamlivetool.platform.base.AbstractPlatformImpl
import com.acel.streamlivetool.platform.base.AppModule
import com.acel.streamlivetool.platform.impl.bilibili.module.*
import com.acel.streamlivetool.platform.impl.bilibili.danmu.BiliDanmuModule

class BilibiliImpl : AbstractPlatformImpl() {
    companion object {
        val INSTANCE by lazy { BilibiliImpl() }
        internal val bilibiliService: BilibiliApi =
            RetrofitUtils.retrofit.create(BilibiliApi::class.java)
    }

    override val platform: String = "bilibili"
    override val platformShowNameRes: Int = R.string.bilibili
    override val platformName: String = MyApplication.application.getString(platformShowNameRes)
    override val iconRes: Int = R.drawable.ic_bilibili

    override val anchorModule = BiliAnchorModule(platform)
    override val anchorCookieModule = BiliAnchorCookieModule(platform, cookieManager)
    override val streamingLiveModule = BiliStreamingLiveModule
    override val appModule: AppModule = BiliAppModule
    override val loginModule: LoginModule = BiliLoginModule(platform, cookieManager)
    override val danmuModule: BaseDanmuModule = BiliDanmuModule(cookieManager)

}