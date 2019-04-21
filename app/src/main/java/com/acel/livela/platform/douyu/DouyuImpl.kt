package com.acel.livela.platform.douyu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.acel.livela.MyApplication
import com.acel.livela.R
import com.acel.livela.bean.Anchor
import com.acel.livela.bean.AnchorStatus
import com.acel.livela.platform.IPlatform
import com.acel.livela.platform.douyu.bean.RoomInfo
import com.acel.livela.util.TextUtil
import org.mozilla.javascript.Function
import java.util.*


object DouyuImpl : IPlatform {
    override val platform: String = "douyu"
    override val platformShowNameRes: Int = R.string.douyu
    val douyuService = retrofit.create(DouyuApi::class.java)

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
//        return getAnchorFromHtml()
        val msg = douyuService.getRoomInfoMsg(queryAnchor.showId).execute().body()
        //error不为0从html获取
        if (msg?.error != 0) {
            return getAnchorFromHtml(queryAnchor)
        } else {
            val roomInfo: RoomInfo? = douyuService.getRoomInfoFromOpen(queryAnchor.showId).execute().body()
            val roomId = roomInfo?.data?.roomId
            val ownerName = roomInfo?.data?.ownerName
            return Anchor(platform, ownerName, roomId, roomId)
        }
    }

    override fun getStatus(queryAnchor: Anchor): AnchorStatus? {
        val roomInfo: RoomInfo? = douyuService.getRoomInfoFromOpen(queryAnchor.showId).execute().body()
        if (roomInfo?.error == 0) {
            val roomStatus = roomInfo.data.roomStatus
            return AnchorStatus(
                queryAnchor.platform,
                queryAnchor.roomId,
                if (roomStatus == "1") true else false
            )
        } else
            return null
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        val h5Enc = douyuService.getH5Enc(queryAnchor.roomId).execute().body()
        if (h5Enc?.error == 0) {
            val enc = h5Enc.data.get("room" + queryAnchor.roomId).toString()
            val paramsMap = getRequestParams(enc, queryAnchor)
            paramsMap?.let {
                val liveInfo = douyuService.getLiveInfo(queryAnchor.roomId, it).execute().body()
                return liveInfo?.data?.rtmpUrl + "/" + liveInfo?.data?.rtmpLive
            }
        }
        return null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri = Uri.parse("douyutvtest://?type=4&room_id=${anchor.roomId}")
        intent.setData(uri)
        intent.setAction("android.intent.action.VIEW")
        context.startActivity(intent)
    }

    private fun getAnchorFromHtml(queryAnchor: Anchor): Anchor? {
        val html = douyuService.getRoomInfo(queryAnchor.showId).execute().body().toString()
        val nickname = TextUtil.subString(html, "\"nickname\":\"", "\",")
        val showId = TextUtil.subString(html, "\"rid\":", ",\"")
        if (nickname != null && showId != null && !showId.isEmpty())
            return Anchor(platform, nickname, showId, showId)
        else
            return null
    }

    fun getRequestParams(enc: String, anchor: Anchor): MutableMap<String, String>? {
        val context = org.mozilla.javascript.Context.enter()
        val uuid = UUID.randomUUID().toString().replace("-", "")
//        val uuid = "07095540bc131c2cc23726a200021501"
        val time = (Date().time / 1000).toString()
        val inputStream = MyApplication.instance.resources.openRawResource(R.raw.douyu_crypto_js)
        val cryptoJs = inputStream.bufferedReader().use {
            it.readText()
        }
        inputStream.close()
        try {
            val scope = context.initStandardObjects()
            context.setOptimizationLevel(-1);
            context.evaluateString(scope, cryptoJs, null, 1, null)
            context.evaluateString(scope, enc, null, 1, null)
            val result =
                context.evaluateString(
                    scope,
                    "ub98484234(${anchor.roomId},\"${uuid}\",${time})",
                    "douyu",
                    1,
                    null
                )
            val params = org.mozilla.javascript.Context.toString(result)
            val list = params.split("&")
            val map = mutableMapOf<String, String>()
            list.forEach {
                val paramList = it.split("=")
                map.put(paramList[0], paramList[1])
            }
            map.put("ver", "Douyu_219041925")
            map.put("rate", "0")
            map.put("iar", "1")
            map.put("ive", "0")
            map.put("cdn", "")
            return map
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            org.mozilla.javascript.Context.exit()
        }
        return null
    }

}