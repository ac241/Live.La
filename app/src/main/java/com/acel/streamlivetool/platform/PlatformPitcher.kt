package com.acel.streamlivetool.platform

import android.support.v4.app.Fragment
import com.acel.streamlivetool.platform.bilibili.BilibiliImpl
import com.acel.streamlivetool.platform.douyu.DouyuImpl
import com.acel.streamlivetool.platform.huomao.HuomaoImpl
import com.acel.streamlivetool.platform.huya.EgameqqImpl
import com.acel.streamlivetool.platform.huya.HuyaImpl
import com.acel.streamlivetool.platform.huya.LongzhuImpl
import com.acel.streamlivetool.platform.huya.YYImpl

object PlatformPitcher {
    val mMap = mutableMapOf<String, IPlatform>()

    init {
        mMap.put("douyu", DouyuImpl)
        mMap.put("bilibili", BilibiliImpl)
        mMap.put("huya", HuyaImpl)
        mMap.put("huomao", HuomaoImpl)
        mMap.put("yy", YYImpl)
        mMap.put("longzhu", LongzhuImpl)
        mMap.put("egameqq", EgameqqImpl)
    }

    fun getPlatformImpl(platform: String): IPlatform? {
        return mMap.get(platform)
    }

    /**
     * @return List<"{platform},{platformShowName}">
     */
    fun getAllPlatfrom(fragment: Fragment): List<String> {
        val platformList = mutableListOf<String>()
        mMap.forEach {
            val platformShowName = fragment.resources.getString(it.value.platformShowNameRes)
            val platform = it.value.platform
            platformList.add(platform + "," + platformShowName)
        }
        return platformList
    }
}