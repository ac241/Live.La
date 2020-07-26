package com.acel.streamlivetool.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.acel.streamlivetool.R
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_cookie_mode.*
import kotlinx.android.synthetic.main.fab_layout.*

class CookieFragment : Fragment() {
    val platforms = mutableListOf<IPlatform>().also {
        for (entry in PlatformDispatcher.getAllPlatformInstance()) {
            if (entry.value.supportCookieMode)
                it.add(entry.value)
        }
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
        return inflater.inflate(R.layout.activity_cookie_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        btn_fab.setOnClickListener {
            fabClick()
        }
    }

    private fun fabClick() {
        (requireActivity() as MainActivity).toggleFragment()
    }

    companion object {
        @JvmStatic
        fun newInstance() = CookieFragment()
    }
}