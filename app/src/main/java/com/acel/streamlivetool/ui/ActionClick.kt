package com.acel.streamlivetool.ui

import android.content.ActivityNotFoundException
import android.content.Context
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

object ActionClick {

    fun itemClick(context: Context, anchor: Anchor) {
        actionWhenClick(
            context,
            MyApplication.application.defaultSharedPreferences.getString(
                MyApplication.application.getString(R.string.pref_key_item_click_action),
                ""
            ), anchor
        )
    }

    fun secondBtnClick(context: Context, anchor: Anchor) {
        actionWhenClick(
            context,
            MyApplication.application.defaultSharedPreferences.getString(
                MyApplication.application.getString(R.string.pref_key_second_button_click_action),
                ""
            ), anchor
        )

    }

    fun actionWhenClick(context: Context, actionSecondBtn: String?, anchor: Anchor) {
        when (actionSecondBtn) {
            context.getString(R.string.string_open_app) -> {
                doAsync {
                    val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
                    try {
                        platformImpl?.startApp(context, anchor)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                        uiThread {
                            MyApplication.application.toast(
                                "没有找到" +
                                        platformImpl?.platformShowNameRes?.let { it1 ->
                                            MyApplication.application.resources.getString(
                                                it1
                                            )
                                        }
                                        + " app..."
                            )
                        }
                    }
                }
            }
            context.getString(R.string.string_outer_player) -> {
                doAsync {
                    val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
                    platformImpl?.callOuterPlayer(context, anchor)
                }
            }
            else -> {
                MyApplication.application.toast("未定义的功能，你是怎么到达这里的0_0")
            }
        }
    }
}