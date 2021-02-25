package com.acel.streamlivetool.platform.bilibili

import com.acel.streamlivetool.platform.bilibili.bean.*
import retrofit2.Call
import retrofit2.http.*

interface BilibiliApi {

    @GET("https://api.live.bilibili.com/room/v1/Room/get_info")
    fun getRoomInfo(@Query("room_id") id: String): Call<RoomInfo>

    @GET("https://api.live.bilibili.com/room/v1/Room/playUrl")
    fun getPlayUrl(@Query("cid") cid: String): Call<PlayUrl>

    @GET("https://api.live.bilibili.com/room/v1/RoomStatic/get_room_static_info")
    fun getStaticInfo(@Query("room_id") id: Long): Call<StaticRoomInfo>

    @GET("https://api.live.bilibili.com/xlive/web-ucenter/user/following?page_size=29")
    fun getFollowing(
        @Header("Cookie") cookie: String,
        @Query("page") page: Int
    ): Call<Following>

    @GET("https://api.live.bilibili.com/xlive/web-room/v1/index/getRoomPlayInfo?play_url=1&mask=1&platform=web")
    fun getRoomPlayInfo(
        @Query("room_id") roomId: String,
        @Query("qn") qn: Int = 10000
    ): Call<RoomPlayInfo>

    @GET("https://api.live.bilibili.com/relation/v1/Feed/getList?page_size=10")
    fun getLivingList(
        @Header("Cookie") cookie: String,
        @Query("page") page: Int
    ): Call<LivingList>

    @GET("https://api.bilibili.com/x/web-interface/search/type?context=&search_type=live_user")
    fun search(
        @Query("keyword") keyword: String
    ): Call<SearchResult>

    @GET("https://api.live.bilibili.com/xlive/app-interface/v1/relation/liveAnchor")
    fun liveAnchor(
        @Header("Cookie") cookie: String
    ): Call<LiveAnchor>

    @GET("https://api.live.bilibili.com/xlive/app-interface/v1/relation/unliveAnchor?page=1&pagesize=500")
    fun unLiveAnchor(
        @Header("Cookie") cookie: String
    ): Call<UnliveAnchor>

    @GET("https://api.live.bilibili.com/xlive/web-room/v1/index/getH5InfoByRoom")
    fun getH5InfoByRoom(@Query("room_id") id: Long): Call<H5Info>

    @FormUrlEncoded
    @POST("https://api.bilibili.com/x/relation/modify")
    fun follow(
        @Header("Cookie") cookie: String,
//        @FieldMap map: MutableMap<String, String>,
        @Field("fid") fid: Long,
        @Field("csrf") csrf: String,
        @Field("act") act: Int = 1,
        @Field("re_src") re_src: Int = 11
    ): Call<FollowResponse>

    @Headers(
        "Accept: application/json, text/javascript, */*; q=0.01",
        "Accept-Encoding: gzip, deflate, br",
        "Accept-Language: zh-CN,zh;q=0.9,zh-TW;q=0.8,en-US;q=0.7,en;q=0.6",
        "Connection: keep-alive",
        "Host: api.live.bilibili.com",
        "Origin: https://live.bilibili.com",
        "Referer: https://live.bilibili.com/",
        "Sec-Fetch-Dest: empty",
        "Sec-Fetch-Mode: cors",
        "Sec-Fetch-Site: same-site",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36"
    )
    @GET("https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo?type=0")
    fun getDanmuInfo(
        @Header("Cookie") cookie: String,
        @Query("id") id: String
    ): Call<DanmuInfo>
}