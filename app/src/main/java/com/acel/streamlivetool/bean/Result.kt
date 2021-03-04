package com.acel.streamlivetool.bean

data class Result(
    val success: Boolean,
    val msg: String,
    val support: Boolean = true,
    val code: Int = 0,
    val data: String = ""
)