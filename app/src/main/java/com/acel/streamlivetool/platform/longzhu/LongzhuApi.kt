package com.acel.streamlivetool.platform.huya

import com.acel.streamlivetool.platform.longzhu.bean.LiveStream
import com.acel.streamlivetool.platform.longzhu.bean.RoomStatus
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LongzhuApi {
    companion object {
        val baseUrl = "https://www.longzhu.com/"
    }
//    @Headers("changeBaseUrl:douyu")
//    @GET
//    fun getBaidu(): Call<String>

    @GET("http://star.longzhu.com/{id}")
    fun getHtml(@Path("id") id: String): Call<String>

    @GET("http://roomapicdn.longzhu.com/room/roomstatus")
    fun roomStatus(@Query("roomid") id: String): Call<RoomStatus>

    @GET("http://livestream.longzhu.com/live/getlivePlayurl?hostPullType=2&isAdvanced=true&playUrlsType=1")
    fun liveStream(@Query("roomId") id: String): Call<LiveStream>
}