package com.acel.streamlivetool.platform.huya

import com.acel.streamlivetool.platform.huya.bean.SearchResult
import com.acel.streamlivetool.platform.huya.bean.Subscribe
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface HuyaApi {
    @GET("https://www.huya.com/{id}")
    fun getHtml(@Path("id") id: String): Call<String>

    @GET("https://m.huya.com/{id}")
    fun getMHtml(
        @Path("id") id: String,
        @Header("User-Agent") userAgent: String = "Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Mobile Safari/537.36"
    ): Call<String>

    @GET("https://fw.huya.com/dispatch?do=subscribeList&page=1&pageSize=100")
    fun getSubscribe(
        @Header("Cookie") cookie: String,
        @Query("uid") uid: String
    ): Call<Subscribe>

    @GET("https://search.cdn.huya.com/?m=Search&do=getSearchContent&typ=-5&rows=10")
    fun search(@Query("q") keyword: String): Call<SearchResult>
}