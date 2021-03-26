package com.acel.streamlivetool.platform.impl.egameqq.module

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.AbstractAnchorCookieImpl
import com.acel.streamlivetool.platform.base.CookieManager
import com.acel.streamlivetool.platform.bean.ApiResult
import com.acel.streamlivetool.platform.impl.egameqq.EgameqqImpl
import com.acel.streamlivetool.util.AnchorUtil
import com.acel.streamlivetool.util.TimeUtil

class EgameqqAnchorCookieModule(private val platform: String, cookieManager: CookieManager) :
    AbstractAnchorCookieImpl(cookieManager) {
    override fun getAnchorsByCookieMode(): ApiResult<List<Anchor>> {
        val cookie = cookieManager.getCookie()
        if (cookie.isEmpty())
            return ApiResult(false, "cookie empty", cookieValid = false)
        val list = EgameqqImpl.egameqqService.getFollowList(cookie).execute().body()
        if (list?.ecode != 0)
            return ApiResult(false, "ecode 0", cookieValid = false)
        if (list.uid == 0)
            return ApiResult(false, "cookie invalid", cookieValid = false)
        if (list.data.key.retCode != 0)
            return ApiResult(false, list.data.key.retMsg, cookieValid = false)
        else {
            val anchorList = mutableListOf<Anchor>()
            with(list.data.key.retBody.data.online_follow_list) {
                this.forEach {
                    anchorList.add(
                        Anchor(
                            platform = platform,
                            nickname = it.live_info.anchor_name,
                            showId = it.live_info.anchor_id.toString(),
                            roomId = it.live_info.anchor_id.toString(),
                            status = it.status == 1,
                            title = it.live_info.title,
                            avatar = it.live_info.anchor_face_url,
                            keyFrame = it.live_info.video_info.url,
                            typeName = it.live_info.appname,
                            online = AnchorUtil.formatOnlineNumber(it.live_info.online),
                            liveTime = TimeUtil.timestampToString(it.last_play_time)
                        )
                    )
                }
            }
            return ApiResult(true, data = anchorList)
        }
    }

}