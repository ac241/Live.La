package com.acel.streamlivetool.ui.main.public_class

enum class ProcessStatus {
    WAIT, SUCCESS, FAILED, COOKIE_INVALID, CAN_NOT_TRACK, ERROR, NET_TIME_OUT, NET_ERROR;

    fun getValue(): String? {
        return when (this) {
            WAIT -> "等待"
            SUCCESS -> "完成"
            FAILED -> "失败"
            ERROR -> "发生错误"
            COOKIE_INVALID -> "未登录"
            NET_TIME_OUT -> "超时"
            CAN_NOT_TRACK -> "无法追踪"
            NET_ERROR -> "网络错误"
        }
    }
}
