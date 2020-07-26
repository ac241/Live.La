package com.acel.streamlivetool.ui.main

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.MainExecutor
import kotlinx.android.synthetic.main.fragment_cookie_anchors.*
import kotlinx.android.synthetic.main.layout_login_first.*


class CookieAnchorsViewModel(private val cookieAnchorsFragment: CookieAnchorsFragment) : ViewModel() {
    class ViewModeFactory(private val cookieAnchorsFragment: CookieAnchorsFragment) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CookieAnchorsViewModel(cookieAnchorsFragment) as T
        }
    }


    val anchorList = mutableListOf<Anchor>()


    internal fun getAnchors() {
        MainExecutor.execute {
            val anchorsCookieMode = cookieAnchorsFragment.platform.getAnchorsWithCookieMode()
            if (!anchorsCookieMode.cookieOk) {
                if (cookieAnchorsFragment.viewStub_login_first != null)
                    cookieAnchorsFragment.showLoginSub()
            } else {
                with(anchorsCookieMode.anchors) {
                    if (this != null) {
                        anchorList.clear()
                        anchorList.addAll(this)
                        com.acel.streamlivetool.util.AnchorListHelper.insertStatusPlaceHolder(
                            anchorList
                        )
                        AppUtil.runOnUiThread {
                            cookieAnchorsFragment.nowAnchorAnchorAdapter.notifyAnchorsChange()
                        }
                    }
                }
                if (cookieAnchorsFragment.login_first_wrapper != null && cookieAnchorsFragment.login_first_wrapper.visibility == View.VISIBLE)
                    AppUtil.runOnUiThread {
                        cookieAnchorsFragment.login_first_wrapper.visibility = View.GONE
                    }
            }
            AppUtil.runOnUiThread {
                cookieAnchorsFragment.cookie_anchor_swipe_refresh.isRefreshing = false
            }
        }
    }
}

