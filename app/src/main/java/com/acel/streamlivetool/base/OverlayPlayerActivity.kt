package com.acel.streamlivetool.base

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.overlay.player.PlayerOverlayWindowManager
import com.acel.streamlivetool.util.AnchorListUtil
import com.acel.streamlivetool.util.ToastUtil
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
open class OverlayPlayerActivity : AppCompatActivity() {
    @NeedsPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun showPlayerOverlayWindow(anchor: Anchor, anchorList: List<Anchor>?) {
        val livingAnchors = anchorList?.let { AnchorListUtil.getLivingAnchors(it) }
        PlayerOverlayWindowManager.instance.playList(anchor, livingAnchors)
    }

    @OnPermissionDenied(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun showDeniedForSystemAlertWindow() {
        ToastUtil.toast("需要权限来使用悬浮窗功能。")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResult(requestCode)
    }
}