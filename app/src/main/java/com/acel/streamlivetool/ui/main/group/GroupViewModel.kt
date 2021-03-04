package com.acel.streamlivetool.ui.main.group

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.lifecycle.*
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.const_value.ConstValue.FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.platform.PlatformDispatcher.platformImpl
import com.acel.streamlivetool.platform.huya.HuyaImpl
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.ui.main.AnchorListManager
import com.acel.streamlivetool.util.AnchorListUtil
import com.acel.streamlivetool.util.AppUtil.mainThread
import com.acel.streamlivetool.util.PreferenceConstant.groupModeUseCookie
import com.acel.streamlivetool.util.ToastUtil.toast
import com.bumptech.glide.util.Util.assertMainThread
import kotlinx.coroutines.*
import java.util.*

class GroupViewModel : ViewModel() {


    //数据库读取的主页anchorList
    private val anchorRepository =
        AnchorRepository.getInstance()

    private val anchorListManager = AnchorListManager.instance
    private val scope = CoroutineScope(Dispatchers.Default)

    //排序后的anchorList
    val sortedAnchorList = MediatorLiveData<MutableList<Anchor>>().also {
        it.value = Collections.synchronizedList(mutableListOf())
        it.addSource(anchorRepository.anchorList) { sourceList ->
            val oldList = it.value?.toMutableList()
            it.value?.clear()
            it.value?.addAll(sourceList)
            //如果旧列表有数据，将数据更新到新列表
            if (oldList != null && oldList.isNotEmpty())
                it.value?.forEach { newAnchor ->
                    val index = oldList.indexOf(newAnchor)
                    if (index != -1) {
                        newAnchor.update(oldList[index])
                    }
                }
            it.postValue(it.value)
            notifyAnchorListChange()
            updateAllAnchor()
        }
    }

    //更新进度
    private val _liveDataUpdateStatus = MutableLiveData<UpdateStatus>().also {
        it.value = UpdateStatus.PREPARE
    }
    val liveDataUpdateStatus: LiveData<UpdateStatus>
        get() = _liveDataUpdateStatus

    enum class UpdateStatus {
        PREPARE, UPDATING, FINISH
    }

    //snackBar通知live data
    private val _updateErrorMsg = MutableLiveData<SpannableStringBuilder>()
    val updateErrorMsg
        get() = _updateErrorMsg

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess
        get() = _updateSuccess
    var lastUpdateTime = 0L
    private var updateAnchorsTask: Job? = null

    val showCheckedFollowDialog = MutableLiveData<Anchor?>()

    @Synchronized
    private fun notifyAnchorListChange() {
        AnchorListUtil.sortAnchorListByStatus(sortedAnchorList.value!!)
        AnchorListUtil.insertStatusPlaceHolder(sortedAnchorList.value!!)
        sortedAnchorList.postValue(sortedAnchorList.value)
    }

    /**
     * 单个anchor更新，不需要cookie
     */
    private fun updateAnchor(anchor: Anchor) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
                platformImpl?.let {
                    val result = platformImpl.updateAnchorData(anchor)
                    if (result)
                        notifyAnchorListChange()
                }
                updateFinish()
            }.onFailure {
                Log.d("updateAnchor", "更新主播信息失败：cause:${it.javaClass.name}------$anchor")
                it.printStackTrace()
                updateFinish()
            }
        }
    }

    /**
     * 更新全部anchor
     */
    @Synchronized
    fun updateAllAnchor() {
        assertMainThread()
        _liveDataUpdateStatus.value = UpdateStatus.UPDATING
        if (groupModeUseCookie)
            updateAllAnchorByCookie()
        else
            sortedAnchorList.value?.forEach { anchor ->
                updateAnchor(anchor)
            }
        lastUpdateTime = System.currentTimeMillis()
    }


    /**
     * 删除anchor
     */
    fun deleteAnchor(anchor: Anchor) {
        anchorRepository.deleteAnchor(anchor)
    }

    /**
     * 以cookie方式更新所有主播信息
     */
    private fun updateAllAnchorByCookie() {
        updateAnchorsTask?.cancel()
        val platforms = PlatformDispatcher.getAllPlatformInstance()
        updateAnchorsTask = scope.launch(Dispatchers.IO) {
            val updateTaskList = mutableListOf<Deferred<UpdateResult>>()
            platforms.forEach { platformEntry ->
                //同平台的anchor列表
                val samePlatformAnchorList = mutableListOf<Anchor>()
                sortedAnchorList.value?.forEach {
                    if (it.platform == platformEntry.key)
                        samePlatformAnchorList.add(it)
                }
                if (samePlatformAnchorList.size > 0) {
                    if (platformEntry.value.supportUpdateAnchorsByCookie()) {
                        //支持cookie方式
                        val task = async {
                            updatePlatformAnchorList(platformEntry.value, samePlatformAnchorList)
                        }
                        updateTaskList.add(task)
                    } else {
                        //不支持cookie方式，使用逐条更新
                        samePlatformAnchorList.forEach {
                            updateAnchor(it)
                        }
                    }
                }
            }

            val resultList = mutableListOf<UpdateResult>()
            updateTaskList.forEach {
                resultList.add(it.await())
            }
            showUpdateResult(resultList)
            updateFinish()

            //检查刚添加的anchor是否关注
            checkFollowed(resultList)
        }
        updateAnchorsTask?.start()
    }

    private fun checkFollowed(resultList: MutableList<UpdateResult>) {
        checkFollowedAnchors.forEach check@{ anchor ->
            resultList.forEach result@{
                if (it.iPlatform == anchor.platformImpl() && it.isSuccess) {
                    val checkIndex = sortedAnchorList.value?.indexOf(anchor)
                    checkIndex?.apply {
                        if (this != -1) {
                            val checkedAnchor = sortedAnchorList.value?.get(this)
                            checkedAnchor?.apply {
                                if (!addToCheckedFollowed()) {
                                    mainThread {
                                        showCheckedFollowDialog.value = anchor
                                    }
                                    checkFollowedAnchors.remove(anchor)
                                    return@check
                                }
                            }
                        }
                    }
                } else
                    return@result
            }
        }
    }

    fun showFollowDialog(context: Context, anchor: Anchor) {
        val builder = AlertDialog.Builder(context)
        anchor.platformImpl()?.let {
            if (it.supportFollow) {
                builder.apply {
                    setMessage("您还未关注${anchor.nickname}，是否关注？")
                    setPositiveButton("是") { dialog: DialogInterface, _: Int ->
                        followAnchor(context, anchor) {
                            dialog.dismiss()
                        }
                    }
                    setNegativeButton("否") { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                    }
                }
            } else {
                builder.apply {
                    setMessage("您还未关注${anchor.nickname}，${it.platformName}暂不支持直接关注，是否打开${it.platformName}app关注？")
                    setPositiveButton("是") { dialog: DialogInterface, _: Int ->
                        viewModelScope.launch(Dispatchers.IO) {
                            it.startApp(MyApplication.application, anchor)
                            dialog.dismiss()
                        }
                    }
                    setNegativeButton("否") { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                    }
                }
            }
        }
        mainThread {
            builder.show()
        }
    }

    /**
     * 显示更新结果
     */
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
            _updateErrorMsg.postValue(builder)
        else {
            _updateSuccess.postValue(true)
        }
    }

    /**
     * 更新主页中某平台的anchor list
     * @param iPlatform 平台impl
     */
    private fun updatePlatformAnchorList(
        iPlatform: IPlatform,
        anchorList: MutableList<Anchor>
    ): UpdateResult {
        var updateResult =
            UpdateResult(isSuccess = false, resultType = ResultType.ERROR, iPlatform = iPlatform)
        runCatching {
            //更新平台anchor list
            val result = anchorListManager.updateAnchorList(iPlatform)
            result?.apply {
                updateResult = if (success && isCookieValid) {
                    val targetList = anchorListManager.getAnchorList(iPlatform)
                    anchorList.forEach {
                        val index = targetList.indexOf(it)
                        if (index == -1)
                            it.setNotFollowedHint()
                        else {
                            //更新信息
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
            updateFinish()
        }
        return updateResult
    }

    /**
     * 更新结果
     */
    data class UpdateResult(
        val isSuccess: Boolean,
        val resultType: ResultType,
        val iPlatform: IPlatform
    )

    /**
     * 更新结果的类型
     */
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
     *  snack bar 登录提示
     */
    private class LoginClickSpan(val platform: IPlatform) : ClickableSpan() {
        @SuppressLint("ResourceType")
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = Color.parseColor(MyApplication.application.getString(R.color.colorPrimary))
            ds.isUnderlineText = false
        }

        override fun onClick(widget: View) {
            val intent = Intent(MyApplication.application, LoginActivity::class.java)
                .also { it.putExtra("platform", platform.platform) }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            MyApplication.application.startActivity(intent)
        }
    }

    /**
     * snack bar 错误提示
     */
    private class ErrorColorSpan : ClickableSpan() {
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = Color.RED
            ds.isUnderlineText = false
        }

        override fun onClick(widget: View) {}
    }

    /**
     * 结束更新
     */
    private fun updateFinish() {
        _liveDataUpdateStatus.postValue(UpdateStatus.FINISH)
    }

    /**
     * 关注列表中不包含改主播时修改
     */
    private fun Anchor.setNotFollowedHint() {
        title = FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR
        status = false
    }

    private fun Anchor.addToCheckedFollowed() = title != FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR

    /**
     * 更新主播数据
     */
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
        liveTime = newAnchor.liveTime
    }

    override fun onCleared() {
        super.onCleared()
        updateAnchorsTask?.cancel()
    }

    fun followAnchor(context: Context, anchor: Anchor, actionOnEnd: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val platformImpl = anchor.platformImpl()
            val result = platformImpl?.follow(anchor)
            result?.let {
                withContext(Dispatchers.Main) {
                    if (it.success) {
                        toast("关注成功：${anchor.nickname}")
                        updateAllAnchorByCookie()
                    } else {
                        when (platformImpl.platform) {
                            "huya" -> {
                                if (it.code == -10003) {
                                    toast(it.msg)
                                    (platformImpl as HuyaImpl)
                                        .showVerifyCodeWindow(context, it.data) {
                                            followAnchor(context, anchor, actionOnEnd)
                                        }
                                    return@withContext
                                } else {
                                    toast("关注失败：${it.msg}，如多次失败请自行关注。")
                                }
                            }
                            else ->
                                toast("关注失败：${it.msg}，如多次失败请自行关注。")
                        }
                    }
                    actionOnEnd.invoke()
                }
            }
        }
    }

    private fun showVerifyCodeWindow() {

    }

    private val checkFollowedAnchors = mutableListOf<Anchor>()

    fun addToCheckedFollowed(anchor: Anchor) {
        checkFollowedAnchors.add(anchor)
    }
}

