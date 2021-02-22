package com.acel.streamlivetool.platform

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.bilibili.BilibiliImpl
import com.acel.streamlivetool.platform.douyu.DouyuImpl
import com.acel.streamlivetool.platform.egameqq.EgameqqImpl
import com.acel.streamlivetool.platform.huomao.HuomaoImpl
import com.acel.streamlivetool.platform.huya.HuyaImpl

object PlatformDispatcher {
    private val platformMap = mutableMapOf<String, IPlatform>()

    init {
        val instanceMap = mapOf(
            Pair("douyu", DouyuImpl.INSTANCE),
            Pair("bilibili", BilibiliImpl.INSTANCE),
            Pair("huya", HuyaImpl.INSTANCE),
            Pair("egameqq", EgameqqImpl.INSTANCE),
            Pair("huomao", HuomaoImpl.INSTANCE)
        )

        val platformArray =
            MyApplication.application.resources.getStringArray(R.array.platform)
        platformArray.forEach { source ->
            val instance = instanceMap[source]
            instance?.let { platformMap[source] = instance }
        }
    }

    fun getPlatformImpl(platform: String): IPlatform? {
        return platformMap[platform]
    }

    fun getPlatformImpl(anchor: Anchor): IPlatform? {
        return platformMap[anchor.platform]
    }

    /**
     * @return Map<platform name ,Platform Instance>
     */
    fun getAllPlatformInstance(): Map<String, IPlatform> {
        return platformMap
    }

    /**
     * @return List<"platform,platformShowName">
     */
    fun getAllPlatform(): List<String> {

        val platformList = mutableListOf<String>()
        platformMap.forEach {
            val platform = it.value.platform
            platformList.add("$platform,${it.value.platformName}")
        }
        return platformList
    }

    fun Anchor.platformImpl(): IPlatform? {
        return getPlatformImpl(this)
    }
}