package com.acel.streamlivetool.platform.impl.huya.module

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.IAnchor
import com.acel.streamlivetool.platform.impl.huya.HuyaImpl
import com.acel.streamlivetool.platform.impl.huya.module.HuyaPatternUtil.getMatchString
import com.acel.streamlivetool.platform.impl.huya.module.Pub.getMHtml
import com.acel.streamlivetool.util.TextUtil
import com.acel.streamlivetool.util.UnicodeUtil

class HuyaAnchorModule(private val platform: String) : IAnchor {
    override fun getAnchor(queryAnchor: Anchor): Anchor? {
        val html: String? = getMHtml(queryAnchor)
        html?.let {
            val showId = html.getMatchString(HuyaPatternUtil.showId)
            if (showId != null && showId.isNotEmpty()) {
                val nickname = html.getMatchString(HuyaPatternUtil.nickName)
                    ?.let { it1 -> UnicodeUtil.decodeUnicode(it1) }
                var avatarUrl = TextUtil.subString(html, "<span class=\"pic-clip\">", "alt=\"")
                    ?.getMatchString(HuyaPatternUtil.avatar)
                avatarUrl?.let {
                    if (!it.contains("https:"))
                        avatarUrl = "https:$avatarUrl"
                }
                return Anchor(
                    platform = platform,
                    nickname = nickname.toString(),
                    showId = showId,
                    roomId = html.getMatchString(HuyaPatternUtil.uid).toString(),
                    status = html.getMatchString(HuyaPatternUtil.status) == "true",
                    title = html.getMatchString(HuyaPatternUtil.title),
                    avatar = avatarUrl,
                    keyFrame = html.getMatchString(HuyaPatternUtil.keyFrame),
                    typeName = html.getMatchString(HuyaPatternUtil.typeName),
                    online = html.getMatchString(HuyaPatternUtil.online)
                )
            }
        }
        return null
    }

    override fun updateAnchorData(queryAnchor: Anchor): Boolean {
        val anchor = getAnchor(queryAnchor)
        return if (anchor != null) {
            queryAnchor.apply {
                status = anchor.status
                title = anchor.title
                avatar = anchor.avatar
                typeName = anchor.typeName
                keyFrame = anchor.keyFrame
                online = anchor.online
            }
            true
        } else
            false
    }

    override fun searchAnchor(keyword: String): List<Anchor>? {
        val result = HuyaImpl.huyaService.search(keyword).execute().body()
        val list = mutableListOf<Anchor>()
        result?.apply {
            val resultList = result.response.`1`.docs
            resultList.forEach {
                list.add(
                    Anchor(
                        platform = platform,
                        nickname = it.game_nick,
                        showId = it.room_id.toString(),
                        roomId = it.uid.toString(),
                        status = it.gameLiveOn,
                        avatar = it.game_avatarUrl52
                    )
                )
            }
        }
        return list
    }
}
