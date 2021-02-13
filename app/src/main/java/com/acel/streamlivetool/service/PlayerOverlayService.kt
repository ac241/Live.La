/*
 * Copyright (c) 2020.
 * @author acel
 */

package com.acel.streamlivetool.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

//@RuntimePermissions
class PlayerOverlayService : Service() {


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