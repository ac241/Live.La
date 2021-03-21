package com.acel.streamlivetool.platform

import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
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
    private val platformIconDrawableMap = mutableMapOf<IPlatform, Drawable>()

    init {
        val implMap = mapOf(
            Pair("douyu", DouyuImpl.INSTANCE),
            Pair("bilibili", BilibiliImpl.INSTANCE),
            Pair("huya", HuyaImpl.INSTANCE),
            Pair("egameqq", EgameqqImpl.INSTANCE),
            Pair("huomao", HuomaoImpl.INSTANCE)
        )

        val enablePlatformArray =
            MyApplication.application.resources.getStringArray(R.array.platform)

        enablePlatformArray.forEach { source ->
            val instance = implMap[source]
            instance?.let { platformMap[source] = instance }
        }
        platformMap.forEach {
            ResourcesCompat.getDrawable(MyApplication.application.resources, it.value.iconRes, null)
                ?.let { it1 -> platformIconDrawableMap[it.value] = it1 }
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
    fun getAllPlatformImpl(): Map<String, IPlatform> {
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

    fun Anchor.getIconDrawable(): Drawable? {
        return platformIconDrawableMap[platformImpl()]
    }

    fun Anchor.platformImpl(): IPlatform? {
        return getPlatformImpl(this)
    }
}