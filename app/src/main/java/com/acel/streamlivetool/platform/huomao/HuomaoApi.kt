package com.acel.streamlivetool.platform.huomao

import com.acel.streamlivetool.platform.huomao.bean.LiveData
import retrofit2.Call
import retrofit2.http.*

interface HuomaoApi {
    companion object {
        val baseUrl = "https://www.huomao.com/"
    }

    @GET("https://www.huomao.com/{id}")
    fun getRoomInfo(@Path("id") id: String): Call<String>

    @FormUrlEncoded
//    @Multipart
    @POST("https://www.huomao.com/swf/live_data")
    fun getLiveData(
        @FieldMap map: MutableMap<String, String>
    ): Call<LiveData>
}
