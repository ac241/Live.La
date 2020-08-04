package com.acel.streamlivetool.ui.main.cookie

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


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        //获取数据
        cookieFragment.viewModel.getAnchors()

        //设置toolbar文字
        (cookieFragment.requireActivity() as MainActivity).setToolbarTitle("平台")

        //隐藏刷新按钮
        cookieFragment.hideSwipeRefreshBtn()

        val mobileDataTextOnly = defaultSharedPreferences.getBoolean(
            cookieFragment.getString(R.string.pref_key_mobile_data_only_text),
            false
        )
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