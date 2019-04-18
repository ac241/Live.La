package com.acel.livela.platform.huya

import com.acel.livela.platform.douyu.bean.RoomInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface HuyaApi {
    companion object {
        val baseUrl = "https://www.huya.com/"
    }
//    @Headers("changeBaseUrl:douyu")
//    @GET
//    fun getBaidu(): Call<String>

    @GET("https://www.huya.com/{id}")
    fun getHtml(@Path("id") id: String): Call<String>

}