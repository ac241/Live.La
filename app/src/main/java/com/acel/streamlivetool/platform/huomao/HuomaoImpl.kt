package com.acel.streamlivetool.platform.huomao

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorStatus
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.huomao.bean.RoomInfo
import com.acel.streamlivetool.util.TextUtil
import com.acel.streamlivetool.util.UnicodeUtil
import com.google.gson.Gson
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*


class HuomaoImpl : IPlatform {
    companion object {
        val INSTANCE by lazy {
            HuomaoImpl()
        }
        private const val SECRETKEY = "6FE26D855E1AEAE090E243EB1AF73685"
    }

    override val platform: String = "huomao"
    override val platformShowNameRes: Int = R.string.huomao
    private val huomaoService: HuomaoApi = retrofit.create(HuomaoApi::class.java)

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
//        return getAnchorFromHtml()
        val roomInfo = getRoomInfo(queryAnchor)
        return if (roomInfo != null) {
            Anchor(
                platform,
                UnicodeUtil.decodeUnicode(roomInfo.nickname),
                roomInfo.roomNumber,
                roomInfo.roomNumber
            )
        } else {
            null
        }
    }

    private fun getRoomInfo(queryAnchor: Anchor): RoomInfo? {
        val html = huomaoService.getRoomInfo(queryAnchor.showId).execute().body()
        val channelOneInfo = html?.let {
            val temp = TextUtil.subString(it, "channelOneInfo = ", "};")
            if (temp == null) null else "$temp}"
        }
        return if (channelOneInfo != null)
            Gson().fromJson(channelOneInfo, RoomInfo::class.java)
        else
            null
    }

    override fun getStatus(queryAnchor: Anchor): AnchorStatus? {
        val roomInfo: RoomInfo? = getRoomInfo(queryAnchor)
        return if (roomInfo != null) {
            AnchorStatus(
                queryAnchor.platform,
                queryAnchor.roomId,
                roomInfo.isLive == 1, UnicodeUtil.decodeUnicode(roomInfo.channel)
            )
        } else
            null
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        val tagFrom = "huomaoh5room"
        val time = (Date().time / 1000).toString()
        val roomInfo = getRoomInfo(queryAnchor)
        if (roomInfo != null) {
            val stream = roomInfo.stream
            val signStr = stream + tagFrom + time + SECRETKEY
            val md = MessageDigest.getInstance("MD5")
            //对字符串加密
            md.update(signStr.toByteArray())
            val secretBytes = md.digest()
            val token = BigInteger(1, secretBytes).toString(16)
            val formMap = mutableMapOf<String, String>()
            formMap["streamtype"] = "live"
            formMap["VideoIDS"] = stream
            formMap["time"] = time
            formMap["cdns"] = "1"
            formMap["from"] = tagFrom
            formMap["token"] = token
            val liveData = huomaoService.getLiveData(formMap).execute().body()
            liveData?.streamList?.get(0)?.list?.forEach {
                if (it.type == "BD")
                    return it.url
            }
        }
        return null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri = Uri.parse("sharehuomao://huomao/scheme?cid=${anchor.roomId}&type=1&screenType=0")
        intent.data = uri
        intent.action = "android.intent.action.VIEW"
        context.startActivity(intent)
    }

}