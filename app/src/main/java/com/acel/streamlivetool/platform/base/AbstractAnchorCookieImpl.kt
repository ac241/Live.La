package com.acel.streamlivetool.platform.base

/**
 * @param cookieManager -> you can use the default manager [AbstractPlatformImpl.cookieManager]
 */

abstract class AbstractAnchorCookieImpl(internal val cookieManager: CookieManager) : IAnchorCookie {

}