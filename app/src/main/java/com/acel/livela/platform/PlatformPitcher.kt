package com.acel.livela.platform

import com.acel.livela.platform.douyu.DouyuImpl

object PlatformPitcher {
    val mMap = mutableMapOf<String, IPlatform>()

    init {
        mMap.put("douyu", DouyuImpl)
    }

    fun getPlatformImpl(platform: String): IPlatform? {
        return mMap.get(platform)
    }
}