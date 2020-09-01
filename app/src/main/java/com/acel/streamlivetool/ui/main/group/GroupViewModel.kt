package com.acel.streamlivetool.ui.main.group

import android.animation.Animator
import android.app.AlertDialog
import android.content.Intent
import android.text.Html
import android.util.Log
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.ui.main.public_class.ProcessStatus
import com.acel.streamlivetool.util.AnchorListUtil
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.PreferenceConstant.groupModeUseCookie
import kotlinx.android.synthetic.main.text_view_process_update_anchors.*
import java.util.*

class GroupViewModel(private val groupFragment: GroupFragment) : ViewModel() {

    class ViewModeFactory(private val groupFragment: GroupFragment) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GroupViewModel(
                groupFragment
            ) as T
        }
    }

    //数据库读取的anchorList
    private val anchorRepository =
        AnchorRepository.getInstance()


    //排序后的anchorList
    val sortedAnchorList = MediatorLiveData<MutableList<Anchor>>().also {
        it.value = Collections.synchronizedList(mutableListOf())
        it.addSource(anchorRepository.anchorList) { sourceList ->
            it.value?.clear()
            it.value?.addAll(sourceList)
            it.postValue(it.value)
            updateAllAnchor()
        }
        it.observe(groupFragment, Observer {
            groupFragment.refreshAnchorAttribute()
        })
    }
    private var updateProcessAnimate: ViewPropertyAnimator? = null
    var lastGetAnchorsTime = 0L
    val processViewAlpha = 0.7f

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
            } finally {
                hideRefreshBtn()
            }
        }
    }

    fun updateAllAnchor() {
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

    private fun MutableLiveData<Process>.update(platform: IPlatform, status: ProcessStatus) {
        value?.apply {
            map[platform] = status
            postValue(value)
        }
    }

    private fun MutableLiveData<Process>.insert(
        platform: IPlatform
    ) {
        value?.apply {
            map[platform] = ProcessStatus.WAIT
            postValue(value)
        }
    }

    private fun MutableLiveData<Process>.allAdded() {
        value?.apply {
            this.isAllAdded = true
        }
    }

    private fun updateAllAnchorByCookie() {
        val platforms = PlatformDispatcher.getAllPlatformInstance()
        //进度 liveData
        val processLiveData =
            MutableLiveData<Process>().also { liveData ->
                liveData.value = Process(mutableMapOf(), false)
                liveData.observe(groupFragment) { process ->
                    val processStringBuilder = StringBuilder().also {
                        it.append("更新数据... ")
                    }
                    var completeSize = 0
                    process.map.forEach { map ->
                        if (map.value == ProcessStatus.WAIT || map.value == ProcessStatus.SUCCESS)
                            processStringBuilder.append("[ ${map.key.platformName}：${map.value.getValue()} ] ")
                        else
                            processStringBuilder.append("[ ${map.key.platformName}：<span style='color:red'>${map.value.getValue()}</span> ] ")

                        if (map.value != ProcessStatus.WAIT) completeSize++
                    }
                    showUpdateProcess(processStringBuilder.toString())
                    if (completeSize == process.map.size && process.isAllAdded) {
                        completeUpdateProcess()
                        liveData.removeObservers(groupFragment)
                    }
                }
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
                //平台支持该功能
                processLiveData.insert(platform.value)
                if (platform.value.supportUpdateAnchorsByCookie()) {
                    MainExecutor.execute {
                        try {
                            val result = platform.value.updateAnchorsDataByCookie(list)
                            if (result.cookieOk) {
                                notifyAnchorListChange()
                                processLiveData.update(platform.value, ProcessStatus.SUCCESS)
                            } else {
                                runOnUiThread {
                                    alertCookieInvalid(platform.value)
                                }
                                processLiveData.update(platform.value, ProcessStatus.COOKIE_INVALID)
                            }
                        } catch (e: Exception) {
                            Log.d(
                                "updateAllAnchorByCookie",
                                "更新主播信息失败：cause:${e.javaClass.name}------"
                            )
                            val processStatus = when (e) {
                                is java.net.SocketTimeoutException -> ProcessStatus.NET_TIME_OUT
                                is java.net.UnknownHostException -> ProcessStatus.NET_TIME_OUT
                                else -> ProcessStatus.NET_TIME_OUT
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

    private data class Process(
        var map: MutableMap<IPlatform, ProcessStatus>,
        var isAllAdded: Boolean
    )

    private fun alertCookieInvalid(platform: IPlatform) {
        val builder = AlertDialog.Builder(groupFragment.requireContext())
        builder.setTitle("${platform.platformName} 的Cookie无效")
        builder.setMessage("是否登录？")
        builder.setPositiveButton("是") { _, _ ->
            val intent = Intent(groupFragment.requireContext(), LoginActivity::class.java).also {
                it.putExtra(
                    "platform",
                    platform.platform
                )
            }
            groupFragment.startActivity(intent)
        }
        builder.show()
    }

    private fun hideRefreshBtn() {
        runOnUiThread {
            groupFragment.hideSwipeRefreshBtn()
        }
    }

    @Suppress("DEPRECATION")
    private fun showUpdateProcess(text: String) {
        updateProcessAnimate?.cancel()
        runOnUiThread {
            groupFragment.textView_process_update_anchors.apply {
                this.text = Html.fromHtml(text)
                visibility = View.VISIBLE
            }
        }
    }

    private fun completeUpdateProcess() {
        groupFragment.textView_process_update_anchors.apply {
            updateProcessAnimate = animate().alpha(0f).setDuration(2000)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(p0: Animator?) {
                        visibility = View.GONE
                        alpha = processViewAlpha
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                        alpha = processViewAlpha
                    }

                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) {}
                }).setStartDelay(3000)
        }
    }

}