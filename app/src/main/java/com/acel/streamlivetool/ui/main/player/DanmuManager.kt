package com.acel.streamlivetool.ui.main.player

import android.util.Log
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.Danmu
import com.acel.streamlivetool.platform.PlatformDispatcher.platformImpl
import com.acel.streamlivetool.platform.base.DanmuClient
import com.acel.streamlivetool.util.ToastUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 弹幕客户端，用于接收弹幕并推送给danmu view显示
 */
class DanmuManager(viewModelScope: CoroutineScope) {

    private var anchor: Anchor? = null
    private var mListener: DanmuListener? = null
    private var danmuJob: Job? = null
    private var state = State.IDLE
    private var scope: CoroutineScope? = viewModelScope
    private var danmuClient: DanmuClient? = null

    private enum class State {
        IDLE, CONNECTING, START, STOP, ERROR, RELEASE
    }

    enum class ErrorType {

        //一般错误类型
        NORMAL,

        //不支持错误类型
        NOT_SUPPORT,

        //cookie不合法错误类型
        COOKIE_INVALID,

        //特殊错误类型，一般用于特殊情况，比如需要配置弹幕参数。
        SPECIAL
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
                stop("加载->断开")
            this.anchor = anchor
            onConnecting(startMessage)
            danmuClient =
                    anchor.platformImpl()?.danmuModule?.getDanmuClient(this@DanmuManager, anchor)
            if (danmuClient == null) {
                errorCallback("该平台暂不支持", ErrorType.NOT_SUPPORT)
                return false
            }
            danmuJob = scope?.launch(Dispatchers.IO) {
                runCatching {
                    danmuClient?.start(anchor, this@DanmuManager)
                }.onFailure {
                    if (it is IllegalArgumentException) {
                        it.message?.let { it1 -> ToastUtil.toastOnMainThread(it1) }
                    } else {
                        errorCallback("${it.message}", ErrorType.NORMAL)
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
                danmuClient?.stop()
                danmuClient = null
            }
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
     * @param errorType 错误类型，一般不填使用默认值[ErrorType.NORMAL]
     * [ErrorType.COOKIE_INVALID]cookie非法时使用,
     * [ErrorType.SPECIAL]用于处理特殊情况，比如需要需要配置弹幕信息,
     * [ErrorType.NOT_SUPPORT]为未实现弹幕功能的值，一般不主动使用。
     *
     */
    fun errorCallback(reason: String, client: DanmuClient, anchor: Anchor, errorType: ErrorType = ErrorType.NORMAL) {
        if (client == danmuClient && this.anchor == anchor) {
            stop()
            mListener?.onError(reason, errorType)
            state = State.ERROR
        }
    }

    /**
     *  发生错误 回调 内部使用
     */
    private fun errorCallback(reason: String, errorType: ErrorType) {
        stop()
        mListener?.onError(reason, errorType)
        state = State.ERROR
    }

    /**
     * 开始推送回调，告知客户端已经可以接收弹幕。
     * 一般是在socket连接后调用
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