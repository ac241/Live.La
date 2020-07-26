package com.acel.streamlivetool.ui.cookie_mode

import android.Manifest
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.base.MyApplication.Companion.finishAllActivity
import com.acel.streamlivetool.base.MyApplication.Companion.isActivityFirst
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.group_mode.GroupModeActivity
import com.acel.streamlivetool.ui.overlay.ListOverlayWindowManager
import com.acel.streamlivetool.ui.overlay.PlayerOverlayWindowManager
import com.acel.streamlivetool.ui.public_interface.PlayOverlayFunction
import com.acel.streamlivetool.util.ToastUtil.toast
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_cookie_mode.*
import permissions.dispatcher.*

@RuntimePermissions
class CookieModeActivity : BaseActivity(), PlayOverlayFunction {
    override fun getResLayoutId(): Int {
        return R.layout.fragment_cookie_mode
    }

    val platforms = mutableListOf<IPlatform>().also {
        for (entry in PlatformDispatcher.getAllPlatformInstance()) {
            if (entry.value.supportCookieMode)
                it.add(entry.value)
        }
    }
    val fragments = mutableMapOf<IPlatform, AnchorsFragment>().also {
        platforms.forEach { platform ->
            it[platform] = AnchorsFragment(platform)
        }
    }

    override fun onBackPressed() {
        if (isActivityFirst(this)) {
            finishAllActivity()
            super.onBackPressed()
        } else
            startGroupModeActivity()
    }

    override fun createdDo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(android.R.color.background_light, null)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        cookie_viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return platforms.size
            }

            override fun createFragment(position: Int): Fragment {
                return fragments[platforms[position]] as Fragment
            }
        }
        TabLayoutMediator(
            cookie_tabLayout,
            cookie_viewPager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                tab.text = resources.getString(platforms[position].platformShowNameRes)
            }
        ).attach()
    }

    @Suppress("UNUSED_PARAMETER")
    fun fabClick(view: View) {
        startGroupModeActivity()
    }

    private fun startGroupModeActivity() {
        startActivity(Intent(this, GroupModeActivity::class.java))
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


    override fun playStream(anchor: Anchor) {
        showPlayerOverlayWindowWithPermissionCheck(anchor)
    }
}