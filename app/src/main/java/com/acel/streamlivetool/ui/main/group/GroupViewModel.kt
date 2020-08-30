package com.acel.streamlivetool.ui.main.group

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.util.AnchorListUtil
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.PreferenceConstant.groupModeUseCookie
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
    }

    fun deleteAnchor(anchor: Anchor) {
        anchorRepository.deleteAnchor(anchor)
    }

    private fun updateAllAnchorByCookie() {
        val platforms = PlatformDispatcher.getAllPlatformInstance()
        platforms.forEach { platform ->
            val list = mutableListOf<Anchor>()
            sortedAnchorList.value?.forEach {
                if (it.platform == platform.key)
                    list.add(it)
            }
            if (list.size > 0) {
                //平台支持该功能
                if (platform.value.supportUpdateAnchorsByCookie()) {
                    MainExecutor.execute {
                        try {
                            val result = platform.value.updateAnchorsDataByCookie(list)
                            if (result.cookieOk) {
                                notifyAnchorListChange()
                            } else
                                runOnUiThread {
                                    alertCookieInvalid(platform.value)
                                }
                        } catch (e: Exception) {
                            Log.d(
                                "updateAllAnchorByCookie",
                                "更新主播信息失败：cause:${e.javaClass.name}------"
                            )
                            e.printStackTrace()
                        } finally {
                            hideRefreshBtn()
                        }
                    }
                }
                //不支持该功能，使用常规方式
                else {
                    list.forEach {
                        updateAnchor(it)
                    }
                }
            }
        }
    }

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
}