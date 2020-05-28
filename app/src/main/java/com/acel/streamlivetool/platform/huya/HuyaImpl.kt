package com.acel.streamlivetool.platform.huya

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorStatus
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.huya.bean.Stream
import com.acel.streamlivetool.util.TextUtil
import com.acel.streamlivetool.util.UnicodeUtil
import com.google.gson.Gson
import java.net.URLEncoder


class HuyaImpl : IPlatform {
    companion object{
        val INSTANCE by lazy {
            HuyaImpl()
        }
    }
    override val platform: String = "huya"
    override val platformShowNameRes: Int = R.string.huya
    private val huyaService: HuyaApi = retrofit.create(HuyaApi::class.java)

    private fun getHtml(queryAnchor: Anchor): String? {
        return huyaService.getHtml(queryAnchor.showId).execute().body()
    }

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
        val html: String? = getHtml(queryAnchor)

        html?.let {
            val showId = TextUtil.subString(it, "\"profileRoom\":\"", "\",")
            if (showId != null && showId.isNotEmpty()) {
                val nickname =
                    TextUtil.subString(it, "\"nick\":\"", "\",")?.let { it1 -> UnicodeUtil.decodeUnicode(it1) }
                val uid = TextUtil.subString(it, "\"lp\":", ",")?.replace("\"", "")
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
            if (state != null && state.isNotEmpty())
                return AnchorStatus(
                    queryAnchor.platform,
                    queryAnchor.roomId,
                    state == "ON"
                )
        }
        return null
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        val html = getHtml(queryAnchor)
        html?.let {
            val streamStr = TextUtil.subString(it, "\"stream\":", "};")
            val stream = Gson().fromJson(streamStr, Stream::class.java)
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
        intent.data = uri
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.action = Intent.ACTION_VIEW
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}