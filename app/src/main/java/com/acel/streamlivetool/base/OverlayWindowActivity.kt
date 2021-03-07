package com.acel.streamlivetool.base

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.overlay.list.ListOverlayWindowManager
import com.acel.streamlivetool.ui.overlay.player.PlayerOverlayWindowManager
import com.acel.streamlivetool.util.AnchorListUtil
import com.acel.streamlivetool.util.ToastUtil
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
open class OverlayWindowActivity : BaseActivity() {
    private var playerOverlayBroadcastReceiver: PlayerOverlayBroadcastReceiver? = null
    private var isPlayerOverlayShown = false
    private var playerOverlayWindowManager: PlayerOverlayWindowManager? = null

    fun isPlayerOverlayShown() = isPlayerOverlayShown

    @NeedsPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun showPlayerOverlayWindow(anchor: Anchor, anchorList: List<Anchor>?) {
        if (playerOverlayBroadcastReceiver == null) {
            playerOverlayBroadcastReceiver = PlayerOverlayBroadcastReceiver()
        }
        playerOverlayBroadcastReceiver?.register()
        val livingAnchors = anchorList?.let { AnchorListUtil.getLivingAnchors(it) }
        if (playerOverlayWindowManager == null)
            playerOverlayWindowManager = PlayerOverlayWindowManager.instance
        playerOverlayWindowManager?.playList(anchor, livingAnchors)
    }

    @OnPermissionDenied(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun showDeniedForSystemAlertWindow() {
        ToastUtil.toast("需要权限来使用悬浮窗功能。")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResult(requestCode)
    }

    @NeedsPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun showListOverlayWindow(anchorList: List<Anchor>) {
        ListOverlayWindowManager.instance.toggleShow(this, anchorList)
    }

    inner class PlayerOverlayBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == PlayerOverlayWindowManager.BROADCAST_ACTION) {
                val isShown = intent.getBooleanExtra(PlayerOverlayWindowManager.SHOWN_KEY, false)
                isPlayerOverlayShown = if (isShown) {
                    true
                } else {
                    unregister()
                    false
                }
            }
        }

        fun register() {
            registerReceiver(this, IntentFilter(PlayerOverlayWindowManager.BROADCAST_ACTION))
        }

        private fun unregister() {
            unregisterReceiver(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerOverlayWindowManager?.release()
        playerOverlayWindowManager = null
    }
}