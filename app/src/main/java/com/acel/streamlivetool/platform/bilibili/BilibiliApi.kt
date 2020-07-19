package com.acel.streamlivetool.platform.bilibili

import com.acel.streamlivetool.platform.bilibili.bean.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface BilibiliApi {

    @GET("https://api.live.bilibili.com/room/v1/Room/get_info")
    fun getRoomInfo(@Query("room_id") id: String): Call<RoomInfo>

    @GET("https://api.live.bilibili.com/room/v1/Room/playUrl")
    fun getPlayUrl(@Query("cid") cid: String): Call<PlayUrl>

    @GET("https://api.live.bilibili.com/room/v1/RoomStatic/get_room_static_info")
    fun getStaticInfo(@Query("room_id") id: Int): Call<StaticRoomInfo>

    @GET("https://api.live.bilibili.com/xlive/web-ucenter/user/following?page_size=29")
    fun getFollowing(
        @Header("cookies") cookies: String,
        @Query("page") page: Int
    ): Call<Following>

    @GET("https://api.live.bilibili.com/xlive/web-room/v1/index/getRoomPlayInfo?play_url=1&mask=1&qn=1&platform=web")
    fun getRoomPlayInfo(
        @Query("room_id") roomId: String
    ): Call<RoomPlayInfo>

    @GET("https://api.live.bilibili.com/relation/v1/Feed/getList?page_size=10")
    fun getLivingList(
        @Header("cookies") cookies: String,
        @Query("page") page: Int
    ): Call<LivingList>

}