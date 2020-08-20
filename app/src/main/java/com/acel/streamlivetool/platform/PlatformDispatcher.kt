package com.acel.streamlivetool.platform

import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.platform.bilibili.BilibiliImpl
import com.acel.streamlivetool.platform.douyu.DouyuImpl
import com.acel.streamlivetool.platform.egameqq.EgameqqImpl
import com.acel.streamlivetool.platform.huomao.HuomaoImpl
import com.acel.streamlivetool.platform.huya.HuyaImpl

object PlatformDispatcher {
    private val mMap = mutableMapOf<String, IPlatform>()

    init {
        mMap["douyu"] = DouyuImpl.INSTANCE
        mMap["bilibili"] = BilibiliImpl.INSTANCE
        mMap["huya"] = HuyaImpl.INSTANCE
        mMap["egameqq"] = EgameqqImpl.INSTANCE
        mMap["huomao"] = HuomaoImpl.INSTANCE
//        mMap["yy"] = YYImpl.INSTANCE
//        mMap["longzhu"] = LongzhuImpl.INSTANCE
    }

    fun getPlatformImpl(platform: String): IPlatform? {
        return mMap[platform]
    }

    /**
     * @return Map<platform name ,Platform Instance>
     */
    fun getAllPlatformInstance(): Map<String, IPlatform> {
        return mMap
    }

    /**
     * @return List<"platform,platformShowName">
     */
    fun getAllPlatform(): List<String> {

        val platformList = mutableListOf<String>()
        mMap.forEach {
            val platformShowName =
                MyApplication.application.resources.getString(it.value.platformShowNameRes)
            val platform = it.value.platform
            platformList.add("$platform,$platformShowName")
        }
        return platformList
    }
}