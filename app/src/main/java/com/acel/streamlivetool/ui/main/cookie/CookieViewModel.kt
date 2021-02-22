package com.acel.streamlivetool.ui.main.cookie

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.main.AnchorListManager
import com.acel.streamlivetool.util.AppUtil.mainThread
import com.acel.streamlivetool.util.ToastUtil.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class CookieViewModel : ViewModel() {
    private val anchorListManager = AnchorListManager.instance
    lateinit var anchorList: List<Anchor>
    lateinit var platform: String
    private lateinit var iPlatform: IPlatform

    fun bindPlatform(platform: String) {
        this.platform = platform
        iPlatform = PlatformDispatcher.getPlatformImpl(platform)
            ?: throw IllegalArgumentException("platform impl does not exist for [$platform]")
        anchorListManager.initPlatform(iPlatform)
        anchorList = anchorListManager.getAnchorList(iPlatform)
    }

    var updateJob: Job? = null

    //更新状态
    private val _liveDataUpdateStatus = MutableLiveData<UpdateStatus>().also {
        it.value = UpdateStatus.PREPARE
    }

    val liveDataUpdateState: LiveData<UpdateStatus>
        get() = _liveDataUpdateStatus

    enum class UpdateStatus {
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

    //更新结果的提示信息
    private val _liveDataUpdateAnchorMsg =
        MutableLiveData<UpdateAnchorMsg>().also { it.value = UpdateAnchorMsg(false, null) }
    val liveDataUpdateAnchorMsg: LiveData<UpdateAnchorMsg>
        get() = _liveDataUpdateAnchorMsg

    data class UpdateAnchorMsg(var show: Boolean, var msg: String?)

    private fun MutableLiveData<UpdateAnchorMsg>.update(show: Boolean, msg: String?) {
        value?.show = show
        value?.msg = msg
        this.postValue(value)
    }
    //live data end

    internal fun updateAnchorList() {
        updateJob?.cancel()
        _liveDataUpdateStatus.value = UpdateStatus.UPDATING
        updateJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val result =
                    anchorListManager.updateAnchorList(iPlatform)
                if (result != null) {
                    if (!result.success && !result.isCookieValid) {
                        _liveDataShowLoginText.postValue(true)
                        notifyDataChange()
                        mainThread {
                            toast("${iPlatform.platformName} " + if (result.message.isEmpty()) "请先登录" else result.message)
                        }
                    } else {
                        with(result.anchorList) {
                            if (this != null) {
                                if (this.isEmpty()) {
                                    _liveDataUpdateAnchorMsg.update(
                                        true,
                                        if (result.message.isEmpty()) "无数据" else result.message
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
            }.onFailure {
                Log.d("getAnchorsCookieMode", "cookie mode获取主播属性失败：cause:${it.javaClass.name}")
                it.printStackTrace()
            }
        }
        updateJob?.start()
        _liveDataUpdateStatus.postValue(UpdateStatus.FINISH)
    }


    private fun notifyDataChange() {
        _liveDataDataChanged.postValue(true)
    }

}

