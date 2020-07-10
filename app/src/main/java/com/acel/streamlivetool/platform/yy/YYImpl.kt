package com.acel.streamlivetool.platform.yy

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.util.TextUtil

class YYImpl : IPlatform {
    companion object{
        val INSTANCE by lazy {
            YYImpl()
        }
    }
    override val platform: String = "yy"
    override val platformShowNameRes: Int = R.string.yy
    override val supportCookieMode: Boolean = false
    private val yyService: YYApi = retrofit.create(YYApi::class.java)

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
        val searchInfo = yyService.search(queryAnchor.showId).execute().body()
        searchInfo?.let {
            val sidInfo = it.data.searchResult.response.x2.docs[0].sid
            val html = yyService.getHtml(sidInfo).execute().body()
            html?.let { htmlString ->
                val nick = TextUtil.subString(htmlString, "nick: \"", "\",")
                val sid = TextUtil.subString(htmlString, "sid : \"", "\",")
                val ssid = TextUtil.subString(htmlString, "ssid : \"", "\",")
                return Anchor(platform, nick, queryAnchor.showId, "$sid/$ssid")
            }
        }
        return null
    }

    override fun getAnchorAttribute(queryAnchor: Anchor): AnchorAttribute? {
        val searchInfo = yyService.search(queryAnchor.showId).execute().body()
        searchInfo?.let {
            val anchorMsg = it.data.searchResult.response.x2.docs[0]
            return AnchorAttribute(
                queryAnchor.platform,
                queryAnchor.roomId,
                anchorMsg.liveOn == "1",""
            )
        }
        return null
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        //todo
        return null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri = Uri.parse("yymobile://Channel/Live/${anchor.roomId}")
        intent.data = uri
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.action = Intent.ACTION_VIEW
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}