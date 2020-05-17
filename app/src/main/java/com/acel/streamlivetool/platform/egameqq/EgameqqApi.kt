package com.acel.streamlivetool.platform.egameqq

import com.acel.streamlivetool.platform.egameqq.bean.LongZhuAnchor
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EgameqqApi {
    @GET("https://egame.qq.com/{uid}")
    fun getHtml(@Path("uid") id: String): Call<String>

    @GET("https://share.egame.qq.com/cgi-bin/pgg_anchor_async_fcgi")
    fun getAnchor(@Query("param") id: String): Call<LongZhuAnchor>
}