package com.acel.streamlivetool.ui.main.add_anchor

import android.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.ToastUtil.toast


class AddAnchorViewModel(private val addAnchorFragment: AddAnchorFragment) : ViewModel() {

    class ViewModeFactory(private val addAnchorFragment: AddAnchorFragment) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AddAnchorViewModel(
                addAnchorFragment
            ) as T
        }
    }

    private val anchorRepository =
        AnchorRepository.getInstance()


    fun addAnchor(queryAnchor: Anchor) {
        MainExecutor.execute(AddAnchorRunnable(queryAnchor))
    }

    fun search(keyword: String, platform: String) {
        MainExecutor.execute {
            val platformImpl = PlatformDispatcher.getPlatformImpl(platform)
            val list = platformImpl?.searchAnchor(keyword)
            if (list == null)
                runOnUiThread { toast("该平台暂不支持搜索功能") }
            list?.apply {
                if (list.isEmpty()) {
                    runOnUiThread { toast("搜索结果为空。") }
                    return@execute
                }
                val builder = AlertDialog.Builder(addAnchorFragment.requireContext())
                val arrayList = arrayListOf<String>()
                forEach {
                    arrayList.add("${it.nickname},${it.roomId}")
                }
                var choiceAnchor: Anchor? = list[0]
                builder.setSingleChoiceItems(
                    arrayList.toTypedArray(),
                    0
                ) { _, which ->
                    choiceAnchor = get(which)
                }
                builder.setPositiveButton("添加") { _, _ ->
                    choiceAnchor?.apply { insertAnchor(this) }
                }
                builder.setNegativeButton("取消", null)
                builder.setTitle("$keyword@${platformImpl.platformName} 的搜索结果：")
                runOnUiThread {
                    builder.show()
                }
            }
        }
    }

    private inner class AddAnchorRunnable(val queryAnchor: Anchor) : Runnable {
        override fun run() {
            val platformImpl = PlatformDispatcher.getPlatformImpl(queryAnchor.platform)
            try {
                val anchor = platformImpl?.getAnchor(queryAnchor)
                if (anchor != null) {
                    if (anchorRepository.anchorList.value!!.indexOf(anchor) == -1) {
                        insertAnchor(anchor)
                    } else {
                        addAnchorFragment.addAnchorFailed("该直播间已存在——${anchor.nickname}")
                    }
                } else {
                    addAnchorFragment.addAnchorFailed("找不到该直播间")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    toast("发生错误。")
                }
            }
        }
    }

    private fun insertAnchor(anchor: Anchor) {
        val result = anchorRepository.insertAnchor(anchor)
        if (result.first)
            addAnchorFragment.addAnchorSuccess(anchor)
        else
            addAnchorFragment.addAnchorFailed(result.second)
    }
}

