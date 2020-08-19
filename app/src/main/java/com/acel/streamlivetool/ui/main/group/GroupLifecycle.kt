package com.acel.streamlivetool.ui.main.group

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.PreferenceConstant.showAnchorImage
import com.acel.streamlivetool.util.PreferenceConstant.showAnchorImageWhenMobileData
import com.acel.streamlivetool.util.ToastUtil

class GroupLifecycle(private val groupFragment: GroupFragment) : LifecycleObserver {
    private var resumeTimes = 0
    private var lastGetAnchorsTime = 0L
    private val refreshDelayTime = 20000

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        //获取数据
        if (resumeTimes != 0) {
            System.currentTimeMillis().apply {
                if (lastGetAnchorsTime == 0L || this - lastGetAnchorsTime > refreshDelayTime) {
                    groupFragment.viewModel.getAllAnchorsAttribute()
                    lastGetAnchorsTime = this
                }
            }
        }
        resumeTimes++
        //设置toolbar文字
        (groupFragment.requireActivity() as MainActivity).setToolbarTitle("主页")
        //隐藏刷新按钮
        groupFragment.hideSwipeRefreshBtn()

        //数据流量切换adapter
        if (showAnchorImage)
            if (showAnchorImageWhenMobileData) {
                if (AppUtil.isWifiConnected()) {
                    if (!groupFragment.isShowImage()) {
                        groupFragment.setShowImage(true)
                        ToastUtil.toast("Wifi切换到有图模式")
                    }
                } else {
                    if (groupFragment.isShowImage()) {
                        groupFragment.setShowImage(false)
                        ToastUtil.toast("移动流量切换到无图模式")
                    }
                }
            }
    }
}