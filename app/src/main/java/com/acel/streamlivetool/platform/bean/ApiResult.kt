package com.acel.streamlivetool.platform.bean

data class ApiResult<T>(
    val success: Boolean,
    val msg: String? = null,
    val data: T? = null,
    val support: Boolean = true,
    val cookieValid: Boolean = true,
    val code: Int = 0
)