package com.acel.streamlivetool.ui.main.cookie

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.PreferenceConstant

class CookieLifecycle(private val cookieFragment: CookieFragment) : LifecycleObserver {
    private var lastGetAnchorsTime = 0L
    private val refreshDelayTime = 20000

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        //获取数据
        if (cookieFragment.isLogining) {
            cookieFragment.viewModel.getAnchors()
            cookieFragment.loginFinish()
        } else
            System.currentTimeMillis().apply {
                if (lastGetAnchorsTime == 0L || this - lastGetAnchorsTime > refreshDelayTime) {
                    cookieFragment.viewModel.getAnchors()
                    lastGetAnchorsTime = this
                }
            }

        //设置toolbar文字
        (cookieFragment.requireActivity() as MainActivity).setToolbarTitle("平台")

        //隐藏刷新按钮
        cookieFragment.hideSwipeRefreshBtn()


        //数据流量切换adapter
        if (PreferenceConstant.showAnchorImage)
            if (AppUtil.isWifiConnected()) {
                if (!cookieFragment.isShowImage()) {
                    cookieFragment.setShowImage(true)
                }
            } else {
                if (!PreferenceConstant.showAnchorImageWhenMobileData) {
                    if (cookieFragment.isShowImage()) {
                        cookieFragment.setShowImage(false)
                    }
                }
            }
    }
}