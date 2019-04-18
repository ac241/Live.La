package com.acel.livela.platform

import android.support.v4.app.Fragment
import com.acel.livela.platform.bilibili.BilibiliImpl
import com.acel.livela.platform.douyu.DouyuImpl
import com.acel.livela.platform.huya.HuyaImpl

object PlatformPitcher {
    val mMap = mutableMapOf<String, IPlatform>()

    init {
        mMap.put("douyu", DouyuImpl)
        mMap.put("bilibili", BilibiliImpl)
        mMap.put("huya", HuyaImpl)
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