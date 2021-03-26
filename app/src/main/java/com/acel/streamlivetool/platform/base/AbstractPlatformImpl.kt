package com.acel.streamlivetool.platform.base

abstract class AbstractPlatformImpl : IMessage {
    open val cookieManager by lazy { DefaultCookieManager(platform) }
    abstract val anchorModule: IAnchor?
    abstract val anchorCookieModule: AbstractAnchorCookieImpl?
    abstract val streamingLiveModule: IStreamingLive?
    abstract val appModule: IApp?
    abstract val loginModule: AbstractLoginImpl?
    abstract val danmuModule: BaseDanmuModule?
}