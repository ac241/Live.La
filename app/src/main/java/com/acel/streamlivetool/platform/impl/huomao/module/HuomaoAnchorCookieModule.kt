package com.acel.streamlivetool.platform.impl.huomao.module

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.AbstractAnchorCookieImpl
import com.acel.streamlivetool.platform.base.CookieManager
import com.acel.streamlivetool.platform.bean.ApiResult
import com.acel.streamlivetool.platform.impl.huomao.HuomaoImpl

class HuomaoAnchorCookieModule(private val platform: String, cookieManager: CookieManager) :
    AbstractAnchorCookieImpl(cookieManager) {
    override fun getAnchorsByCookieMode(): ApiResult<List<Anchor>> {
        val cookie = cookieManager.getCookie()
        if (cookie.isEmpty())
            return ApiResult(false, "cookie empty", cookieValid = false)
        val subscribe = HuomaoImpl.huomaoService.getUsersSubscribe(cookie).execute().body()
        subscribe?.let {
            val list = subscribe.data.usersSubChannels
            val anchorList = mutableListOf<Anchor>()
            list.forEach {
                anchorList.add(
                    Anchor(
                        platform = platform,
                        nickname = it.nickname,
                        showId = it.room_number,
                        roomId = it.id,
                        status = it.is_live == 1,
                        title = it.channel,
                        avatar = it.headimg.big,
                        keyFrame = it.image,
                        typeName = it.gameCname,
                        online = it.views,
                        liveTime = it.event_starttime
                    )
                )
            }
            return ApiResult(true, data = anchorList)
        }
        return ApiResult(false, cookieValid = false)
    }

}