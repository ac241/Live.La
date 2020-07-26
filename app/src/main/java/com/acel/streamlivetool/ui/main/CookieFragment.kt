package com.acel.streamlivetool.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.defaultSharedPreferences
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_cookie_mode.*

class CookieFragment : Fragment() {
    val a = mutableListOf<IPlatform>().also {
        for (entry in PlatformDispatcher.getAllPlatformInstance()) {
            if (entry.value.supportCookieMode)
                it.add(entry.value)
        }
    }
    val platforms by lazy {
        val platforms = mutableListOf<IPlatform>()
        val sortPlatformArray = MyApplication.application.resources.getStringArray(R.array.platform)
        val showSet = defaultSharedPreferences.getStringSet(
            MyApplication.application.getString(R.string.pref_key_cookie_mode_platform_showable),
            setOf()
        )

        if (showSet != null)
            sortPlatformArray.forEach {
                if (!showSet.contains(it))
                    return@forEach
                val platform = PlatformDispatcher.getPlatformImpl(it)
                if (platform != null) {
                    if (platform.supportCookieMode)
                        platforms.add(platform)
                }
            }
        platforms
    }

    val fragments = mutableMapOf<IPlatform, CookieAnchorsFragment>().also {
        platforms.forEach { platform ->
            it[platform] = CookieAnchorsFragment(platform)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cookie_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    companion object {
        @JvmStatic
        fun newInstance() = CookieFragment()
    }
}