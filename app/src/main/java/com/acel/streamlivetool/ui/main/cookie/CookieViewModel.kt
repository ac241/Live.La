package com.acel.streamlivetool.ui.main.cookie

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.MainExecutor
import kotlinx.android.synthetic.main.fragment_cookie_anchors.*
import kotlinx.android.synthetic.main.layout_login_first.*


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
                        if (cookieFragment.viewStub_login_first != null)
                            cookieFragment.showLoginSub()
                    } else {
                        with(anchorsCookieMode.anchors) {
                            if (this != null) {
                                anchorList.clear()
                                anchorList.addAll(this)
                                com.acel.streamlivetool.util.AnchorListHelper.insertStatusPlaceHolder(
                                    anchorList
                                )
                                AppUtil.runOnUiThread {
                                    cookieFragment.nowAnchorAnchorAdapter.notifyAnchorsChange()
                                }
                            }
                        }
                        if (cookieFragment.login_first_wrapper != null && cookieFragment.login_first_wrapper.visibility == View.VISIBLE)
                            AppUtil.runOnUiThread {
                                cookieFragment.login_first_wrapper.visibility = View.GONE
                            }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                AppUtil.runOnUiThread {
                    cookieFragment.cookie_swipe_refresh.isRefreshing = false
                }
            }

        }
    }
}

