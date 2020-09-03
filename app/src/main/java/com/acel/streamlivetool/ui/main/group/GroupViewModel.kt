package com.acel.streamlivetool.ui.main.group

import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.main.public_class.ProcessStatus
import com.acel.streamlivetool.util.AnchorListUtil
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.PreferenceConstant.groupModeUseCookie
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

    private val _liveDataUpdateState = MutableLiveData<UpdateState>().also {
        it.value = UpdateState.PREPARE
    }
    val liveDataUpdateState: LiveData<UpdateState>
        get() = _liveDataUpdateState

    enum class UpdateState {
        PREPARE, UPDATING, FINISH
    }

    private val _liveDataUpdateAnchorResult =
        MutableLiveData<UpdateAnchorResult>().also { it.value = UpdateAnchorResult(false, null) }
    val liveDataUpdateAnchorResult: LiveData<UpdateAnchorResult>
        get() = _liveDataUpdateAnchorResult

    data class UpdateAnchorResult(var complete: Boolean, var result: String?)

    private fun MutableLiveData<UpdateAnchorResult>.update(complete: Boolean, result: String) {
        value?.complete = complete
        value?.result = result
        this.postValue(value)
    }

    private val _liveDataCookieInvalid = MutableLiveData<String?>().also { it.value = null }
    val liveDataCookieInvalid: LiveData<String?>
        get() = _liveDataCookieInvalid
//    live data end

    var lastGetAnchorsTime = 0L

    @Synchronized
    private fun notifyAnchorListChange() {
        AnchorListUtil.sortAnchorListByStatus(sortedAnchorList.value!!)
        AnchorListUtil.insertStatusPlaceHolder(sortedAnchorList.value!!)
        sortedAnchorList.postValue(sortedAnchorList.value)
    }

    private fun updateAnchor(anchor: Anchor) {
        MainExecutor.execute {
            try {
                val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
                platformImpl?.let {
                    val result = platformImpl.updateAnchorData(anchor)
                    if (result)
                        notifyAnchorListChange()
                }
            } catch (e: Exception) {
                Log.d("updateAnchor", "更新主播信息失败：cause:${e.javaClass.name}------$anchor")
                Log.d("updateAllAnchorByCookie", "${e.printStackTrace()}")
            } finally {
                hideRefreshBtn()
            }
        }
    }

    fun updateAllAnchor() {
        _liveDataUpdateState.postValue(UpdateState.UPDATING)
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

    private fun MutableLiveData<UpdateProcessByCookie>.update(
        platform: IPlatform,
        status: ProcessStatus
    ) {
        value?.apply {
            map[platform] = status
            postValue(value)
        }
    }

    private fun MutableLiveData<UpdateProcessByCookie>.insert(
        platform: IPlatform
    ) {
        value?.apply {
            map[platform] = ProcessStatus.WAIT
            postValue(value)
        }
    }

    private fun MutableLiveData<UpdateProcessByCookie>.allAdded() {
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
        val processLiveData =
            MutableLiveData<UpdateProcessByCookie>().also { liveData ->
                val observer = object : Observer<UpdateProcessByCookie> {
                    override fun onChanged(process: UpdateProcessByCookie) {
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
                }
                liveData.value = UpdateProcessByCookie(mutableMapOf(), false)
                liveData.observeForever(observer)
            }

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
                    MainExecutor.execute {
                        try {
                            val result = platform.value.updateAnchorsDataByCookie(list)
                            if (result.cookieOk) {
                                notifyAnchorListChange()
                                processLiveData.update(platform.value, ProcessStatus.SUCCESS)
                            } else {
                                _liveDataCookieInvalid.postValue(platform.value.platform)
                                processLiveData.update(platform.value, ProcessStatus.COOKIE_INVALID)
                            }
                        } catch (e: Exception) {
                            Log.d(
                                "updateAllAnchorByCookie",
                                "更新主播信息失败：cause:${e.javaClass.name}------"
                            )
                            Log.d("updateAllAnchorByCookie", "${e.printStackTrace()}")
                            val processStatus = when (e) {
                                is java.net.SocketTimeoutException -> ProcessStatus.NET_TIME_OUT
                                is java.net.UnknownHostException -> ProcessStatus.NET_ERROR
                                else -> ProcessStatus.ERROR
                            }
                            processLiveData.update(platform.value, processStatus)
                            e.printStackTrace()
                        } finally {
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
     * 更新主播信息的进度
     */
    private data class UpdateProcessByCookie(
        var map: MutableMap<IPlatform, ProcessStatus>,
        var isAllAdded: Boolean
    )

    private fun hideRefreshBtn() {
        _liveDataUpdateState.postValue(UpdateState.FINISH)
    }
}