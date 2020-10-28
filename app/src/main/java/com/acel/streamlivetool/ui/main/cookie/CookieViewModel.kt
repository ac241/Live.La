package com.acel.streamlivetool.ui.main.cookie

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.main.AnchorListManager
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.ToastUtil.toast


class CookieViewModel : ViewModel() {
    private val anchorListManager = AnchorListManager.instance
    lateinit var anchorList: List<Anchor>
    lateinit var platform: String
    lateinit var iPlatform: IPlatform

    fun bindPlatform(platform: String) {
        this.platform = platform
        iPlatform = PlatformDispatcher.getPlatformImpl(platform)
            ?: throw IllegalArgumentException("platform impl does not exist for $platform")
        anchorListManager.initPlatform(iPlatform)
        anchorList = anchorListManager.getAnchorList(iPlatform)
    }

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

    internal fun updateAnchorList() {
        _liveDataUpdateState.postValue(UpdateState.UPDATING)
        try {
            val anchorsCookieMode =
                anchorListManager.updateAnchorList(iPlatform)
            if (anchorsCookieMode != null) {
                if (!anchorsCookieMode.isCookieOk) {
                    _liveDataShowLoginText.postValue(true)
                    notifyDataChange()
                    runOnUiThread {
                        toast(if (anchorsCookieMode.message.isEmpty()) "请先登录" else anchorsCookieMode.message)
                    }
                } else {
                    with(anchorsCookieMode.anchorList) {
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

                            notifyDataChange()
                        }
                    }
                    _liveDataShowLoginText.postValue(false)
                }
            }
        } catch (e: Exception) {
            Log.d("getAnchorsCookieMode", "cookie mode获取主播属性失败：cause:${e.javaClass.name}")
            e.printStackTrace()
        } finally {
            _liveDataUpdateState.postValue(UpdateState.FINISH)
        }
    }


    private fun notifyDataChange() {
        Log.d("notifyDataChange", "notifi")
        _liveDataDataChanged.postValue(true)
    }

}

