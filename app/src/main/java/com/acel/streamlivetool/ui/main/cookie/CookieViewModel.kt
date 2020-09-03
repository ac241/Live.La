package com.acel.streamlivetool.ui.main.cookie

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.main.public_class.ProcessStatus
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.ToastUtil.toast


class CookieViewModel :
    ViewModel() {
    class ViewModeFactory :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CookieViewModel() as T
        }
    }

    var platform: String? = null
    val anchorList = mutableListOf<Anchor>()

    //live data start
    //是否在更新状态
    private val _liveDataUpdateState = MutableLiveData<UpdateState>().also {
        it.value = UpdateState.PREPARE
    }
    val liveDataUpdateState: LiveData<UpdateState>
        get() = _liveDataUpdateState

    enum class UpdateState {
        PREPARE, UPDATING, FINISH
    }

    //数据更新
    private val _liveDataDataChanged = MutableLiveData<Boolean>().also { it.value = false }
    val liveDataDataChanged: LiveData<Boolean>
        get() = _liveDataDataChanged

    //显示登录框
    private val _liveDataShowLoginText = MutableLiveData<Boolean>().also {
        it.value = false
    }
    val liveDataShowLoginText: LiveData<Boolean>
        get() = _liveDataShowLoginText

    //更新anchor信息
    private val _liveDataUpdateAnchorMsg =
        MutableLiveData<ListMsg>().also { it.value = ListMsg(false, null) }
    val liveDataUpdateAnchorMsg: LiveData<ListMsg>
        get() = _liveDataUpdateAnchorMsg

    data class ListMsg(var show: Boolean, var msg: String?)

    private fun MutableLiveData<ListMsg>.update(show: Boolean, msg: String?) {
        value?.show = show
        value?.msg = msg
        this.postValue(value)
    }
    //live data end

    internal fun getAnchors() {
        _liveDataUpdateState.postValue(UpdateState.UPDATING)
        MainExecutor.execute {
            try {
                val anchorsCookieMode =
                    platform?.let {
                        PlatformDispatcher.getPlatformImpl(it)?.getAnchorsWithCookieMode()
                    }
                if (anchorsCookieMode != null) {
                    if (!anchorsCookieMode.cookieOk) {
                        _liveDataShowLoginText.postValue(true)
                        anchorList.clear()
                        notifyDataChange()
                        runOnUiThread {
                            toast(if (anchorsCookieMode.message.isEmpty()) "请先登录" else anchorsCookieMode.message)
                        }
                    } else {
                        with(anchorsCookieMode.anchors) {
                            if (this != null) {
                                if (this.isEmpty()) {
                                    _liveDataUpdateAnchorMsg.update(
                                        true,
                                        if (anchorsCookieMode.message.isEmpty()) "无数据" else anchorsCookieMode.message
                                    )
                                } else
                                    _liveDataUpdateAnchorMsg.update(
                                        false,
                                        null
                                    )
                                anchorList.clear()
                                anchorList.addAll(this)
                                com.acel.streamlivetool.util.AnchorListUtil.insertStatusPlaceHolder(
                                    anchorList
                                )
                                notifyDataChange()
                            }
                        }
                        _liveDataShowLoginText.postValue(false)
                    }
                }
            } catch (e: Exception) {
                Log.d("getAnchorsCookieMode", "cookie mode获取主播属性失败：cause:${e.javaClass.name}")
                when (e) {
                    is java.net.SocketTimeoutException -> ProcessStatus.NET_TIME_OUT
                    is java.net.UnknownHostException -> ProcessStatus.NET_TIME_OUT
                    else -> ProcessStatus.NET_TIME_OUT
                }
            } finally {
                _liveDataUpdateState.postValue(UpdateState.FINISH)
            }
        }
    }

    private fun notifyDataChange() {
        _liveDataDataChanged.postValue(true)
    }
}

