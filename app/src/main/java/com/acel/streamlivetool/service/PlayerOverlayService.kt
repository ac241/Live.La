/*
 * Copyright (c) 2020.
 * @author acel
 */

package com.acel.streamlivetool.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.overlay.list.ListOverlayWindowManager
import com.acel.streamlivetool.ui.overlay.player.PlayerOverlayWindowManager
import com.acel.streamlivetool.util.AnchorListUtil
import com.acel.streamlivetool.util.ToastUtil
import permissions.dispatcher.*

//@RuntimePermissions
class PlayerOverlayService : Service() {
    override fun onCreate() {
        super.onCreate()
        Log.d("onCreate", "service create")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("onCreate", "service onStartCommand")

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
//
//    @NeedsPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
//    fun showListOverlayWindow(anchorList: List<Anchor>) {
//        ListOverlayWindowManager.instance.toggleShow(
//            this,
//            anchorList
//        )
//    }
//
//    @NeedsPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
//    fun showPlayerOverlayWindow(anchor: Anchor, anchorList: List<Anchor>) {
//        val livingAnchors = AnchorListUtil.getLivingAnchors(anchorList)
//        PlayerOverlayWindowManager.instance.playList(anchor, livingAnchors)
//    }
//
//    @Suppress("UNUSED_PARAMETER")
//    @OnShowRationale(Manifest.permission.SYSTEM_ALERT_WINDOW)
//    internal fun showRationaleForSystemAlertWindow(request: PermissionRequest?) {
//    }
//
//    @OnPermissionDenied(Manifest.permission.SYSTEM_ALERT_WINDOW)
//    internal fun showDeniedForSystemAlertWindow() {
//        ToastUtil.toast("无权限")
//    }
//
//    fun playStream(
//        anchor: Anchor,
//        list: List<Anchor>
//    ) {
////        showPlayerOverlayWindowWithPermissionCheck(anchor, list as MutableList<Anchor>)
//    }
}