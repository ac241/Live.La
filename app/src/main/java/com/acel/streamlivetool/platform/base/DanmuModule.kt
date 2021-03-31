package com.acel.streamlivetool.platform.base

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.main.player.DanmuManager

interface DanmuModule {
    fun getDanmuClient(danmuManager: DanmuManager, anchor: Anchor): DanmuClient

//    /**
//     * 弹幕接收器
//     */
//    val danmuClient: DanmuClient?
//        get() = null
//
//    /**
//     * 弹幕开启
//     * 默认以[danmuClient]实现
//     * 如果你复写这个方法，你需要自行实现弹幕接收推送，并且需要同时复写[danmuStop]
//     * @return success true/false  未实现 null
//     */
//    fun danmuStart(
//        anchor: Anchor,
//        danmuManager: DanmuManager
//    ): Boolean {
//        if (this.danmuClient == null) {
//            danmuManager.errorCallback("该平台弹幕功能还没建设", DanmuManager.ErrorType.NOT_SUPPORT)
//            return false
//        }
//        this.danmuStop(danmuManager)
//        this.danmuClient?.onDanmuStart(getCookie(), anchor, danmuManager)
//        return true
//    }
//
//    /**
//     * 弹幕关闭
//     * & [danmuStart]
//     */
//    fun danmuStop(danmuManager: DanmuManager): Boolean {
//        return if (this.danmuClient != null) {
//            this.danmuClient?.onDanmuStop(danmuManager)
//            true
//        } else
//            false
//    }
//
//    /**
//     * 弹幕管理器，用于连接弹幕服务器、接收弹幕、推送弹幕给弹幕客户端
//     */
//    abstract class DanmuClient {
//
//    }

}