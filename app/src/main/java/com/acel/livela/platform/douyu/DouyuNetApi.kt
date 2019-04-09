package com.acel.livela.platform.douyu

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface DouyuNetApi {
    companion object {
        val baseUrl = "https://www.douyu.com/"
    }

    @GET
    fun getBaidu(): Call<String>

    @GET("https://m.douyu.com/{id}")
    fun getRoomInfo(@Path("id") id: String): Call<String>
}