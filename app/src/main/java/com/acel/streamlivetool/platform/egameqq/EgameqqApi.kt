package com.acel.streamlivetool.platform.egameqq

import com.acel.streamlivetool.platform.egameqq.bean.EgameQQAnchor
import com.acel.streamlivetool.platform.egameqq.bean.FollowList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface EgameqqApi {
    @GET("https://egame.qq.com/{uid}")
    fun getHtml(@Path("uid") id: String): Call<String>

    @GET("https://share.egame.qq.com/cgi-bin/pgg_anchor_async_fcgi")
    fun getAnchor(@Query("param") id: String): Call<EgameQQAnchor>

    @GET("https://game.egame.qq.com/cgi-bin/pgg_async_fcgi?param={\"key\":{\"module\":\"pgg_user_profile_mt_svr\",\"method\":\"get_follow_list_mt\",\"param\":{\"uid\":0,\"page_no\":0,\"page_size\":5,\"flag\":1}}}")
    fun getFollowList(@Header("cookies") cookies: String): Call<FollowList>
}