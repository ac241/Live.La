package com.acel.streamlivetool.platform.huya

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface HuyaApi {
    @GET("https://www.huya.com/{id}")
    fun getHtml(@Path("id") id: String): Call<String>

}