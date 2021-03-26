package com.acel.streamlivetool.platform.impl.egameqq

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.platform.base.*
import com.acel.streamlivetool.platform.impl.egameqq.module.*


class EgameqqImpl : AbstractPlatformImpl() {
    companion object {
        val INSTANCE by lazy {
            EgameqqImpl()
        }
        internal val egameqqService: EgameqqApi =
            RetrofitUtils.retrofit.create(EgameqqApi::class.java)

    }

    override val platform: String = "egameqq"
    override val platformShowNameRes: Int = R.string.egameqq
    override val iconRes: Int = R.drawable.ic_egameqq
    override val platformName: String = MyApplication.application.getString(platformShowNameRes)

    override val anchorModule = EgameqqAnchorModule(platform)
    override val anchorCookieModule = EgameqqAnchorCookieModule(platform, cookieManager)
    override val streamingLiveModule = EgameqqStreamingLiveModule
    override val appModule = EgameqqAppModule
    override val loginModule = EgameqqLoginModule(platform, cookieManager)
    override val danmuModule: BaseDanmuModule? = null
}