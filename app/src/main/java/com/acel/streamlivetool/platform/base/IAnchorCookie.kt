package com.acel.streamlivetool.platform.base

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.bean.ApiResult
import com.acel.streamlivetool.platform.bean.ResultGetAnchorListByCookieMode

interface IAnchorCookie {
    /**
     * cookie方式获取列表
     * @return AnchorsCookieMode
     */
    fun getAnchorsByCookieMode(): ApiResult<List<Anchor>> =
        ApiResult(false, "该平台不支持。", support = false)

    val supportFollow
        get() = false

    /**
     * 关注
     */
    fun follow(anchor: Anchor): ApiResult<String> = ApiResult(false, "该平台不支持。", support = false)

    /**
     * 取消关注
     */
    fun unFollow(anchor: Anchor): ApiResult<String> = ApiResult(false, "该平台不支持。", support = false)


}