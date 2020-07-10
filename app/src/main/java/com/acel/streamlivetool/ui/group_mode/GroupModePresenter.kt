package com.acel.streamlivetool.ui.group_mode

import android.content.Context
import com.acel.streamlivetool.MainAnchorHelper
import com.acel.streamlivetool.MainAnchorHelper.anchorList
import com.acel.streamlivetool.MainAnchorHelper.initAnchorList
import com.acel.streamlivetool.MainAnchorHelper.loadAnchorList
import com.acel.streamlivetool.MainAnchorHelper.sortAnchorListByStatus
import com.acel.streamlivetool.MainExecutor
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.platform.PlatformDispatcher
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast


class GroupModePresenter(private var view: GroupModeConstract.View?) : GroupModeConstract.Presenter,
    AnkoLogger {
    val context = view as Context
    val anchorAttributeMap: MutableMap<String, AnchorAttribute> = mutableMapOf()

    override fun addAnchor(queryAnchor: Anchor) {
        MainExecutor.execute(AddAnchorRunnable(queryAnchor))
    }

    inner class AddAnchorRunnable(val queryAnchor: Anchor) : Runnable {
        override fun run() {
            val platformImpl = PlatformDispatcher.getPlatformImpl(queryAnchor.platform)
            try {
                val anchor = platformImpl?.getAnchor(queryAnchor)
                if (anchor != null) {
                    if (anchorList.value!!.indexOf(anchor) == -1) {
                        insertAnchor(anchor)
                    } else {
                        view?.addAnchorFailed("该直播间已存在——${anchor.nickname}")
                    }
                } else {
                    view?.addAnchorFailed("该直播间找寻不到")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                context.runOnUiThread {
                    context.toast("发生错误。")
                }
            }
        }

        private fun insertAnchor(anchor: Anchor) {
            MainAnchorHelper.insertAnchor(anchor)
            view?.addAnchorSuccess(anchor)
            //添加后获取状态
            getAnchorsStatus(anchor)
        }
    }

    init {
        initAnchorList()
//        loadAnchorList()
        getAllAnchorsStatus()
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
                    anchorAttributeMap[anchorAttribute.getAnchorKey()] = anchorAttribute
                    context.runOnUiThread {
                        sortAnchorListByStatus(anchorAttributeMap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    override fun getAllAnchorsStatus() {
        anchorList.value!!.forEach { anchor ->
            getAnchorsStatus(anchor)
        }
    }

    override fun onDestroy() {
        view = null
    }

}
