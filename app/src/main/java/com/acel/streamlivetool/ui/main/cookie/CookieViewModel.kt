package com.acel.streamlivetool.ui.main.cookie

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.MainExecutor


class CookieViewModel(private val cookieFragment: CookieFragment) :
    ViewModel() {
    class ViewModeFactory(private val cookieFragment: CookieFragment) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CookieViewModel(
                cookieFragment
            ) as T
        }
    }


    val anchorList = mutableListOf<Anchor>()


    internal fun getAnchors() {
        MainExecutor.execute {
            try {
                val anchorsCookieMode =
                    cookieFragment.platform?.let {
                        PlatformDispatcher.getPlatformImpl(it)?.getAnchorsWithCookieMode()
                    }
                if (anchorsCookieMode != null) {
                    if (!anchorsCookieMode.cookieOk) {
                        cookieFragment.showLoginTextView()
                        anchorList.clear()
                        notifyDataChange()
                    } else {
                        with(anchorsCookieMode.anchors) {
                            if (this != null) {
                                if (this.isEmpty()) {
                                    cookieFragment.showListMsg("无数据")
                                } else
                                    cookieFragment.hideListMsg()
                                anchorList.clear()
                                anchorList.addAll(this)
                                com.acel.streamlivetool.util.AnchorListHelper.insertStatusPlaceHolder(
                                    anchorList
                                )
                                notifyDataChange()
                            }
                        }
                        cookieFragment.hideLoginTextView()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                AppUtil.runOnUiThread {
                    cookieFragment.binding?.cookieSwipeRefresh?.isRefreshing = false
                }
            }

        }
    }

    private fun notifyDataChange() {
        AppUtil.runOnUiThread {
            cookieFragment.nowAnchorAnchorAdapter.notifyAnchorsChange()
        }
    }
}

