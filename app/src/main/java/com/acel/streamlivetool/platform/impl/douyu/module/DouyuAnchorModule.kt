package com.acel.streamlivetool.platform.impl.douyu.module

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.AnchorModule
import com.acel.streamlivetool.platform.impl.douyu.DouyuImpl.Companion.douyuService
import com.acel.streamlivetool.platform.impl.douyu.bean.RoomInfo
import com.acel.streamlivetool.util.AnchorUtil
import com.acel.streamlivetool.util.TextUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class DouyuAnchorModule(private val platform: String) : AnchorModule {
    override fun getAnchor(queryAnchor: Anchor): Anchor? {
//        return getAnchorFromHtml()
        val msg = douyuService.getRoomInfoMsg(queryAnchor.showId).execute().body()
        //error不为0从html获取
        return if (msg?.error != 0) {
            getAnchorFromHtml(queryAnchor)
        } else {
            val roomInfo: RoomInfo? =
                douyuService.getRoomInfoFromOpen(queryAnchor.showId).execute().body()
            val info = roomInfo?.data
            if (info == null)
                null
            else {
                Anchor(
                    platform = platform,
                    nickname = info.ownerName,
                    showId = info.roomId,
                    roomId = info.roomId,
                    status = info.roomStatus == "1",
                    title = info.roomName,
                    avatar = info.avatar,
                    keyFrame = info.roomThumb,
                    typeName = info.cateName,
                    online = info.online.toString(),
                    liveTime = info.startTime
                )
            }
        }
    }

    override fun updateAnchorData(queryAnchor: Anchor): Boolean {
        return runBlocking {
            val info = async(Dispatchers.IO) {
                val roomInfo =
                    douyuService.getRoomInfoBetard(queryAnchor.showId).execute().body()
                if (roomInfo != null) {
                    queryAnchor.apply {
                        status = roomInfo.room.show_status == 1
                        title = roomInfo.room.room_name
                        avatar = roomInfo.room.avatar.big
                        keyFrame = roomInfo.room.room_pic
                        if (roomInfo.room.videoLoop == 1) secondaryStatus =
                            MyApplication.application.getString(R.string.video_looping)
                        typeName = roomInfo.game.tag_name
                    }
                    true
                } else false
            }
            val online = async(Dispatchers.IO) {
                val roomInfo = douyuService.getRoomInfoFromOpen(queryAnchor.showId).execute().body()
                if (roomInfo?.data?.online != null) {
                    queryAnchor.online = AnchorUtil.formatOnlineNumber(roomInfo.data.online)
                    true
                } else false
            }
            info.await() && online.await()
        }
    }

    override fun searchAnchor(keyword: String): List<Anchor>? {
        val result = douyuService.search(keyword).execute().body()
        val list = mutableListOf<Anchor>()
        result?.apply {
            val resultList = this.data.roomResult
            resultList.forEach {
                list.add(
                    Anchor(
                        platform = platform,
                        nickname = it.nickName,
                        showId = it.rid.toString(),
                        roomId = it.rid.toString(),
                        status = it.isLive == 1,
                        avatar = it.avatar
                    )
                )
            }
        }
        return list
    }

    private fun getAnchorFromHtml(queryAnchor: Anchor): Anchor? {
        val html = douyuService.getRoomInfo(queryAnchor.showId).execute().body().toString()
        val nickname = TextUtil.subString(html, "\"nickname\":\"", "\",")
        val showId = TextUtil.subString(html, "\"rid\":", ",\"")
        return if (nickname != null && showId != null && showId.isNotEmpty())
            Anchor(platform, nickname, showId, showId)
        else
            null
    }
}
