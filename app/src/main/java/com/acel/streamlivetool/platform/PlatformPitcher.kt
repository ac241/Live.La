package com.acel.streamlivetool.platform

import android.support.v4.app.Fragment
import com.acel.streamlivetool.platform.bilibili.BilibiliImpl
import com.acel.streamlivetool.platform.douyu.DouyuImpl
import com.acel.streamlivetool.platform.huomao.HuomaoImpl
import com.acel.streamlivetool.platform.egameqq.EgameqqImpl
import com.acel.streamlivetool.platform.huya.HuyaImpl
import com.acel.streamlivetool.platform.huya.LongzhuImpl
import com.acel.streamlivetool.platform.huya.YYImpl

object PlatformPitcher {
    private val mMap = mutableMapOf<String, IPlatform>()

    init {
        mMap["douyu"] = DouyuImpl
        mMap["bilibili"] = BilibiliImpl
        mMap["huya"] = HuyaImpl
        mMap["huomao"] = HuomaoImpl
        mMap["yy"] = YYImpl
        mMap["longzhu"] = LongzhuImpl
        mMap["egameqq"] = EgameqqImpl
    }

    fun getPlatformImpl(platform: String): IPlatform? {
        return mMap[platform]
    }

    /**
     * @return List<"{platform},{platformShowName}">
     */
    fun getAllPlatfrom(fragment: Fragment): List<String> {
        val platformList = mutableListOf<String>()
        mMap.forEach {
            val platformShowName = fragment.resources.getString(it.value.platformShowNameRes)
            val platform = it.value.platform
            platformList.add("$platform,$platformShowName")
        }
        return platformList
    }
}