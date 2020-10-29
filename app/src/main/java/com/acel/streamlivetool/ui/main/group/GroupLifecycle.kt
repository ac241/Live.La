package com.acel.streamlivetool.ui.main.group

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.PreferenceConstant.showAnchorImage
import com.acel.streamlivetool.util.PreferenceConstant.showAnchorImageWhenMobileData
import com.acel.streamlivetool.util.ToastUtil

class GroupLifecycle(private val groupFragment: GroupFragment) : LifecycleObserver {
    private var resumeTimes = 0
    private val refreshDelayTime = 20000

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        //获取数据
        if (resumeTimes != 0) {
            System.currentTimeMillis().apply {
                groupFragment.viewModel.lastUpdateTime.let {
                    if (it == 0L || this - it > refreshDelayTime) {
                        groupFragment.viewModel.updateAllAnchor()
                    }
                }
            }
        }
        resumeTimes++
        //隐藏刷新按钮
        groupFragment.hideSwipeRefreshBtn()

        //数据流量切换adapter
        if (showAnchorImage)
            if (AppUtil.isWifiConnected()) {
                if (!groupFragment.isShowImage()) {
                    groupFragment.setShowImage(true)
                    ToastUtil.toast("Wifi切换到有图模式")
                }
            } else {
                if (!showAnchorImageWhenMobileData) {
                    if (groupFragment.isShowImage()) {
                        groupFragment.setShowImage(false)
                        ToastUtil.toast("移动流量切换到无图模式")
                    }
                }
            }
    }
}