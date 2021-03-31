package com.acel.streamlivetool.platform.base

abstract class AbstractPlatformImpl : IMessage {
    open val cookieManager by lazy { DefaultCookieManager(platform) }
    abstract val anchorModule: AnchorModule?
    abstract val anchorCookieModule: AbstractAnchorCookieImpl?
    abstract val streamingLiveModule: StreamingLiveModule?
    abstract val appModule: AppModule?
    abstract val loginModule: AbstractLoginImpl?
    abstract val danmuModule: BaseDanmuModule?
}