package com.acel.streamlivetool.platform.impl.huomao.module

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.StreamingLive
import com.acel.streamlivetool.platform.base.StreamingLiveModule
import com.acel.streamlivetool.platform.impl.huomao.HuomaoImpl
import com.acel.streamlivetool.platform.impl.huomao.module.Pub.getRoomInfo
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

object HuomaoStreamingLiveModule : StreamingLiveModule {
    private const val SECRETKEY = "6FE26D855E1AEAE090E243EB1AF73685"

    override fun getStreamingLive(
        queryAnchor: Anchor,
        queryQuality: StreamingLive.Quality?
    ): StreamingLive? {
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
            val liveData = HuomaoImpl.huomaoService.getLiveData(formMap).execute().body()
            liveData?.streamList?.get(0)?.listHls?.get(0)?.let {
                return StreamingLive(url = it.url, null, null)
            }
        }
        return null
    }

}