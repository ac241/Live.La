package com.acel.streamlivetool.platform.huomao

import com.acel.streamlivetool.platform.huomao.bean.LiveData
import com.acel.streamlivetool.platform.huomao.bean.UsersSubscribe
import retrofit2.Call
import retrofit2.http.*

interface HuomaoApi {

    @GET("https://www.huomao.com/{id}")
    fun getRoomInfo(@Path("id") id: String): Call<String>

    @FormUrlEncoded
//    @Multipart
    @POST("https://www.huomao.com/swf/live_data")
    fun getLiveData(
        @FieldMap map: MutableMap<String, String>
    ): Call<LiveData>

    @GET("https://www.huomao.com/subscribe/getUsersSubscribe?page=1&page_size=100")
    fun getUsersSubscribe(@Header("cookies") cookies: String): Call<UsersSubscribe>


}
