package com.acel.livela.platform

import android.content.Context
import com.acel.livela.bean.Anchor
import com.acel.livela.bean.AnchorStatus
import com.acel.livela.net.RetrofitUtils
import org.jetbrains.annotations.Nullable
import retrofit2.Retrofit

interface IPlatform {
    /**
     * 平台名，例如"Douyu"
     */
    val platform: String

    /**
     * 平台显示名resId，例如：R.string.douyu
     */
    val platformShowNameRes: Int

    /**
     * Retrofit实例
     */
    val retrofit: Retrofit get() = RetrofitUtils.retrofit

    /**
     * 获取直播间信息
     * @param //todo
     * @return Anchor if fail return null
     */
    fun getAnchor(queryAnchor: Anchor): Anchor?

    /**
     * 获取直播状态
     * @return AnchorStatus if fail return null
     */
    fun getStatus(queryAnchor: Anchor): AnchorStatus?

    /**
     * 获取直播流
     */
    fun getStreamingLiveUrl(queryAnchor: Anchor): String?

    /**
     * 打开直播间
     */
    fun startApp(context: Context, anchor: Anchor)

    /**
     * 搜索直播间
     * @return 如果没有此功能，返回null
     */
    @Nullable
    fun searchAnchor() {

    }


}