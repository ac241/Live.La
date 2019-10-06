package com.acel.streamlivetool.platform.huya

import com.acel.streamlivetool.platform.egameqq.bean.LongZhuAnchor
import com.acel.streamlivetool.platform.egameqq.bean.Param
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EgameqqApi {
    companion object {
        val baseUrl = "https://www.yy.com/"
    }
//    @Headers("changeBaseUrl:douyu")
//    @GET
//    fun getBaidu(): Call<String>

    @GET("https://egame.qq.com/{uid}")
    fun getHtml(@Path("uid") id: String): Call<String>

    @GET("https://share.egame.qq.com/cgi-bin/pgg_anchor_async_fcgi")
    fun getAnchor(@Query("param") id: String): Call<LongZhuAnchor>
}