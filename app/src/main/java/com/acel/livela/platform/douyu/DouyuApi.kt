package com.acel.livela.platform.douyu

import com.acel.livela.platform.douyu.bean.RoomInfo
import com.acel.livela.platform.douyu.bean.RoomInfoMsg
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface DouyuApi {
    companion object {
        val baseUrl = "https://www.douyu.com/"
    }
//    @Headers("changeBaseUrl:douyu")
//    @GET
//    fun getBaidu(): Call<String>

    @GET("https://m.douyu.com/{id}")
    fun getRoomInfo(@Path("id") id: String): Call<String>

    @GET("https://open.douyucdn.cn/api/RoomApi/room/{id}")
    fun getRoomInfoFromOpen(@Path("id") id: String): Call<RoomInfo>

    @GET("https://open.douyucdn.cn/api/RoomApi/room/{id}")
    fun getRoomInfoMsg(@Path("id") id: String): Call<RoomInfoMsg>
}