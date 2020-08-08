package com.acel.streamlivetool.ui.main.group

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.acel.streamlivetool.R
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.main.adapter.GraphicAnchorAdapter
import com.acel.streamlivetool.ui.main.adapter.TextAnchorAdapter
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.ToastUtil
import com.acel.streamlivetool.util.defaultSharedPreferences

class GroupLifecycle(private val groupFragment: GroupFragment) : LifecycleObserver {
    private var resumeTimes = 0
    var lastGetAnchorsTime = 0L
    private val refreshDelayTime = 20000
    private val mobileDataTextOnly = defaultSharedPreferences.getBoolean(
        groupFragment.getString(R.string.pref_key_mobile_data_only_text),
        false
    )

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
        if (mobileDataTextOnly) {
            if (AppUtil.isWifiConnected()) {
                if (groupFragment.nowAnchorAnchorAdapter !is GraphicAnchorAdapter &&
                    groupFragment.listItemType == MainActivity.Companion.ListItemType.Graphic
                ) {
                    groupFragment.setGraphicAdapter()
                    ToastUtil.toast("Wifi切换到有图模式")
                }
            } else {
                if (groupFragment.nowAnchorAnchorAdapter !is TextAnchorAdapter) {
                    groupFragment.setTextAdapter()
                    ToastUtil.toast("移动流量切换到无图模式")
                }
            }
        }
    }
}