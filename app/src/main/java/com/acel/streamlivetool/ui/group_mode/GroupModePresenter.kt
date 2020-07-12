package com.acel.streamlivetool.ui.group_mode

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.acel.streamlivetool.MainAnchorHelper
import com.acel.streamlivetool.MainExecutor
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.ToastUtil.toast

class GroupModePresenter(private var view: GroupModeConstract.View?) : GroupModeConstract.Presenter {
    val context = view as Context
    val anchorAttributeMap = MutableLiveData<MutableMap<String, AnchorAttribute>>().also {
        it.value = mutableMapOf()
    }

    fun setAnchorAttribute(anchorAttribute: AnchorAttribute) {
        anchorAttributeMap.value?.set(anchorAttribute.getAnchorKey(), anchorAttribute)
        anchorAttributeMap.postValue(anchorAttributeMap.value)
    }

    val anchorRepository = AnchorRepository.getInstance(context.applicationContext)
    private var firstTimeLoadAnchorList = true
    internal val sortedAnchorList = mutableListOf<Anchor>()
    private fun sortAnchorList() {
        val list = MainAnchorHelper.sortAnchorListByStatus(
            anchorRepository.anchorList,
            anchorAttributeMap
        )
        sortedAnchorList.clear()
        sortedAnchorList.addAll(list)
    }

    override fun addAnchor(queryAnchor: Anchor) {
        MainExecutor.execute(AddAnchorRunnable(queryAnchor))
    }

    inner class AddAnchorRunnable(val queryAnchor: Anchor) : Runnable {
        override fun run() {
            val platformImpl = PlatformDispatcher.getPlatformImpl(queryAnchor.platform)
            try {
                val anchor = platformImpl?.getAnchor(queryAnchor)
                if (anchor != null) {
                    if (anchorRepository.anchorList.value!!.indexOf(anchor) == -1) {
                        insertAnchor(anchor)
                    } else {
                        view?.addAnchorFailed("该直播间已存在——${anchor.nickname}")
                    }
                } else {
                    view?.addAnchorFailed("该直播间找寻不到")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    toast("发生错误。")
                }
            }
        }

        private fun insertAnchor(anchor: Anchor) {
            anchorRepository.insertAnchor(anchor)
            view?.addAnchorSuccess(anchor)
            //添加后获取状态
            getAnchorsStatus(anchor)
        }
    }

    init {
        anchorRepository.anchorList.observe(context as GroupModeActivity, Observer {
            if (firstTimeLoadAnchorList) {
                getAllAnchorsStatus()
                firstTimeLoadAnchorList = false
            }
            sortAnchorList()
            view?.refreshAnchorList()
        })
        anchorAttributeMap.observe(context, Observer {
            sortAnchorList()
            view?.refreshAnchorStatus()
        })
    }

    override fun getAnchorsStatus(anchor: Anchor) {
        MainExecutor.execute(GetStatusRunnable(anchor))
    }

    inner class GetStatusRunnable(val anchor: Anchor) : Runnable {
        override fun run() {
            try {
                val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
                val anchorAttribute = platformImpl?.getAnchorAttribute(anchor)
                if (anchorAttribute != null) {
                    setAnchorAttribute(anchorAttribute)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getAllAnchorsStatus() {
        anchorRepository.anchorList.value?.forEach { anchor ->
            getAnchorsStatus(anchor)
        }
    }

    override fun onDestroy() {
        view = null
    }

}
