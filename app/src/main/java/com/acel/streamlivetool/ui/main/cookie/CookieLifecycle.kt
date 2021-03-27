package com.acel.streamlivetool.ui.main.cookie

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.acel.streamlivetool.ui.main.MainActivity

@Suppress("unused")
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
    }
}