package com.acel.streamlivetool.platform.base

/**
 * 平台信息
 */
interface IMessage {
    /**
     * 平台名，例如"Douyu"
     */
    val platform: String

    /**
     * 平台显示名resId，例如：R.string.douyu
     */
    val platformShowNameRes: Int

    /**
     * 平台名
     */
    val platformName: String

    /**
     * icon res
     */
    val iconRes: Int



}