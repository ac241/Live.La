package com.acel.streamlivetool.platform.impl.huya

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.platform.base.BaseDanmuModule
import com.acel.streamlivetool.platform.base.AbstractPlatformImpl
import com.acel.streamlivetool.platform.impl.huya.danmu.HuyaDanmuModule
import com.acel.streamlivetool.platform.impl.huya.module.*


class HuyaImpl : AbstractPlatformImpl() {
    companion object {
        val INSTANCE by lazy {
            HuyaImpl()
        }
        internal val huyaService: HuyaApi = RetrofitUtils.retrofit.create(HuyaApi::class.java)

    }

    override val platform: String = "huya"
    override val platformShowNameRes: Int = R.string.huya
    override val platformName: String = MyApplication.application.getString(platformShowNameRes)
    override val iconRes: Int = R.drawable.ic_huya

    override val anchorModule = HuyaAnchorModule(platform)
    override val anchorCookieModule = HuyaAnchorCookieModule(platform, cookieManager)
    override val streamingLiveModule = HuyaStreamingLiveModule
    override val appModule = HuyaAppModule
    override val loginModule = HuyaLoginModule(platform, cookieManager)
    override val danmuModule: BaseDanmuModule? = HuyaDanmuModule(cookieManager)

}