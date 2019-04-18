package com.acel.livela.platform.huya

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.acel.livela.R
import com.acel.livela.bean.Anchor
import com.acel.livela.bean.AnchorStatus
import com.acel.livela.platform.IPlatform
import com.acel.livela.platform.huya.bean.Stream
import com.acel.livela.util.TextUtil
import com.acel.livela.util.UnicodeUtil
import com.google.gson.Gson
import java.net.URLEncoder


object HuyaImpl : IPlatform {
    override val platform: String = "huya"
    override val platformShowNameRes: Int = R.string.huya
    val huyaService = retrofit.create(HuyaApi::class.java)

    private fun getHtml(queryAnchor: Anchor): String? {
        val html: String? = huyaService.getHtml(queryAnchor.showId).execute().body()
        return html
    }

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
        val html: String? = getHtml(queryAnchor)

        html?.let {
            val showId = TextUtil.subString(it, "\"profileRoom\":\"", "\",")
            if (showId != null && !showId.isEmpty()) {
                val nickname =
                    TextUtil.subString(it, "\"nick\":\"", "\",")?.let { it1 -> UnicodeUtil.decodeUnicode(it1) }
                val uid = TextUtil.subString(it, "\"uid\":\"", "\",")?.let { it1 -> UnicodeUtil.decodeUnicode(it1) }
                return Anchor(platform, nickname, showId, uid)
            }
        }
        return null
    }

    override fun getStatus(queryAnchor: Anchor): AnchorStatus? {
        val html: String? = getHtml(queryAnchor)
        html?.let {
            //            val showId = TextUtil.subString(it, "\"profileRoom\":\"", "\",")
            val state = TextUtil.subString(it, "\"state\":\"", "\",")
            if (state != null && !state.isEmpty())
                return AnchorStatus(
                    queryAnchor.platform,
                    queryAnchor.roomId,
                    if (state == "ON") true else false
                )
        }
        return null
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        val html = getHtml(queryAnchor)
        html?.let {
            val streamStr = TextUtil.subString(it, "\"stream\":", "};")
            val stream = Gson().fromJson<Stream>(streamStr, Stream::class.java)
            val streamInfo = stream.data[0].gameStreamInfoList[0]
            return streamInfo.sHlsUrl + "/" + streamInfo.sStreamName + "." + streamInfo.sHlsUrlSuffix + "?" + streamInfo.sHlsAntiCode
        }
        return null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri =
            Uri.parse(
                "yykiwi://homepage?banneraction=" + URLEncoder.encode(
                    "https://secstatic.yy.com/huya?hyaction=live&uid=${anchor.roomId}",
                    "utf-8"
                )
            )
        intent.setData(uri)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.setAction(Intent.ACTION_VIEW)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

}