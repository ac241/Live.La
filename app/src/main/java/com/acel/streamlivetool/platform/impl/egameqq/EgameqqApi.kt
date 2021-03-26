package com.acel.streamlivetool.platform.impl.egameqq

import com.acel.streamlivetool.platform.impl.egameqq.bean.EgameQQAnchor
import com.acel.streamlivetool.platform.impl.egameqq.bean.FollowList
import com.acel.streamlivetool.platform.impl.egameqq.bean.LiveAndProfileInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface EgameqqApi {

    @GET("https://share.egame.qq.com/cgi-bin/pgg_anchor_async_fcgi")
    fun getAnchor(@Query("param") id: String): Call<EgameQQAnchor>

    @GET("https://game.egame.qq.com/cgi-bin/pgg_async_fcgi?param={\"key\":{\"module\":\"pgg_user_profile_mt_svr\",\"method\":\"get_follow_list_mt\",\"param\":{\"uid\":0,\"page_no\":0,\"page_size\":5,\"flag\":1}}}")
    fun getFollowList(@Header("Cookie") cookie: String): Call<FollowList>

    @GET("https://egame.qq.com/search/anchor")
    fun search(@Query("kw") keyword: String): Call<String>

    @GET("https://share.egame.qq.com/cgi-bin/pgg_async_fcgi")
    fun getLiveAndProfileInfo(
        @Query(
            value = "param",
            encoded = true
        ) param: String
    ): Call<LiveAndProfileInfo>
}