package com.acel.streamlivetool.ui.main

import android.Manifest
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.overlay.ListOverlayWindowManager
import com.acel.streamlivetool.ui.overlay.PlayerOverlayWindowManager
import com.acel.streamlivetool.ui.settings.SettingsActivity
import com.acel.streamlivetool.util.ToastUtil.toast
import permissions.dispatcher.*
import kotlin.properties.Delegates

@RuntimePermissions
class MainActivity : BaseActivity() {
    private val groupFragment by lazy { GroupFragment.newInstance() }
    private val cookieFragment by lazy { CookieFragment.newInstance() }
    private val addAnchorFragment by lazy { AddAnchorFragment.instance }

    private var nowFragment: Fragment? = null

    override fun createdDo() {
        supportFragmentManager.beginTransaction().add(R.id.fragment, cookieFragment)
            .add(R.id.fragment, groupFragment).show(groupFragment).commit()
        nowFragment = groupFragment
    }

    override fun getResLayoutId(): Int {
        return R.layout.activity_test
    }

    companion object {
        enum class ListItemType {
            Text, Graphic;
        }
    }

    @NeedsPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun showListOverlayWindow(anchorList: List<Anchor>) {
        ListOverlayWindowManager.instance.toggleShow(
            this,
            anchorList
        )
    }

    @NeedsPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun showPlayerOverlayWindow(anchor: Anchor) {
        PlayerOverlayWindowManager.instance.play(anchor)
    }

    @Suppress("UNUSED_PARAMETER")
    @OnShowRationale(Manifest.permission.SYSTEM_ALERT_WINDOW)
    internal fun showRationaleForSystemAlertWindow(request: PermissionRequest?) {
    }

    @OnPermissionDenied(Manifest.permission.SYSTEM_ALERT_WINDOW)
    internal fun showDeniedForSystemAlertWindow() {
        toast("无权限")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResult(requestCode)
    }

    fun playStream(anchor: Anchor) {
        showPlayerOverlayWindowWithPermissionCheck(anchor)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.group_mode_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_main_setting -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.action_cookie_anchor -> {
                showAddAnchorFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAddAnchorFragment() {
        addAnchorFragment.show(supportFragmentManager, "add_anchor_fragment")
    }

    fun toggleFragment() {
        when (nowFragment) {
            is GroupFragment -> {
                showCookieFragment()
            }
            is CookieFragment -> {
                showGroupFragment()
            }
        }
    }

    private fun showGroupFragment() {
        supportFragmentManager.popBackStack()
        supportFragmentManager.beginTransaction().addToBackStack("group").hide(cookieFragment)
            .show(groupFragment)
            .commit()
        nowFragment = groupFragment
        Log.d("showGroupFragment", supportFragmentManager.backStackEntryCount.toString())
    }

    private fun showCookieFragment() {
        supportFragmentManager.popBackStack()
        supportFragmentManager.beginTransaction().addToBackStack("cookie").hide(groupFragment)
            .show(cookieFragment)
            .commit()
        nowFragment = cookieFragment
        Log.d("showGroupFragment", supportFragmentManager.backStackEntryCount.toString())
    }

    private var backPressedTime by Delegates.observable(0L) { _, old, new ->
        // 2次的时间间隔小于2秒就退出了
        if (new - old < 2000) {
            finish()
        } else {
            toast("双击返回键退出")
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0)
            super.onBackPressed()
        else
            backPressedTime = System.currentTimeMillis()
    }

}