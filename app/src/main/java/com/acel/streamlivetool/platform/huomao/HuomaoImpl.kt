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


object HuomaoImpl : IPlatform {
    override val platform: String = "huomao"
    override val platformShowNameRes: Int = R.string.huomao
    val huomaoService = retrofit.create(HuomaoApi::class.java)
    val SECRETKEY = "6FE26D855E1AEAE090E243EB1AF73685"

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
//        return getAnchorFromHtml()
        val roomInfo = getRoomInfo(queryAnchor)
        if (roomInfo != null) {
            return Anchor(
                platform,
                UnicodeUtil.decodeUnicode(roomInfo.nickname),
                roomInfo.roomNumber,
                roomInfo.roomNumber
            )
        } else {
            return null
        }
    }

    private fun getRoomInfo(queryAnchor: Anchor): RoomInfo? {
        val html = huomaoService.getRoomInfo(queryAnchor.showId).execute().body()
        val channelOneInfo = html?.let {
            val temp = TextUtil.subString(it, "channelOneInfo = ", "};")
            if (temp == null) null else temp + "}"
        }
        if (channelOneInfo != null)
            return Gson().fromJson<RoomInfo>(channelOneInfo, RoomInfo::class.java)
        else
            return null
    }

    override fun getStatus(queryAnchor: Anchor): AnchorStatus? {
        val roomInfo: RoomInfo? = getRoomInfo(queryAnchor)
        if (roomInfo != null) {
            return AnchorStatus(
                queryAnchor.platform,
                queryAnchor.roomId,
                if (roomInfo.isLive == 1) true else false
            )
        } else
            return null
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        val tag_from = "huomaoh5room"
        val time = (Date().time / 1000).toString()
        val roomInfo = getRoomInfo(queryAnchor)
        if (roomInfo != null) {
            val stream = roomInfo.stream
            val signStr = stream + tag_from + time + SECRETKEY
            val md = MessageDigest.getInstance("MD5")
            //对字符串加密
            md.update(signStr.toByteArray())
            val secretBytes = md.digest()
            val token = BigInteger(1, secretBytes).toString(16)
            val formMap = mutableMapOf<String, String>()
            formMap.put("streamtype", "live")
            formMap.put("VideoIDS", stream)
            formMap.put("time", time)
            formMap.put("cdns", "1")
            formMap.put("from", tag_from)
            formMap.put("token", token)
            val liveData = huomaoService.getLiveData(formMap).execute().body()
            val list = liveData?.streamList?.get(0)?.list
            if (list != null) {
                list.forEach {
                    if (it.type == "BD")
                        return it.url
                }
            }
        }
        return null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri = Uri.parse("sharehuomao://huomao/scheme?cid=${anchor.roomId}&type=1&screenType=0")
        intent.setData(uri)
        intent.setAction("android.intent.action.VIEW")
        context.startActivity(intent)
    }

}