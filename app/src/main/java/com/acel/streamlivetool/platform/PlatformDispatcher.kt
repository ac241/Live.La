package com.acel.streamlivetool.platform

import androidx.fragment.app.Fragment
import com.acel.streamlivetool.platform.bilibili.BilibiliImpl
import com.acel.streamlivetool.platform.douyu.DouyuImpl
import com.acel.streamlivetool.platform.huomao.HuomaoImpl
import com.acel.streamlivetool.platform.egameqq.EgameqqImpl
import com.acel.streamlivetool.platform.huya.HuyaImpl
import com.acel.streamlivetool.platform.longzhu.LongzhuImpl
import com.acel.streamlivetool.platform.yy.YYImpl

object PlatformDispatcher {
    private val mMap = mutableMapOf<String, IPlatform>()

    init {
        mMap["douyu"] = DouyuImpl.INSTANCE
        mMap["bilibili"] = BilibiliImpl.INSTANCE
        mMap["huya"] = HuyaImpl.INSTANCE
        mMap["huomao"] = HuomaoImpl.INSTANCE
        mMap["yy"] = YYImpl.INSTANCE
        mMap["longzhu"] = LongzhuImpl.INSTANCE
        mMap["egameqq"] = EgameqqImpl.INSTANCE
    }

    fun getPlatformImpl(platform: String): IPlatform? {
        return mMap[platform]
    }

    /**
     * @return List<"platform,platformShowName">
     */
    fun getAllPlatform(fragment: Fragment): List<String> {
        val platformList = mutableListOf<String>()
        mMap.forEach {
            val platformShowName = fragment.resources.getString(it.value.platformShowNameRes)
            val platform = it.value.platform
            platformList.add("$platform,$platformShowName")
        }
        return platformList
    }
}