package com.acel.streamlivetool.platform.bilibili

import com.acel.streamlivetool.platform.bilibili.bean.Following
import com.acel.streamlivetool.platform.bilibili.bean.PlayUrl
import com.acel.streamlivetool.platform.bilibili.bean.RoomInfo
import com.acel.streamlivetool.platform.bilibili.bean.StaticRoomInfo
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

    @GET("https://api.live.bilibili.com/xlive/web-ucenter/user/following?page=1&page_size=10")
    fun getFollowing(
        @Header("cookies") cookies: String,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): Call<Following>
}