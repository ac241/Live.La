package com.acel.streamlivetool.manager

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.IPlatform

interface UpdateResultReceiver {

    /**
     * 更新结果的类型
     */
    enum class ResultType {
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

    data class ResultSingleAnchor(
        val success: Boolean,
        val anchor: Anchor,
        val resultType: ResultType
    )

    /**
     * 更新结果
     */
    data class ResultCookieMode(
        val isSuccess: Boolean,
        val resultType: ResultType,
        val iPlatform: IPlatform
    )

    fun onUpdateFinish(resultList: List<ResultSingleAnchor>)

    fun onCookieModeUpdateFinish(resultList: List<ResultCookieMode>)
}