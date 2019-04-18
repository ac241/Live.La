package com.acel.livela.platform.bilibili

import com.acel.livela.platform.bilibili.bean.PlayUrl
import com.acel.livela.platform.bilibili.bean.RoomInfo
import com.acel.livela.platform.bilibili.bean.StaticRoomInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BilibiliApi {
    companion object {
        val baseUrl = "https://live.bilibili.com/"
    }
//    @Headers("changeBaseUrl:douyu")
//    @GET
//    fun getBaidu(): Call<String>

    @GET("https://api.live.bilibili.com/room/v1/Room/get_info")
    fun getRoomInfo(@Query("room_id") id: String): Call<RoomInfo>

    @GET("https://api.live.bilibili.com/room/v1/Room/playUrl")
    fun getPlayUrl(@Query("cid") cid: String): Call<PlayUrl>

    @GET("https://api.live.bilibili.com/room/v1/RoomStatic/get_room_static_info")
    fun getStaticInfo(@Query("room_id") id: Int): Call<StaticRoomInfo>

}