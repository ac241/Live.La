package com.acel.streamlivetool.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.ToastUtil


class AddAnchorViewModel(private val addAnchorFragment: AddAnchorFragment) : ViewModel() {

    class ViewModeFactory(private val addAnchorFragment: AddAnchorFragment) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AddAnchorViewModel(addAnchorFragment) as T
        }
    }
    private val anchorRepository =
        AnchorRepository.getInstance(addAnchorFragment.requireContext().applicationContext)


    fun addAnchor(queryAnchor: Anchor) {
        MainExecutor.execute(AddAnchorRunnable(queryAnchor))
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
                AppUtil.runOnUiThread {
                    ToastUtil.toast("发生错误。")
                }
            }
        }

        private fun insertAnchor(anchor: Anchor) {
            anchorRepository.insertAnchor(anchor)
            addAnchorFragment.addAnchorSuccess(anchor)
        }
    }
}

