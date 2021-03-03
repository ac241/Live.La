package com.acel.streamlivetool.ui.main.player

import android.util.Log
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.Danmu
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.platform.PlatformDispatcher.platformImpl
import com.acel.streamlivetool.util.ToastUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 弹幕客户端，用于接收弹幕并推送给danmu view进行显示
 */
class DanmuClient(viewModelScope: CoroutineScope) {

    private var anchor: Anchor? = null
    private var mListener: DanmuListener? = null
    private var danmuJob: Job? = null
    private var state = State.IDLE
    private var scope: CoroutineScope? = viewModelScope

    private enum class State {
        IDLE, CONNECTING, RECONNECTING, START, STOP, ERROR, RELEASE
    }

    enum class ErrorType {
        NORMAL, NOT_SUPPORT, COOKIE_INVALID
    }

    private fun isStarting() = state == State.START

    /**
     * 开启弹幕接收
     */
    fun start(anchor: Anchor, startMessage: String = "正在连接弹幕服务器"): Boolean {
        synchronized(this) {
            if (anchor == this.anchor && isStarting()) {
                Log.d("acel_log@start", "重复的请求。")
                return false
            }
            if (state == State.START)
                stop("断开")
            this.anchor = anchor
            onConnecting(startMessage)
            danmuJob = scope?.launch(Dispatchers.IO) {
                runCatching {
                    anchor.platformImpl()?.danmuStart(anchor, this@DanmuClient)
                }.onFailure {
                    if (it is IllegalArgumentException) {
                        it.message?.let { it1 -> ToastUtil.toastOnMainThread(it1) }
                    } else {
                        errorCallback("${it.message}")
                    }
                    it.printStackTrace()
                }
            }
            return true
        }
    }

    /**
     * 结束弹幕接收
     */
    fun stop(reason: String = "") {
        synchronized(this) {
            danmuJob?.cancel()
            anchor?.let {
                PlatformDispatcher.getPlatformImpl(it)?.danmuStop(this)
            }
//            anchor = null
            stopCallBack(reason)
        }
    }

    /**
     * 重新启动
     */
    fun restart(message: String) {
        val anchor = anchor
        stop(message)
        if (anchor != null) {
            start(anchor, message)
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
        scope = null
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
     * 调用这个函数后，[stop]将被调用
     * @param errorType 错误类型，一般不填用默认值。
     */
    fun errorCallback(reason: String, errorType: ErrorType = ErrorType.NORMAL) {
        stop()
        mListener?.onError(reason, errorType)
        state = State.ERROR
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

    /**
     * 弹幕连接中
     */
    private fun onConnecting(message: String) {
        mListener?.onConnecting(message)
        state = State.CONNECTING
    }

    /**
     * 弹幕监听器
     */
    interface DanmuListener {
        fun onConnecting(message: String) {}
        fun onStart() {}
        fun onNewDanmu(danmu: Danmu) {}
        fun onStop(reason: String) {}
        fun onError(reason: String, errorType: ErrorType) {}
    }

}