package com.acel.livela.net

import android.util.Log
import com.acel.livela.platform.douyu.DouyuApi
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitUtils {
    companion object {
        val okHttpClient = OkHttpClient.Builder()
            //拦截器 动态切换baseUrl
            .addInterceptor {
                val originalRequest = it.request()
                val oldUrl = originalRequest.url()
                Log.d("retrofit请求链接：", oldUrl.toString())
                val builder = Request.Builder()
                val changeBaseUrlList = originalRequest.headers("changeBaseUrl")
//                builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
                if (changeBaseUrlList.size > 0) {
                    builder.removeHeader("changeBaseUrl")
                    val changeBaseUrl = changeBaseUrlList.get(0)
                    //动态切换baseUrl
                    val baseUrl = when {
                        "douyu" == changeBaseUrl -> HttpUrl.parse(DouyuApi.baseUrl)!!
                        else -> HttpUrl.parse("")!!
                    }
                    val newHttpUrl = oldUrl.newBuilder()
                        .scheme(baseUrl.scheme())
                        .host(baseUrl.host())
                        .port(baseUrl.port())
                        .build()
                    val newRequest = builder.url(newHttpUrl).build()
                    it.proceed(newRequest)
                } else
                    it.proceed(originalRequest)

            }
            .retryOnConnectionFailure(true)
            .connectTimeout(5, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://www.baidu.com")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}