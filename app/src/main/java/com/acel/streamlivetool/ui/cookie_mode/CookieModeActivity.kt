package com.acel.streamlivetool.ui.cookie_mode

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.group_mode.GroupModeActivity
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_cookie_mode.*

class CookieModeActivity : BaseActivity() {
    override fun getResLayoutId(): Int {
        return R.layout.activity_cookie_mode
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(android.R.color.background_light, null)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return platforms.size
            }

            override fun createFragment(position: Int): Fragment {
                return fragments[platforms[position]] as Fragment
            }
        }
        TabLayoutMediator(
            tabLayout,
            viewPager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                tab.text = resources.getString(platforms[position].platformShowNameRes)
            }
        ).attach()
    }

    @Suppress("UNUSED_PARAMETER")
    fun fabClick(view: View) {
        startActivity(Intent(this, GroupModeActivity::class.java))
    }
}