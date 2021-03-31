package com.acel.streamlivetool.platform.impl.douyu

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.platform.base.*
import com.acel.streamlivetool.platform.impl.douyu.danmu.DouyuDanmuModule
import com.acel.streamlivetool.platform.impl.douyu.module.*


class DouyuImpl : AbstractPlatformImpl() {
    companion object {
        val INSTANCE by lazy {
            DouyuImpl()
        }
        val douyuService: DouyuApi = RetrofitUtils.retrofit.create(DouyuApi::class.java)

    }

    override val platform: String = "douyu"
    override val platformShowNameRes: Int = R.string.douyu
    override val platformName: String = MyApplication.application.getString(platformShowNameRes)
    override val iconRes: Int = R.drawable.ic_douyu

    override val anchorModule: AnchorModule? = DouyuAnchorModule(platform)
    override val anchorCookieModule = DouyuAnchorCookieModule(platform, cookieManager)
    override val streamingLiveModule = DouyuStreamingLiveModule
    override val appModule = DouyuAppModule
    override val loginModule = DouyuLoginModule(platform, cookieManager)
    override val danmuModule = DouyuDanmuModule(cookieManager)

}