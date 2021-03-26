package com.acel.streamlivetool.platform.impl.huomao.module

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.IAnchor
import com.acel.streamlivetool.platform.impl.huomao.HuomaoImpl
import com.acel.streamlivetool.platform.impl.huomao.bean.RoomInfo
import com.acel.streamlivetool.platform.impl.huomao.module.Pub.getRoomInfo
import com.acel.streamlivetool.util.AnchorUtil
import com.acel.streamlivetool.util.UnicodeUtil

class HuomaoAnchorModule(private val platform: String) : IAnchor {
    override fun getAnchor(queryAnchor: Anchor): Anchor? {
//        return getAnchorFromHtml()
        val roomInfo = getRoomInfo(queryAnchor)
        return if (roomInfo != null) {
            Anchor(
                platform,
                UnicodeUtil.decodeUnicode(roomInfo.nickname),
                roomInfo.roomNumber,
                roomInfo.id
            )
        } else {
            null
        }
    }

    override fun updateAnchorData(queryAnchor: Anchor): Boolean {
        val roomInfo: RoomInfo? = getRoomInfo(queryAnchor)
        return if (roomInfo != null) {
            queryAnchor.apply {
                status = roomInfo.isLive == 1
                title = UnicodeUtil.decodeUnicode(roomInfo.channel)
                avatar = roomInfo.headimg.big
                keyFrame = roomInfo.image
                typeName = roomInfo.gameCname
                online = AnchorUtil.formatOnlineNumber(roomInfo.views)
            }
            true
        } else false
    }

    override fun searchAnchor(keyword: String): List<Anchor>? {
        val result =
            UnicodeUtil.cnToUnicode(keyword)?.let { HuomaoImpl.huomaoService.search(it).execute().body() }
        val list = mutableListOf<Anchor>()
        result?.apply {
            val resultList = result.data.anchor.list
            resultList.forEach {
                list.add(
                    Anchor(
                        platform = platform,
                        nickname = it.nickname.replace(
                            "<i style=\"color: red;font-style: normal\">",
                            ""
                        )
                            .replace("</i>", ""),
                        showId = it.room_number,
                        roomId = it.cid,
                        status = it.is_live == 1,
                        avatar = it.img.big
                    )
                )
            }
        }
        return list
    }

}
