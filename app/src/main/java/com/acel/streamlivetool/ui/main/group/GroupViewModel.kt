package com.acel.streamlivetool.ui.main.group

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.lifecycle.*
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.const_value.PreferenceVariable.groupUseCookie
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.manager.AnchorUpdateManager
import com.acel.streamlivetool.manager.UpdateResultReceiver
import com.acel.streamlivetool.manager.UpdateResultReceiver.*
import com.acel.streamlivetool.platform.PlatformDispatcher.platformImpl
import com.acel.streamlivetool.platform.base.AbstractPlatformImpl
import com.acel.streamlivetool.platform.impl.huya.HuyaImpl
import com.acel.streamlivetool.ui.custom.AlertDialogTool
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.util.AnchorListUtil
import com.acel.streamlivetool.util.AnchorUtil.isFollowed
import com.acel.streamlivetool.util.AnchorUtil.update
import com.acel.streamlivetool.util.AppUtil.mainThread
import com.acel.streamlivetool.util.ToastUtil.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class GroupViewModel : ViewModel(), UpdateResultReceiver {

    //数据库读取的主页anchorList
    private val anchorRepository =
        AnchorRepository.getInstance()

    private val anchorListManager = AnchorUpdateManager.instance

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
            it.value?.apply {
                AnchorListUtil.appointAdditionalActions(this)
            }
            it.postValue(it.value)
            notifyAnchorListChange()
            updateAllAnchor()
        }
    }

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
        AnchorListUtil.insertSection(sortedAnchorList.value!!)
        sortedAnchorList.postValue(sortedAnchorList.value)
    }


    /**
     * 更新全部anchor
     */
    @Synchronized
    fun updateAllAnchor() {
        mainThread {
            _liveDataUpdateStatus.value = UpdateStatus.UPDATING
        }
        if (groupUseCookie.value!!) {
            //使用cookie方式
            sortedAnchorList.value?.let {
                anchorListManager.updateAllAnchorByCookie(this, it, viewModelScope)
            }
        } else
        //使用直接更新
            sortedAnchorList.value?.let {
                anchorListManager.updateAnchors(this, it, viewModelScope)
            }
        lastUpdateTime = System.currentTimeMillis()
    }


    /**
     * 删除anchor
     */
    fun deleteAnchor(anchor: Anchor) {
        anchorRepository.deleteAnchor(anchor)
    }

    override fun onCleared() {
        super.onCleared()
        updateAnchorsTask?.cancel()
    }

    /**
     * 关注
     */
    fun followAnchor(context: Context, anchor: Anchor, actionOnEnd: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val platformImpl = anchor.platformImpl()
            val result = platformImpl?.anchorCookieModule?.follow(anchor)
            result?.let {
                withContext(Dispatchers.Main) {
                    if (it.success) {
                        toast("关注成功：${anchor.nickname}")
                        sortedAnchorList.value?.let { it1 ->
                            anchorListManager.updateAllAnchorByCookie(
                                this@GroupViewModel,
                                it1,
                                viewModelScope
                            )
                        }
                    } else {
                        when (platformImpl.platform) {
                            "huya" -> {
                                if (it.code == -10003) {
                                    it.msg?.let { it1 -> toast(it1) }
                                    it.data?.let { it1 ->
                                        (platformImpl as HuyaImpl).anchorCookieModule
                                            .showVerifyCodeWindow(context, it1) {
                                                followAnchor(context, anchor, actionOnEnd)
                                            }
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

    private val checkFollowedAnchors = mutableSetOf<Anchor>()

    fun waitToCheckFollowed(anchor: Anchor) {
        checkFollowedAnchors.add(anchor)
    }

    /**
     * 普通方式更新结束
     */
    override fun onUpdateFinish(resultList: List<ResultSingleAnchor>) {
        updateFinish()
        notifyAnchorListChange()
        showUpdateResult(resultList)
    }

    /**
     * cookie方式更新结束
     */
    override fun onCookieModeUpdateFinish(resultList: List<ResultCookieMode>) {
        updateFinish()
        notifyAnchorListChange()
        showUpdateResultCookieMode(resultList)
        //检查刚添加的anchor是否关注
        checkFollowed(resultList)
    }

    /**
     * 结束更新
     */
    private fun updateFinish() {
        _liveDataUpdateStatus.postValue(UpdateStatus.FINISH)
    }

    /**
     * 显示普通方式更新结果
     */
    private fun showUpdateResult(list: List<ResultSingleAnchor>) {
        val builder = SpannableStringBuilder()
        var failed = 0
        list.forEach {
            if (!it.success)
                failed++
        }
        if (failed > 0) {
            builder.append("更新失败($failed/${list.size})")
            builder.setSpan(
                ErrorColorSpan(),
                0,
                builder.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            _updateErrorMsg.postValue(builder)
        } else {
            _updateSuccess.postValue(true)
        }
    }

    /**
     * 显示更新结果 cookie mode
     */
    private fun showUpdateResultCookieMode(list: List<ResultCookieMode>) {
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
     *  snack bar 登录提示
     */
    private class LoginClickSpan(val platform: AbstractPlatformImpl) : ClickableSpan() {
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
     * 检查是否需要关注
     */
    private fun checkFollowed(resultList: List<ResultCookieMode>) {
        checkFollowedAnchors.forEach check@{ anchor ->
            resultList.forEach result@{
                if (it.iPlatform == anchor.platformImpl() && it.isSuccess) {
                    val checkIndex = sortedAnchorList.value?.indexOf(anchor)
                    checkIndex?.apply {
                        if (this != -1) {
                            val checkedAnchor = sortedAnchorList.value?.get(this)
                            checkedAnchor?.apply {
                                if (!isFollowed())
                                    mainThread {
                                        showCheckedFollowDialog.value = anchor
                                    }
//                                checkFollowedAnchors.remove(anchor)
                                return@check
                            }
                        }
                    }
                } else
                    return@result
            }
        }
    }

    /**
     * 显示关注提示框
     */
    fun showFollowDialog(context: Context, anchor: Anchor) {
        val builder = AlertDialogTool.newAlertDialog(context)
        anchor.platformImpl()?.let {
            if (it.anchorCookieModule != null && it.anchorCookieModule!!.supportFollow) {
                builder.apply {
                    setMessage("您还未关注${anchor.nickname}，是否关注？")
                    setPositiveButton("是") { _, _ ->
                        followAnchor(context, anchor) {
                            checkFollowedAnchors.remove(anchor)
                        }
                    }
                    setNegativeButton("否") { _, _ ->
                        checkFollowedAnchors.remove(anchor)
                    }
                    setOnCancelListener {
                        checkFollowedAnchors.remove(anchor)
                    }
                }
            } else {
                builder.apply {
                    setMessage("您还未关注${anchor.nickname}，${it.platformName}暂不支持直接关注，是否打开${it.platformName}app关注？")
                    setPositiveButton("是") { _, _ ->
                        viewModelScope.launch(Dispatchers.IO) {
                            it.appModule?.startApp(MyApplication.application, anchor)
                            checkFollowedAnchors.remove(anchor)
                        }
                    }
                    setNegativeButton("否") { _, _ ->
                        checkFollowedAnchors.remove(anchor)
                    }
                    setOnCancelListener {
                        checkFollowedAnchors.remove(anchor)
                    }
                }
            }
        }
        mainThread {
            builder.show()
        }
    }
}

