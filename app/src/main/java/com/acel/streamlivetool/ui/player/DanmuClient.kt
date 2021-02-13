package com.acel.streamlivetool.ui.player

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.Danmu
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.ToastUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 弹幕客户端，用于接收弹幕并推送给danmu view进行显示
 */
class DanmuClient {

    private var anchor: Anchor? = null
    private var mListener: DanmuListener? = null
    private var isStart: Boolean = false
    private var danmuJob: Job? = null

    /**
     * 开启弹幕接收
     */
    fun start(scope: CoroutineScope, anchor: Anchor) {
        synchronized(this) {
            if (isStart)
                stop()
            this.anchor = anchor

            danmuJob = scope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    val result = PlatformDispatcher.getPlatformImpl(anchor)
                        ?.danmuStart(anchor, this@DanmuClient)
                    if (result != null) {
                        if (result)
                            isStart = true
                        else
                            errorCallback("该平台不支持弹幕功能")
                    }
                }.onFailure {
                    if (it is IllegalArgumentException) {
                        it.message?.let { it1 -> ToastUtil.toastOnMainThread(it1) }
                    } else {
                        errorCallback("加载弹幕时发生错误")
                    }
                    it.printStackTrace()
                }
            }
        }
    }

    /**
     * 结束弹幕接收
     */
    fun stop() {
        synchronized(this) {
            anchor?.let {
                val result = PlatformDispatcher.getPlatformImpl(it)?.danmuStop(this)
                isStart = false
            }
        }
    }

    fun reset() {
        stop()
        danmuJob?.cancel()
    }

    /**
     * 设置监听器
     */
    fun setListener(listener: DanmuListener) {
        mListener = listener
    }

    /**
     * 释放资源
     */
    fun release() {
        mListener = null
        anchor = null
        danmuJob?.cancel()
    }

    /**
     * 接收弹幕回调
     */
    fun newDanmuCallback(danmu: Danmu) {
        mListener?.onNewDanmu(danmu)
//        Log.d("acel_log@newDanmuCallBack", "${danmu.msg}")
    }

    /**
     * 发生错误回调
     */
    fun errorCallback(reason: String) {
        mListener?.onError(reason)
//        Log.d("acel_log@errorCallBack", "$reason")
    }

    /**
     * cookie 相关提示
     */
    fun cookieMsgCallback(reason: String) {
        mListener?.onCookieMsg(reason)
    }

    /**
     * 开始收集回调
     */
    fun startCallback() {
        mListener?.onStart()
    }

    /**
     * 停止收集回调
     */
    fun stopCallBack(reason: String) {
        mListener?.onStop(reason)
    }

    interface DanmuListener {
        fun onStart() {}
        fun onNewDanmu(danmu: Danmu) {}
        fun onStop(reason: String) {}
        fun onError(reason: String) {}
        fun onCookieMsg(reason: String){}
    }

}