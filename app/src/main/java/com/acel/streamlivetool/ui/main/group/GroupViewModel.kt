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
import com.acel.streamlivetool.ui.main.AnchorListManager
import com.acel.streamlivetool.util.AnchorListUtil
import com.acel.streamlivetool.util.PreferenceConstant.groupModeUseCookie
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.log

class GroupViewModel : ViewModel() {
    companion object {
        private const val FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR = "关注列表中没有这个主播，请关注该主播或关闭cookie方式"
    }

    class ViewModeFactory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GroupViewModel() as T
        }
    }

    //数据库读取的主页anchorList
    private val anchorRepository =
        AnchorRepository.getInstance()

    private val anchorListManager = AnchorListManager.instance

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
    var nowUpdateTask: Job? = null

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
                it.printStackTrace()
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
        status: ResultType
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
            map[platform] = ResultType.WAIT
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
        nowUpdateTask?.let {
            Log.d("updateAllAnchorByCookie", "isActive:${it.isActive}")
            it.cancel()
        }
        Log.d("updateAllAnchorByCookie", "update cookie")
        val platforms = PlatformDispatcher.getAllPlatformInstance()
        Log.d("updateAllAnchorByCookie", "0")
        nowUpdateTask = viewModelScope.launch{
            Log.d("updateAllAnchorByCookie", "0.5")
            val updateTaskList = mutableListOf<Deferred<UpdateResult>>()
            platforms.forEach { platformEntry ->
                //同平台的anchor列表
                val samePlatformAnchorList = mutableListOf<Anchor>()
                Log.d("updateAllAnchorByCookie", "1")
                sortedAnchorList.value?.forEach {
                    if (it.platform == platformEntry.key)
                        samePlatformAnchorList.add(it)
                }
                Log.d("updateAllAnchorByCookie", "2")
                if (samePlatformAnchorList.size > 0) {
                    if (platformEntry.value.supportUpdateAnchorsByCookie()) {
                        //支持cookie方式
                        val task = async(Dispatchers.IO)  {
                            updatePlatformAnchorList(platformEntry.value, samePlatformAnchorList)
                        }
                        updateTaskList.add(task)
                    } else {
                        // TODO: 2020/10/28 删除
                        //不支持cookie方式，使用逐条更新
                        samePlatformAnchorList.forEach {
                            updateAnchor(it)
                        }
                    }
                }
            }
            hideRefreshBtn()
            val resultList = mutableListOf<UpdateResult>()
            updateTaskList.forEach {
                resultList.add(it.await())
            }
            showUpdateResult(resultList)
        }
        //todo --------------
        return
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
                //平台支持cookie方式
                if (platform.value.supportUpdateAnchorsByCookie()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        kotlin.runCatching {
                            val result = platform.value.updateAnchorsDataByCookie(list)
                            if (result.cookieOk) {
                                notifyAnchorListChange()
                                processLiveData.update(platform.value, ResultType.SUCCESS)
                            } else {
                                processLiveData.update(
                                    platform.value,
                                    ResultType.COOKIE_INVALID
                                )
                            }
                        }.onFailure {
                            Log.d(
                                "updateAllAnchorByCookie",
                                "更新主播信息失败：cause:${it.javaClass.name}------"
                            )
                            val processStatus = when (it) {
                                is java.net.SocketTimeoutException -> ResultType.NET_TIME_OUT
                                is java.net.UnknownHostException -> ResultType.NET_ERROR
                                else -> ResultType.ERROR
                            }
                            processLiveData.update(platform.value, processStatus)
                            it.printStackTrace()
                            hideRefreshBtn()
                        }.onSuccess {
                            hideRefreshBtn()
                        }
                    }
                }
                //不支持cookie方式，使用逐条更新
                else {
                    processLiveData.update(platform.value, ResultType.CAN_NOT_TRACK)
                    list.forEach {
                        updateAnchor(it)
                    }
                }
            }
        }
    }

    private fun showUpdateResult(list: MutableList<UpdateResult>) {
        var builder: SpannableStringBuilder? = null
        var failed = 0
        list.forEach { result ->
            if (!result.isSuccess) {
                if (builder == null)
                    builder =
                        SpannableStringBuilder().also { it.append("主页 获取数据失败：") }
                failed++
                val startIndex =
                    if (builder!!.isNotEmpty()) builder!!.length - 1 else 0
                val platformName = "${result.iPlatform.platformName}: "
                val status = "${result.resultType.getValue()}"
                builder?.append("$platformName$status；")
                when (result.resultType) {
                    ResultType.COOKIE_INVALID -> {
                        builder?.setSpan(
                            LoginClickSpan(result.iPlatform),
                            startIndex + platformName.length,
                            startIndex + platformName.length + status.length + 1,
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                    else -> {
                        builder?.setSpan(
                            ErrorColorSpan(),
                            startIndex + platformName.length,
                            startIndex + platformName.length + status.length + 1,
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                    //为什么要+1？？？
                }
            }
        }
        if (failed > 0)
            _snackBarMsg.postValue(builder)
    }

    /**
     * 更新主页中某平台的anchor list
     */
    private fun updatePlatformAnchorList(
        iPlatform: IPlatform,
        anchorList: MutableList<Anchor>
    ): UpdateResult {
        Log.d("updatePlatformAnchor", "${iPlatform.platformName}更新中")
        var updateResult =
            UpdateResult(isSuccess = false, resultType = ResultType.ERROR, iPlatform = iPlatform)
        runCatching {
            //更新平台anchor list
            val result = anchorListManager.updateAnchorList(iPlatform)
            result?.apply {
                updateResult = if (isCookieOk) {
                    val targetList = anchorListManager.getAnchorList(iPlatform)
                    anchorList.forEach {
                        val index = targetList.indexOf(it)
                        if (index == -1)
                            it.setNonExistentHint()
                        else {
                            it.update(targetList[index])
                        }
                    }
                    notifyAnchorListChange()
                    UpdateResult(
                        isSuccess = true,
                        resultType = ResultType.SUCCESS,
                        iPlatform = iPlatform
                    )
                } else {
                    UpdateResult(
                        isSuccess = false,
                        resultType = ResultType.COOKIE_INVALID,
                        iPlatform = iPlatform
                    )
                }
            }
        }.onFailure {
            Log.d(
                "updateAllAnchorByCookie",
                "更新主播信息失败：cause:${it.javaClass.name}------"
            )
            val resultType = when (it) {
                is java.net.SocketTimeoutException -> ResultType.NET_TIME_OUT
                is java.net.UnknownHostException -> ResultType.NET_ERROR
                is java.net.ConnectException -> ResultType.NET_ERROR
                else -> ResultType.ERROR
            }
            updateResult = UpdateResult(
                isSuccess = false,
                resultType = resultType,
                iPlatform = iPlatform
            )
            it.printStackTrace()
            hideRefreshBtn()
        }
        return updateResult
    }

    data class UpdateResult(
        val isSuccess: Boolean,
        val resultType: ResultType,
        val iPlatform: IPlatform
    )

    enum class ResultType {
        WAIT, SUCCESS, FAILED, COOKIE_INVALID, CAN_NOT_TRACK, ERROR, NET_TIME_OUT, NET_ERROR;

        fun getValue(): String? {
            return when (this) {
                WAIT -> "等待"
                SUCCESS -> "完成"
                FAILED -> "失败"
                ERROR -> "发生错误"
                COOKIE_INVALID -> "未登录"
                NET_TIME_OUT -> "超时"
                CAN_NOT_TRACK -> "无法追踪"
                NET_ERROR -> "网络错误"
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
                        if (map.value == ResultType.WAIT || map.value == ResultType.SUCCESS)
                            processStringBuilder.append(
                                " [ ${map.key.platformName}：${map.value.getValue()} ]" +
                                        if (index != process.map.size) "<br/>" else ""
                            )
                        else
                            processStringBuilder.append(
                                " [ ${map.key.platformName}：<span style='color:red'>${map.value.getValue()}</span> ]" +
                                        if (index != process.map.size) "<br/>" else ""
                            )
                        if (map.value != ResultType.WAIT) completeSize++
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
                        if (map.value != ResultType.WAIT) {
                            completeSize++
                            when (map.value) {
                                ResultType.SUCCESS -> {
                                }
                                else -> {
                                    if (builder == null)
                                        builder =
                                            SpannableStringBuilder().also { it.append("主页 获取数据失败：") }
                                    failedSize++
                                    val startIndex =
                                        if (builder!!.isNotEmpty()) builder!!.length - 1 else 0
                                    val platformName = "${map.key.platformName}: "
                                    val status = "${map.value.getValue()}"
                                    builder?.append("$platformName$status；")
                                    when (map.value) {
                                        ResultType.COOKIE_INVALID -> {
                                            builder?.setSpan(
                                                LoginClickSpan(map.key),
                                                startIndex + platformName.length,
                                                startIndex + platformName.length + status.length + 1,
                                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                            )
                                        }
                                        else -> {
                                            builder?.setSpan(
                                                ErrorColorSpan(),
                                                startIndex + platformName.length,
                                                startIndex + platformName.length + status.length + 1,
                                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                            )
                                        }
                                        //为什么要+1？？？
                                    }
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
            Log.d("onClick", "click")
            val intent = Intent(MyApplication.application, LoginActivity::class.java)
                .also { it.putExtra("platform", platform.platform) }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            MyApplication.application.startActivity(intent)
        }
    }

    private class ErrorColorSpan : ClickableSpan() {
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = Color.RED
            ds.isUnderlineText = false
        }

        override fun onClick(widget: View) {}
    }

    /**
     * 更新主播信息的进度
     * @property map 平台，更新信息
     */
    private data class UpdateAnchorsByCookieResult(
        var map: MutableMap<IPlatform, ResultType>,
        var isAllAdded: Boolean
    )

    private fun hideRefreshBtn() {
        _liveDataUpdateDetails.postValue(UpdateState.FINISH)
    }

    private fun Anchor.setNonExistentHint() {
        title = FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR
    }

    private fun Anchor.update(newAnchor: Anchor) {
        title = newAnchor.title
        otherParams = newAnchor.otherParams
        status = newAnchor.status
        title = newAnchor.title
        avatar = newAnchor.avatar
        keyFrame = newAnchor.keyFrame
        secondaryStatus = newAnchor.secondaryStatus
        typeName = newAnchor.typeName
        online = newAnchor.online
    }

}

