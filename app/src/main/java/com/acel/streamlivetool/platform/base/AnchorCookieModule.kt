package com.acel.streamlivetool.platform.base

/**
 * @param cookieManager -> you can use the default manager [AbstractPlatformImpl.cookieManager]
 */

abstract class AnchorCookieModule(internal val cookieManager: CookieManager) : AnchorCookieModuleInterface {

}