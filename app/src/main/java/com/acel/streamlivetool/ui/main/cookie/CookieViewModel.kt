package com.acel.streamlivetool.ui.main.cookie

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.manager.AnchorUpdateManager
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.platform.base.AbstractPlatformImpl
import com.acel.streamlivetool.util.AppUtil.mainThread
import com.acel.streamlivetool.util.ToastUtil.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class CookieViewModel : ViewModel() {
    private lateinit var iPlatform: AbstractPlatformImpl
    private val anchorListManager = AnchorUpdateManager.instance
    lateinit var anchorList: List<Anchor>
    lateinit var platform: String

    fun bindPlatform(platform: String) {
        this.platform = platform
        iPlatform = PlatformDispatcher.getPlatformImpl(platform)
            ?: throw IllegalArgumentException("platform impl does not exist for [$platform]")
        anchorListManager.initPlatform(iPlatform)
        anchorList = anchorListManager.getAnchorList(iPlatform)
    }

    private var updateJob: Job? = null

    //更新状态
    private val _liveDataUpdateStatus = MutableLiveData<UpdateStatus>().also {
        it.value = UpdateStatus.IDLE
    }

    val liveDataUpdateState: LiveData<UpdateStatus>
        get() = _liveDataUpdateStatus

    enum class UpdateStatus {
        IDLE, UPDATING, FINISH
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

    //更新结果的提示信息
    private val _liveDataUpdateAnchorResultMsg =
        MutableLiveData<UpdateAnchorResultMsg>().also {
            it.value = UpdateAnchorResultMsg(false, null)
        }
    val liveDataUpdateAnchorMsg: LiveData<UpdateAnchorResultMsg>
        get() = _liveDataUpdateAnchorResultMsg

    data class UpdateAnchorResultMsg(var show: Boolean, var msg: String?)

    private fun MutableLiveData<UpdateAnchorResultMsg>.update(show: Boolean, msg: String?) {
        value?.show = show
        value?.msg = msg
        this.postValue(value)
    }

    internal fun updateAnchorList() {
        updateJob?.cancel()
        _liveDataUpdateStatus.value = UpdateStatus.UPDATING
        updateJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val result =
                    anchorListManager.getAnchorsByCookie(iPlatform)
                if (result != null) {
                    if (!result.success && !result.cookieValid) {
                        _liveDataShowLoginText.postValue(true)
                        notifyDataChange()
                        mainThread {
                            toast("${iPlatform.platformName} " + if (result.msg.isNullOrEmpty()) "请先登录" else result.msg)
                        }
                    } else {
                        with(result.data) {
                            if (this != null) {
                                if (this.isEmpty()) {
                                    _liveDataUpdateAnchorResultMsg.update(
                                        true,
                                        if (result.msg.isNullOrEmpty()) "无数据" else result.msg
                                    )
                                } else
                                    _liveDataUpdateAnchorResultMsg.update(
                                        false,
                                        null
                                    )
                                notifyDataChange()
                            }
                        }
                        _liveDataShowLoginText.postValue(false)
                    }
                }
            }.onFailure {
                Log.d("getAnchorsCookieMode", "cookie mode获取主播属性失败：cause:${it.javaClass.name}")
                it.printStackTrace()
            }
            _liveDataUpdateStatus.postValue(UpdateStatus.FINISH)
        }
        updateJob?.start()
    }


    private fun notifyDataChange() {
        _liveDataDataChanged.postValue(true)
    }

}

