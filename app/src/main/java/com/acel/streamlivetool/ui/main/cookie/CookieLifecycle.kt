package com.acel.streamlivetool.ui.main.cookie

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.acel.streamlivetool.R
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.main.adapter.GraphicAnchorAdapter
import com.acel.streamlivetool.ui.main.adapter.TextAnchorAdapter
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.defaultSharedPreferences

class CookieLifecycle(private val cookieFragment: CookieFragment) : LifecycleObserver {
    var lastGetAnchorsTime = 0L
    private val refreshDelayTime = 20000
    private val mobileDataTextOnly = defaultSharedPreferences.getBoolean(
        cookieFragment.getString(R.string.pref_key_mobile_data_only_text),
        false
    )

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
        if (mobileDataTextOnly) {
            if (AppUtil.isWifiConnected()) {
                if (cookieFragment.nowAnchorAnchorAdapter !is GraphicAnchorAdapter &&
                    cookieFragment.layoutManagerType == MainActivity.Companion.ListItemType.Graphic
                ) {
                    cookieFragment.setGraphicAdapter()
//                    ToastUtil.toast("Wifi切换到有图模式")
                }
            } else {
                if (cookieFragment.nowAnchorAnchorAdapter !is TextAnchorAdapter) {
                    cookieFragment.setTextAdapter()
//                    ToastUtil.toast("移动流量切换到无图模式")
                }
            }
        }
    }
}