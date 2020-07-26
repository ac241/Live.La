package com.acel.streamlivetool.util

import android.content.Context
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.public_interface.PlayOverlayFunction
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.AppUtil.startApp
import com.acel.streamlivetool.util.ToastUtil.toast

object ActionClick {

    fun itemClick(context: Context, anchor: Anchor) {
        actionWhenClick(
            context,
            defaultSharedPreferences.getString(
                MyApplication.application.getString(R.string.pref_key_item_click_action),
                ""
            ), anchor
        )
    }

    fun secondBtnClick(context: Context, anchor: Anchor) {
        actionWhenClick(
            context,
            defaultSharedPreferences.getString(
                MyApplication.application.getString(R.string.pref_key_second_button_click_action),
                ""
            ), anchor
        )
    }

    private fun actionWhenClick(context: Context, actionSecondBtn: String?, anchor: Anchor) {
        when (actionSecondBtn) {
            context.getString(R.string.string_open_app) -> {
                startApp(context, anchor)
            }
            context.getString(R.string.string_outer_player) -> {
                runOnUiThread {
                    val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
                    MainExecutor.execute {
                        platformImpl?.callOuterPlayer(context, anchor)
                    }
                }
            }
            context.getString(R.string.string_overlay_player) -> {
                (context as MainActivity).playStream(anchor)
            }
            else -> {
                toast("未定义的功能，你是怎么到达这里的0_0")
            }
        }
    }


}