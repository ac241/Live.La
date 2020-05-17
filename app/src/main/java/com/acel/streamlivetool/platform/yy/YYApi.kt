package com.acel.streamlivetool.platform.yy

import com.acel.streamlivetool.platform.yy.bean.SearchInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YYApi {
    @GET("https://www.yy.com/{sid}")
    fun getHtml(@Path("sid") sid: String): Call<String>

    @GET("https://www.yy.com/search/search2.json?t=-3")
    fun search(@Query("q") id: String): Call<SearchInfo>


}