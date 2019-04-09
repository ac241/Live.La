package com.acel.livela.platform

import com.acel.livela.bean.Anchor
import com.acel.livela.net.RetrofitUtils
import org.jetbrains.annotations.Nullable
import retrofit2.Retrofit

interface IPlatform {
    /**
     * 平台名，例如"Douyu"
     */
    val platform: String

    /**
     * Retrofit实例
     */
    val retrofit: Retrofit get() = RetrofitUtils.retrofit

    /**
     * 获取直播间信息
     * @return Anchor
     */
    fun getAnchor(): Anchor

    /**
     * 获取直播状态
     */
    fun getStatus()

    /**
     * 获取直播流
     */
    fun getStreamingLive()

    /**
     * 打开该平台直播间
     */
    fun startAppById()

    /**
     * 搜索直播间
     * @return 如果没有此功能，返回null
     */
    @Nullable
    fun searchAnchor() {

    }


}