package com.acel.streamlivetool.net

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitUtils {
    companion object {
        val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                //拦截器 动态切换baseUrl
//                .addInterceptor(AddCookiesInterceptor())
                .retryOnConnectionFailure(true)
                .connectTimeout(5, TimeUnit.SECONDS)
                .build()
        }

        val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://www.baidu.com")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}

/**
 * 用于添加cookie的拦截器
 */
private class AddCookiesInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        val requestBuilder = originRequest.newBuilder()
        //添加cookie
        val cookies = originRequest.header("cookies")
        cookies?.let {
            requestBuilder.removeHeader("cookies")
            requestBuilder.addHeader("Cookie", it)
        }
        return chain.proceed(requestBuilder.build())
    }
}
