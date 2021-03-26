package com.acel.streamlivetool.platform.impl.douyu.module

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.StreamingLive
import com.acel.streamlivetool.platform.base.IStreamingLive
import com.acel.streamlivetool.platform.impl.douyu.DouyuImpl
import com.acel.streamlivetool.platform.impl.douyu.bean.LiveInfo
import com.acel.streamlivetool.platform.impl.douyu.bean.LiveInfoTestError
import com.google.gson.Gson
import java.util.*

object DouyuStreamingLiveModule : IStreamingLive {
    override fun getStreamingLive(
        queryAnchor: Anchor,
        queryQuality: StreamingLive.Quality?
    ): StreamingLive? {
        val h5Enc = DouyuImpl.douyuService.getH5Enc(queryAnchor.roomId).execute().body()
        if (h5Enc?.error == 0) {
            val enc = h5Enc.data["room" + queryAnchor.roomId].toString()
            val paramsMap = getRequestParams(enc, queryAnchor, queryQuality?.num ?: 4)
            paramsMap?.let { pm ->
                val jsonStr = DouyuImpl.douyuService.getLiveInfo(queryAnchor.roomId, pm).execute().body()
                val gson = Gson()
                val testError = gson.fromJson(jsonStr, LiveInfoTestError::class.java)
                if (testError.error == 0) {
                    val liveInfo = gson.fromJson(jsonStr, LiveInfo::class.java)
                    liveInfo?.data?.let { data ->
                        val multiRates = data.multirates
                        val returnQualityList = mutableListOf<StreamingLive.Quality>()
                        multiRates.forEach {
                            returnQualityList.add(StreamingLive.Quality(it.name, it.rate))
                        }
                        val rate = liveInfo.data.rate
                        val rateIndex = multiRates.indexOf(LiveInfo.Multirate(0, 0, "", rate))
                        val currentQuality =
                            if (rateIndex != -1) multiRates[rateIndex] else null
                        val returnCurrentQuality =
                            if (currentQuality != null) StreamingLive.Quality(
                                currentQuality.name,
                                currentQuality.rate
                            ) else null

                        val returnUrl: String =
                            liveInfo.data.rtmp_url + "/" + liveInfo.data.rtmp_live

                        return StreamingLive(
                            url = returnUrl,
                            currentQuality = returnCurrentQuality,
                            qualityList = returnQualityList
                        )
                    }
                }
            }
        }
        return null
    }
    private fun getRequestParams(
        enc: String,
        anchor: Anchor,
        rate: Int = 4
    ): MutableMap<String, String>? {
        val context = org.mozilla.javascript.Context.enter()
        val uuid = UUID.randomUUID().toString().replace("-", "")
//        val uuid = "07095540bc131c2cc23726a200021501"
        val time = (Date().time / 1000).toString()
        val inputStream = MyApplication.application.resources.openRawResource(R.raw.douyu_crypto_js)
        val cryptoJs = inputStream.bufferedReader().use {
            it.readText()
        }
        inputStream.close()
        try {
            val scope = context.initStandardObjects()
            context.optimizationLevel = -1
            context.evaluateString(scope, cryptoJs, "cryptoJs", 1, null)
            context.evaluateString(scope, enc, "enc", 1, null)
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
                map[paramList[0]] = paramList[1]
            }
            map["ver"] = "Douyu_219041925"
            map["rate"] = "$rate"
            map["iar"] = "1"
            map["ive"] = "0"
            map["cdn"] = ""
            return map
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            org.mozilla.javascript.Context.exit()
        }
        return null
    }

}