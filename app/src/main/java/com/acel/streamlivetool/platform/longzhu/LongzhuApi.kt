package com.acel.streamlivetool.platform.longzhu

import com.acel.streamlivetool.platform.longzhu.bean.LiveStream
import com.acel.streamlivetool.platform.longzhu.bean.RoomStatus
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LongzhuApi {
    @GET("https://star.longzhu.com/{id}")
    fun getHtml(@Path("id") id: String): Call<String>

    @GET("https://roomapicdn.longzhu.com/room/RoomAppStatusV2")
    fun roomStatus(@Query("roomId") id: String): Call<RoomStatus>

    @GET("https://livestream.longzhu.com/live/getlivePlayurl?hostPullType=2&isAdvanced=true&playUrlsType=1")
    fun liveStream(@Query("roomId") id: String): Call<LiveStream>
}