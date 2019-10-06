package com.acel.streamlivetool.platform.huya

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorStatus
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.util.TextUtil

object YYImpl : IPlatform {
    override val platform: String = "yy"
    override val platformShowNameRes: Int = R.string.yy
    val yyService = retrofit.create(YYApi::class.java)

    private fun getHtml(queryAnchor: Anchor): String? {
        val html: String? = yyService.getHtml(queryAnchor.showId).execute().body()
        return html
    }


    override fun getAnchor(queryAnchor: Anchor): Anchor? {
        val searchInfo = yyService.search(queryAnchor.showId).execute().body()
        searchInfo?.let {
            val sidInfo = it.data.searchResult.response.x2.docs[0].sid
            val html = yyService.getHtml(sidInfo).execute().body()
            html?.let {
                val nick = TextUtil.subString(it, "nick: \"", "\",")
                val sid = TextUtil.subString(it, "sid : \"", "\",")
                val ssid = TextUtil.subString(it, "ssid : \"", "\",")
                return Anchor(platform, nick, queryAnchor.showId, "$sid/$ssid")
            }
        }

//        val html: String? = getHtml(queryAnchor)
//
//        html?.let {
//            val showId = TextUtil.subString(it, "\"profileRoom\":\"", "\",")
//            if (showId != null && !showId.isEmpty()) {
//                val nickname =
//                    TextUtil.subString(it, "\"nick\":\"", "\",")
//                        ?.let { it1 -> UnicodeUtil.decodeUnicode(it1) }
//                val uid = TextUtil.subString(it, "\"lp\":", ",")?.replace("\"", "")
//                return Anchor(platform, nickname, showId, uid)
//            }
//        }
        return null
    }

    override fun getStatus(queryAnchor: Anchor): AnchorStatus? {

        val searchInfo = yyService.search(queryAnchor.showId).execute().body()
        searchInfo?.let {
            val anchorMsg = it.data.searchResult.response.x2.docs[0]
//            return Anchor(platform,anchor.name,anchor.asid,anchor.sid)
            return AnchorStatus(
                queryAnchor.platform,
                queryAnchor.roomId,
                if (anchorMsg.liveOn == "1") true else false
            )
        }

//        val html: String? = getHtml(queryAnchor)
//        html?.let {
//            //            val showId = TextUtil.subString(it, "\"profileRoom\":\"", "\",")
//            val state = TextUtil.subString(it, "\"state\":\"", "\",")
//            if (state != null && !state.isEmpty())
//                return AnchorStatus(
//                    queryAnchor.platform,
//                    queryAnchor.roomId,
//                    if (state == "ON") true else false
//                )
//        }

        return null
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        //todo
        return null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri = Uri.parse("yymobile://Channel/Live/${anchor.roomId}")
        intent.setData(uri)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.setAction(Intent.ACTION_VIEW)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}