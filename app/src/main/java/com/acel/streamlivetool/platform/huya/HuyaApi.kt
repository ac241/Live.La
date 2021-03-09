package com.acel.streamlivetool.platform.huya

import com.acel.streamlivetool.platform.huya.bean.FollowResponse
import com.acel.streamlivetool.platform.huya.bean.SearchResult
import com.acel.streamlivetool.platform.huya.bean.Subscribe
import retrofit2.Call
import retrofit2.http.*

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

    /**
     * @param pid 目标id
     * @param uid 自己的id
     * @param timestamp 时间戳 1605764330470
     */
    @Headers(
        "Referer: https://www.huya.com/",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.190 Safari/537.36",
        "Accept-Language: zh-CN,zh;q=0.9",
        "Accept: */*",
        "Accept-Encoding: gzip, deflate, br",
        "Sec-Fetch-Dest: script",
        "Sec-Fetch-Mode: no-cors",
        "Sec-Fetch-Site: same-site",
        "DNT: 1",
    )
    @GET("https://subapi.huya.com/user/liveSubscribe?from=act&source=web&type=Subscribe")
    fun follow(
        @Header("Cookie") cookie: String,
        @Query("pid") pid: String,
        @Query("uid") uid: String,
        @Query("_") timestamp: Long,
    ): Call<FollowResponse>

}