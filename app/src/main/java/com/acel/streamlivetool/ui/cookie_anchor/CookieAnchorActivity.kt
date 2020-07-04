package com.acel.streamlivetool.ui.cookie_anchor

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.ActionClick.actionWhenClick
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_cookie_anchor.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.defaultSharedPreferences

class CookieAnchorActivity : BaseActivity() {
    override fun getResLayoutId(): Int {
        return R.layout.activity_cookie_anchor
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
}