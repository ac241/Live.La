package com.acel.streamlivetool.ui.player

import android.util.Log
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
    private var danmuJob: Job? = null
    private var state = State.IDLE

    private enum class State {
        IDLE, CONNECTING, START, STOP, ERROR, RELEASE
    }

    private fun isStarting() = state == State.START

    /**
     * 开启弹幕接收
     */
    fun start(scope: CoroutineScope, anchor: Anchor) {
        synchronized(this) {
            if (anchor == this.anchor && isStarting()) {
                Log.d("acel_log@start", "重复的请求。")
                return
            }
            if (state == State.START)
                stop()
            this.anchor = anchor

            danmuJob = scope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    val result = PlatformDispatcher.getPlatformImpl(anchor)
                        ?.danmuStart(anchor, this@DanmuClient)
                    if (result != null) {
                        if (!result)
                            errorCallback("该平台不支持弹幕功能")
                        else
                            onConnecting()
                    }
                }.onFailure {
                    if (it is IllegalArgumentException) {
                        it.message?.let { it1 -> ToastUtil.toastOnMainThread(it1) }
                    } else {
                        errorCallback("加载弹幕时发生错误:${it.message}")
                    }
                    it.printStackTrace()
                }
            }
        }
    }

    /**
     * 结束弹幕接收
     */
    private fun stop() {
        if (state == State.START)
            synchronized(this) {
                anchor?.let {
                    PlatformDispatcher.getPlatformImpl(it)?.danmuStop(this)
                }
            }
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
        stop()
        mListener = null
        anchor = null
        danmuJob?.cancel()
        state = State.RELEASE
    }

    /**
     * 接收弹幕回调
     * 在调用此函数前，请确保你已经调用过[startCallback]，否则此客户端不能接收到弹幕
     * @link #startCallback
     */
    fun newDanmuCallback(danmu: Danmu) {
        if (isStarting())
            mListener?.onNewDanmu(danmu)
        else
            Log.d("acel_log@newDanmu", "弹幕客户端未启动")
    }

    /**
     * 发生错误 回调
     * 原则上，调用这个函数后，你应该释放推送弹幕的资源，因为错误信息将显示给用户
     */
    fun errorCallback(reason: String) {
        mListener?.onError(reason)
        state = State.ERROR
    }

    /**
     * cookie 相关提示
     */
    fun cookieMsgCallback(reason: String) {
        mListener?.onCookieMsg(reason)
    }

    /**
     * 开始推送回调，告知客户端已经可以接收弹幕，一般是在socket连接后调用。
     * 在开始推送弹幕前，你必须调用此函数，否则客户端不能接收弹幕信息
     */
    fun startCallback() {
        mListener?.onStart()
        state = State.START
    }

    /**
     * 停止收集回调
     */
    fun stopCallBack(reason: String) {
        mListener?.onStop(reason)
        state = State.STOP
    }

    private fun onConnecting() {
        mListener?.onConnecting()
        state = State.CONNECTING
    }

    interface DanmuListener {
        fun onStart() {}
        fun onNewDanmu(danmu: Danmu) {}
        fun onStop(reason: String) {}
        fun onError(reason: String) {}
        fun onCookieMsg(reason: String) {}

        /**
         * 正在连接弹幕推送
         */
        fun onConnecting() {}
    }

}