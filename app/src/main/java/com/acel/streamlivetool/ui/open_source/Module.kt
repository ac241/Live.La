package com.acel.streamlivetool.ui.open_source

data class Module(
    val name: String,
    val `package`: String,
    val path: String,
    val author: String,
    val licensed: String,
    val hideWhenNotFullVersion: Boolean = false
)