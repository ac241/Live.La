package com.acel.streamlivetool.platform.huya

import com.acel.streamlivetool.platform.huya.bean.Subscribe
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface HuyaApi {
    @GET("https://www.huya.com/{id}")
    fun getHtml(@Path("id") id: String): Call<String>


    @GET("https://fw.huya.com/dispatch?do=subscribeList&page=1&pageSize=100")
    fun getSubscribe(
        @Header("cookies") cookies: String,
        @Query("uid") uid: String
    ): Call<Subscribe>
}