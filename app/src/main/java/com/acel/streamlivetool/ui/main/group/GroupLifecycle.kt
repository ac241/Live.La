package com.acel.streamlivetool.ui.main.group

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.PreferenceConstant.showAnchorImage
import com.acel.streamlivetool.util.PreferenceConstant.showAnchorImageWhenMobileData

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
//        groupFragment.updateFinish()

        //切换显示图片

        if (showAnchorImage) {
            //如果显示图片
            if (AppUtil.isWifiConnected()) {
                //如果wifi连接
                groupFragment.setShowImage(true)
            } else {
                //如果wifi未连接
                if (showAnchorImageWhenMobileData) {
                    //如果流量时显示图片
                    groupFragment.setShowImage(true)
                } else {
                    //如果流量时不显示图片
                    groupFragment.setShowImage(false)
                }
            }
        } else {
            //如果不显示图片
            groupFragment.setShowImage(false)
        }
    }
}