package com.acel.streamlivetool.ui.main.group

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

@Suppress("unused")
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
    }
}