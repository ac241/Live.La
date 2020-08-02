package com.acel.streamlivetool.ui.main.group

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.acel.streamlivetool.ui.main.MainActivity

class GroupLifecycle(private val groupFragment: GroupFragment) : LifecycleObserver {
    var resumeTimes = 0

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        if (resumeTimes != 0) {
            groupFragment.viewModel.getAllAnchorsAttribute()
        }
        resumeTimes++
        (groupFragment.requireActivity() as MainActivity).setToolbarTitle("主页")
    }
}