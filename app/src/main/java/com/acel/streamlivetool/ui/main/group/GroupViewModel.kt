package com.acel.streamlivetool.ui.main.group

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.ui.main.public_class.ProcessStatus
import com.acel.streamlivetool.util.AnchorListUtil
import com.acel.streamlivetool.util.PreferenceConstant.groupModeUseCookie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class GroupViewModel : ViewModel() {

    class ViewModeFactory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GroupViewModel() as T
        }
    }

    //数据库读取的anchorList
    private val anchorRepository =
        AnchorRepository.getInstance()

    //    live data start
    //排序后的anchorList
    val sortedAnchorList = MediatorLiveData<MutableList<Anchor>>().also {
        it.value = Collections.synchronizedList(mutableListOf())
        it.addSource(anchorRepository.anchorList) { sourceList ->
            it.value?.clear()
            it.value?.addAll(sourceList)
            it.postValue(it.value)
            updateAllAnchor()
        }
    }

    private val _liveDataUpdateDetails = MutableLiveData<UpdateState>().also {
        it.value = UpdateState.PREPARE
    }
    val liveDataUpdateDetails: LiveData<UpdateState>
        get() = _liveDataUpdateDetails

    enum class UpdateState {
        PREPARE, UPDATING, FINISH
    }

    //右上角窗口提示更新进度
    private val _liveDataUpdateAnchorResult =
        MutableLiveData<UpdateAnchorResult>().also { it.value = UpdateAnchorResult(false, null) }
    val liveDataUpdateAnchorResult: LiveData<UpdateAnchorResult>
        get() = _liveDataUpdateAnchorResult

    //snackBar通知live data
    private val _snackBarMsg = MutableLiveData<SpannableStringBuilder>()
    val snackBarMsg
        get() = _snackBarMsg

    data class UpdateAnchorResult(var complete: Boolean, var result: String?)

    private fun MutableLiveData<UpdateAnchorResult>.update(complete: Boolean, result: String) {
        value?.complete = complete
        value?.result = result
        this.postValue(value)
    }

//    live data end

    var lastGetAnchorsTime = 0L

    @Synchronized
    private fun notifyAnchorListChange() {
        AnchorListUtil.sortAnchorListByStatus(sortedAnchorList.value!!)
        AnchorListUtil.insertStatusPlaceHolder(sortedAnchorList.value!!)
        sortedAnchorList.postValue(sortedAnchorList.value)
    }

    private fun updateAnchor(anchor: Anchor) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
                platformImpl?.let {
                    val result = platformImpl.updateAnchorData(anchor)
                    if (result)
                        notifyAnchorListChange()
                }
                hideRefreshBtn()
            }.onFailure {
                Log.d("updateAnchor", "更新主播信息失败：cause:${it.javaClass.name}------$anchor")
                hideRefreshBtn()
            }
        }
    }

    fun updateAllAnchor() {
        _liveDataUpdateDetails.postValue(UpdateState.UPDATING)
        if (groupModeUseCookie)
            updateAllAnchorByCookie()
        else
            sortedAnchorList.value?.forEach { anchor ->
                updateAnchor(anchor)
            }
        lastGetAnchorsTime = System.currentTimeMillis()
    }

    fun deleteAnchor(anchor: Anchor) {
        anchorRepository.deleteAnchor(anchor)
    }

    private fun MutableLiveData<UpdateAnchorsByCookieResult>.update(
        platform: IPlatform,
        status: ProcessStatus
    ) {
        value?.apply {
            map[platform] = status
            postValue(value)
        }
    }

    private fun MutableLiveData<UpdateAnchorsByCookieResult>.insert(
        platform: IPlatform
    ) {
        value?.apply {
            map[platform] = ProcessStatus.WAIT
            postValue(value)
        }
    }

    private fun MutableLiveData<UpdateAnchorsByCookieResult>.allAdded() {
        value?.apply {
            this.isAllAdded = true
        }
    }

    /**
     * 以cookie方式更新所有主播信息
     */
    private fun updateAllAnchorByCookie() {
        val platforms = PlatformDispatcher.getAllPlatformInstance()
        //进度 liveData
        val processLiveData = processLiveDataByCookie()

        var added = 0
        platforms.forEach { platform ->
            if (++added == platforms.size)
                processLiveData.allAdded()
            //同平台的anchor
            val list = mutableListOf<Anchor>()
            sortedAnchorList.value?.forEach {
                if (it.platform == platform.key)
                    list.add(it)
            }
            if (list.size > 0) {
                processLiveData.insert(platform.value)
                //平台支持该功能
                if (platform.value.supportUpdateAnchorsByCookie()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        kotlin.runCatching {
                            val result = platform.value.updateAnchorsDataByCookie(list)
                            if (result.cookieOk) {
                                notifyAnchorListChange()
                                processLiveData.update(platform.value, ProcessStatus.SUCCESS)
                            } else {
                                processLiveData.update(
                                    platform.value,
                                    ProcessStatus.COOKIE_INVALID
                                )
                            }
                        }.onFailure {
                            Log.d(
                                "updateAllAnchorByCookie",
                                "更新主播信息失败：cause:${it.javaClass.name}------"
                            )
                            val processStatus = when (it) {
                                is java.net.SocketTimeoutException -> ProcessStatus.NET_TIME_OUT
                                is java.net.UnknownHostException -> ProcessStatus.NET_ERROR
                                else -> ProcessStatus.ERROR
                            }
                            processLiveData.update(platform.value, processStatus)
                            it.printStackTrace()
                            hideRefreshBtn()
                        }.onSuccess {
                            hideRefreshBtn()
                        }
                    }
                }
                //不支持该功能，使用常规方式
                else {
                    processLiveData.update(platform.value, ProcessStatus.CAN_NOT_TRACK)
                    list.forEach {
                        updateAnchor(it)
                    }
                }
            }
        }
    }


    /**
     * 用于cookie方式更新信息时显示进度。
     */
    private fun processLiveDataByCookie(): MutableLiveData<UpdateAnchorsByCookieResult> {
        return MutableLiveData<UpdateAnchorsByCookieResult>().also { liveData ->
            val observer = object : Observer<UpdateAnchorsByCookieResult> {
                override fun onChanged(process: UpdateAnchorsByCookieResult) {
                    showSnackBar(process)
//                    showDetails(process)
                }

                private fun showDetails(process: UpdateAnchorsByCookieResult) {
                    val processStringBuilder = StringBuilder()
                    var completeSize = 0
                    var index = 0
                    process.map.forEach { map ->
                        index++
                        if (map.value == ProcessStatus.WAIT || map.value == ProcessStatus.SUCCESS)
                            processStringBuilder.append(
                                " [ ${map.key.platformName}：${map.value.getValue()} ]" +
                                        if (index != process.map.size) "<br/>" else ""
                            )
                        else
                            processStringBuilder.append(
                                " [ ${map.key.platformName}：<span style='color:red'>${map.value.getValue()}</span> ]" +
                                        if (index != process.map.size) "<br/>" else ""
                            )
                        if (map.value != ProcessStatus.WAIT) completeSize++
                    }
                    _liveDataUpdateAnchorResult.update(false, processStringBuilder.toString())
                    if (completeSize == process.map.size && process.isAllAdded) {
                        _liveDataUpdateAnchorResult.update(
                            true,
                            processStringBuilder.toString()
                        )
                        liveData.removeObserver(this)
                    }
                }

                private fun showSnackBar(result: UpdateAnchorsByCookieResult) {
                    var builder: SpannableStringBuilder? = null
                    var completeSize = 0
                    var failedSize = 0
                    var index = 0
                    result.map.forEach { map ->
                        index++
                        if (map.value != ProcessStatus.WAIT) {
                            completeSize++
                            when (map.value) {
                                ProcessStatus.SUCCESS -> {
                                }
                                ProcessStatus.COOKIE_INVALID -> {
                                    if (builder == null)
                                        builder =
                                            SpannableStringBuilder().also { it.append("主页 获取数据失败：") }
                                    failedSize++
                                    val startIndex =
                                        if (builder!!.isNotEmpty()) builder!!.length - 1 else 0
                                    val platformName = "${map.key.platformName}: "
                                    val status = "${map.value.getValue()}"
                                    builder?.append("$platformName$status；")
                                    builder?.setSpan(
                                        LoginClickSpan(map.key),
                                        startIndex + platformName.length,
                                        startIndex + platformName.length + status.length+1,
                                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                    )
                                    //为什么要+1？
                                }
                                else -> {
                                    if (builder == null)
                                        builder =
                                            SpannableStringBuilder().also { it.append("主页 获取数据失败：") }
                                    failedSize++
                                    builder?.append("${map.key.platformName}:${map.value.getValue()}； ")
                                }
                            }

                        }
                    }
                    if (failedSize > 0 && completeSize == result.map.size && result.isAllAdded)
                        _snackBarMsg.postValue(builder)
                }
            }
            liveData.value = UpdateAnchorsByCookieResult(mutableMapOf(), false)
            liveData.observeForever(observer)
        }
    }

    private class LoginClickSpan(val platform: IPlatform) : ClickableSpan() {
        @SuppressLint("ResourceType")
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = Color.parseColor(MyApplication.application.getString(R.color.colorPrimary))
            ds.isUnderlineText = false
        }

        override fun onClick(widget: View) {
            val intent = Intent(MyApplication.application, LoginActivity::class.java).also {
                it.putExtra(
                    "platform",
                    platform.platform
                )
            }
            MyApplication.application.startActivity(intent)
        }
    }

    /**
     * 更新主播信息的进度
     * @property map 平台，更新信息
     */
    private data class UpdateAnchorsByCookieResult(
        var map: MutableMap<IPlatform, ProcessStatus>,
        var isAllAdded: Boolean
    )

    private fun hideRefreshBtn() {
        _liveDataUpdateDetails.postValue(UpdateState.FINISH)
    }
}