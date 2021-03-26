package com.acel.streamlivetool.platform.impl.bilibili.module

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.IAnchor
import com.acel.streamlivetool.platform.impl.bilibili.BilibiliImpl
import com.acel.streamlivetool.util.AnchorUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class BiliAnchorModule(private val platform: String) : IAnchor {
    override fun getAnchor(queryAnchor: Anchor): Anchor? {
        val anchor = Anchor()
        var success = true
        runBlocking {
            val result = async(Dispatchers.IO) {
                BilibiliImpl.bilibiliService.getRoomInfo(queryAnchor.showId).execute().body()
            }
            val h5Info = async(Dispatchers.IO) {
                BilibiliImpl.bilibiliService.getH5InfoByRoom(queryAnchor.showId.toLong()).execute()
                    .body()
            }
            result.await().apply {
                if (this != null) {
                    val info = data
                    if (code == 0 && info != null) {
                        val roomId = info.room_id
                        val ownerName = getAnchorName(roomId.toLong())
                        anchor.apply {
                            platform = this@BiliAnchorModule.platform
                            nickname = ownerName.toString()
                            showId = roomId.toString()
                            this.roomId = roomId.toString()
                            status = info.live_status == 1
                            title = info.title
                            keyFrame = info.keyframe
                            typeName = info.area_name
                            online = info.online.toString()
                            liveTime = info.live_time
                        }
                    } else {
                        success = false
                    }
                } else
                    success = false
            }
            h5Info.await().apply {
                if (this != null) {
                    val info = data
                    if (code == 0) {
                        anchor.avatar = info.anchor_info.base_info.face
                    } else
                        success = false
                } else
                    success = false
            }
        }
        return if (success) anchor else null
    }

    override fun updateAnchorData(queryAnchor: Anchor): Boolean {
        var success = true
        runBlocking {
            val result = async(Dispatchers.IO) {
                BilibiliImpl.bilibiliService.getRoomInfo(queryAnchor.showId).execute().body()
            }
            val h5Info = async(Dispatchers.IO) {
                BilibiliImpl.bilibiliService.getH5InfoByRoom(queryAnchor.showId.toLong()).execute()
                    .body()
            }
            result.await().apply {
                if (this != null) {
                    val info = data
                    if (code == 0 && info != null) {
                        queryAnchor.apply {
                            keyFrame = info.keyframe
                        }
                    } else {
                        success = false
                    }
                } else
                    success = false
            }
            h5Info.await().apply {
                if (this != null) {
                    val info = data
                    if (code == 0) {
                        queryAnchor.avatar = info.anchor_info.base_info.face
                        queryAnchor.apply {
                            status = data.room_info.live_status == 1
                            title = data.room_info.title
                            avatar = data.anchor_info.base_info.face
                            keyFrame = data.room_info.cover
                            typeName = data.room_info.area_name
                            online = AnchorUtil.formatOnlineNumber(data.room_info.online)
                        }
                    } else
                        success = false
                } else
                    success = false
            }
        }
        return success
    }

    override fun searchAnchor(keyword: String): List<Anchor>? {
        val result = BilibiliImpl.bilibiliService.search(keyword).execute().body()
        val list = mutableListOf<Anchor>()
        result?.apply {
            val resultList = result.data.result
            resultList.forEach {
                list.add(
                    Anchor(
                        platform = this@BiliAnchorModule.platform,
                        nickname = it.uname.replace("<em class=\"keyword\">", "")
                            .replace("</em>", ""),
                        showId = it.roomid.toString(),
                        roomId = it.roomid.toString(),
                        status = it.is_live,
                        avatar = "http:${it.uface}"
                    )
                )
            }
        }
        return list
    }

    private fun getAnchorName(roomId: Long): String? {
        val h5Info =
            BilibiliImpl.bilibiliService.getH5InfoByRoom(roomId).execute().body()
        return h5Info?.data?.anchor_info?.base_info?.uname
    }
}
