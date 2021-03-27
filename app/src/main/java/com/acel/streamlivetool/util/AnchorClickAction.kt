package com.acel.streamlivetool.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher.platformImpl
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.util.AppUtil.mainThread
import com.acel.streamlivetool.util.AppUtil.startApp
import com.acel.streamlivetool.util.ToastUtil.toast

object AnchorClickAction {

    fun itemClick(context: Context, anchor: Anchor, list: List<Anchor>) {
        actionWhenClick(
            context,
            defaultSharedPreferences.getString(
                MyApplication.application.getString(R.string.pref_key_item_click_action),
                ""
            ), anchor, list
        )
    }

    fun secondBtnClick(context: Context, anchor: Anchor, list: List<Anchor>) {
        actionWhenClick(
            context,
            defaultSharedPreferences.getString(
                MyApplication.application.getString(R.string.pref_key_second_button_click_action),
                ""
            ), anchor, list
        )
    }

    fun contextItemClick(context: Context, anchor: Anchor, list: List<Anchor>, itemId: Int) {
        val action =
            when (itemId) {
                R.id.action_item_open_as_open_app ->
                    context.getString(R.string.string_open_app)
                R.id.action_item_open_as_inner_player ->
                    context.getString(R.string.string_inner_player)
                R.id.action_item_open_as_outer_player ->
                    context.getString(R.string.string_outer_player)
                R.id.action_item_open_as_overlay_player ->
                    context.getString(R.string.string_overlay_player)
                else ->
                    ""
            }
        actionWhenClick(context, action, anchor, list)
    }

    private fun actionWhenClick(
        context: Context,
        action: String?,
        anchor: Anchor,
        list: List<Anchor>
    ) {
        when (action) {
            context.getString(R.string.string_open_app) -> {
                startApp(context, anchor)
            }
            context.getString(R.string.string_outer_player) -> {
                MainExecutor.execute {
                    try {
                        callOuterPlayer(context, anchor)
                    } catch (e: Exception) {
                        mainThread {
                            toast(context.getString(R.string.get_streaming_failed))
                        }
                        e.printStackTrace()
                    }
                }
            }
            context.getString(R.string.string_overlay_player) -> {
                (context as MainActivity).playStreamOverlay(anchor, list)
            }
            context.getString(R.string.string_inner_player) -> {
                startInnerPlayer(context, anchor)
            }
            else -> {
                toast("未定义的功能，你是怎么到达这里的0_0")
            }
        }
    }

    fun startInnerPlayer(
        context: Context,
        anchor: Anchor
    ) {
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            action = MainActivity.ACTION_OPEN_FRAGMENT
            putExtra(
                MainActivity.EXTRA_KEY_OPEN_FRAGMENT,
                MainActivity.EXTRA_VALUE_OPEN_PLAYER_FRAGMENT
            )
            putExtra(MainActivity.EXTRA_KEY_ANCHOR, anchor)
        })
    }

    /**
     * 调用第三方播放器
     */
    private fun callOuterPlayer(context: Context, anchor: Anchor) {
        val streamingLive = anchor.platformImpl()?.streamingLiveModule?.getStreamingLive(anchor)
        if (streamingLive?.url != null) {
            val uri = Uri.parse(streamingLive.url)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "video/*")
            context.startActivity(intent)
        } else {
            mainThread {
                toast(context.getString(R.string.streaming_url_is_null))
            }
        }
    }

}