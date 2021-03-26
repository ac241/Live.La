package com.acel.streamlivetool.platform.impl.huomao

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.platform.base.BaseDanmuModule
import com.acel.streamlivetool.platform.base.AbstractPlatformImpl
import com.acel.streamlivetool.platform.impl.huomao.module.*


class HuomaoImpl : AbstractPlatformImpl() {
    companion object {
        val INSTANCE by lazy {
            HuomaoImpl()
        }
        internal val huomaoService: HuomaoApi = RetrofitUtils.retrofit.create(HuomaoApi::class.java)
    }


    override val platform: String = "huomao"
    override val platformShowNameRes: Int = R.string.huomao
    override val platformName: String = MyApplication.application.getString(platformShowNameRes)
    override val iconRes: Int = R.drawable.ic_huomao

    override val anchorModule = HuomaoAnchorModule(platform)
    override val anchorCookieModule = HuomaoAnchorCookieModule(platform, cookieManager)
    override val streamingLiveModule = HuomaoStreamingLiveModule
    override val appModule = HuomaoAppModule
    override val loginModule = HuomaoLoginModule(platform, cookieManager)
    override val danmuModule: BaseDanmuModule? = null
}