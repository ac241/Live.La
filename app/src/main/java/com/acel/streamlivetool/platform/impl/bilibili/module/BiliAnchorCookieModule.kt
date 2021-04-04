package com.acel.streamlivetool.platform.impl.bilibili.module

import android.content.Context
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.AnchorCookieModule
import com.acel.streamlivetool.platform.base.CookieManager
import com.acel.streamlivetool.platform.bean.ApiResult
import com.acel.streamlivetool.platform.impl.bilibili.BilibiliImpl
import com.acel.streamlivetool.util.AnchorUtil
import com.acel.streamlivetool.util.CookieUtil
import com.acel.streamlivetool.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class BiliAnchorCookieModule(private val platform: String, cookieManager: CookieManager) :
        AnchorCookieModule(cookieManager) {
    override fun getAnchorsByCookieMode(): ApiResult<List<Anchor>> {
        cookieManager.getCookie().let { cookie ->
            if (cookie.isEmpty())
                return ApiResult(false, "非法的cookie", cookieValid = false)
            var cookieOk = true
            var message = ""
            val anchorList = mutableListOf<Anchor>()
            runBlocking {
                val liveList = async(Dispatchers.IO) {
                    val result = BilibiliImpl.bilibiliService.liveAnchor(cookie).execute().body()
                    result?.let {
                        val list = mutableListOf<Anchor>()
                        if (result.code != 0) {
                            cookieOk = false
                            message = result.message
                        }
                        if (result.data.total_count == 0)
                            return@async list
                        val rooms = result.data.rooms
                        rooms.forEach {
                            list.add(
                                    Anchor(
                                            platform = platform,
                                            nickname = it.uname,
                                            showId = it.roomid.toString(),
                                            roomId = it.roomid.toString(),
                                            status = true,
                                            title = it.title,
                                            avatar = it.face,
                                            keyFrame = it.cover,
                                            typeName = it.area_v2_name,
                                            online = AnchorUtil.formatOnlineNumber(it.online),
                                            liveTime = TimeUtil.timestampToString(it.live_time)
                                    )
                            )
                        }
                        list
                    }
                }
                val unLiveList = async(Dispatchers.IO) {
                    val result = BilibiliImpl.bilibiliService.unLiveAnchor(cookie).execute().body()
                    val list = mutableListOf<Anchor>()
                    if (result?.data?.total_count == 0)
                        return@async list
                    val rooms = result?.data?.rooms
                    rooms?.forEach {
                        list.add(
                                Anchor(

                                        platform = platform,
                                        nickname = it.uname,
                                        showId = it.roomid.toString(),
                                        roomId = it.roomid.toString(),
                                        status = false,
                                        title = "${it.live_desc} 直播了 ${it.area_v2_name}",
                                        avatar = it.face,
                                        typeName = it.area_v2_name,
                                        liveTime = it.live_desc
                                )
                        )
                    }
                    list
                }
                anchorList.addAll(liveList.await() as Collection<Anchor>)
                anchorList.addAll(unLiveList.await() as Collection<Anchor>)
            }
            return ApiResult(true, message, anchorList, cookieValid = cookieOk)
        }
    }

    override val supportFollow: Boolean = true
    override fun follow(context: Context, anchor: Anchor): ApiResult<String> {
        cookieManager.getCookie().let { cookie ->
            if (cookie.isEmpty())
                return ApiResult(success = false, msg = "未登录", cookieValid = false)

            val roomInfo = BilibiliImpl.bilibiliService.getRoomInfo(anchor.roomId).execute().body()
            roomInfo?.let { info ->
                info.data?.uid?.let { uid ->
                    val jct = CookieUtil.getCookieField(cookie, "bili_jct")
                    jct?.let { j ->
                        val response =
                                BilibiliImpl.bilibiliService.follow(cookie, uid, j).execute().body()
                        response?.apply {
                            return if (code == 0)
                                ApiResult(success = true, msg = "关注成功")
                            else
                                ApiResult(success = false, msg = message)
                        }
                    }
                }

            }
        }
        return ApiResult(success = false, msg = "发生错误")
    }
}