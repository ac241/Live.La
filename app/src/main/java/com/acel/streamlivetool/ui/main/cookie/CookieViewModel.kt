package com.acel.streamlivetool.ui.main.cookie

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.ToastUtil.toast


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
                        runOnUiThread{
                            toast(if (anchorsCookieMode.message.isEmpty()) "请先登录" else anchorsCookieMode.message)
                        }
                    } else {
                        with(anchorsCookieMode.anchors) {
                            if (this != null) {
                                if (this.isEmpty()) {
                                    cookieFragment.showListMsg(if (anchorsCookieMode.message.isEmpty()) "无数据" else anchorsCookieMode.message)
                                } else
                                    cookieFragment.hideListMsg()
                                anchorList.clear()
                                anchorList.addAll(this)
                                com.acel.streamlivetool.util.AnchorListUtil.insertStatusPlaceHolder(
                                    anchorList
                                )
                                notifyDataChange()
                            }
                        }
                        cookieFragment.hideLoginTextView()
                    }
                }
            } catch (e: Exception) {
                Log.d("getAnchorsCookieMode", "cookie mode获取主播属性失败：cause:${e.javaClass.name}")
            } finally {
                runOnUiThread {
                    cookieFragment.binding?.cookieSwipeRefresh?.isRefreshing = false
                }
            }

        }
    }

    private fun notifyDataChange() {
        runOnUiThread {
            cookieFragment.nowAnchorAdapter.notifyAnchorsChange()
        }
    }
}

