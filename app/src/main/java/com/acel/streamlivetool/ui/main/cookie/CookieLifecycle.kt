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
            cookieFragment.viewModel.updateAnchorList()
            cookieFragment.loginFinish()
        } else
            System.currentTimeMillis().apply {
                if (lastGetAnchorsTime == 0L || this - lastGetAnchorsTime > refreshDelayTime) {
                    cookieFragment.viewModel.updateAnchorList()
                    lastGetAnchorsTime = this
                }
            }

        //设置toolbar文字
        (cookieFragment.requireActivity() as MainActivity).setToolbarTitle("平台")

        //隐藏刷新按钮
//        cookieFragment.updateFinish()


        //切换显示图片

        if (PreferenceConstant.showAnchorImage) {
            //如果显示图片
            if (AppUtil.isWifiConnected()) {
                //如果wifi连接
                cookieFragment.setShowImage(true)
            } else {
                //如果wifi未连接
                if (PreferenceConstant.showAnchorImageWhenMobileData) {
                    //如果流量时显示图片
                    cookieFragment.setShowImage(true)
                } else {
                    //如果流量时不显示图片
                    cookieFragment.setShowImage(false)
                }
            }
        } else {
            //如果不显示图片
            cookieFragment.setShowImage(false)
        }
    }
}