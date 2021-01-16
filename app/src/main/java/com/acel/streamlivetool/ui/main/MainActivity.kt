package com.acel.streamlivetool.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.databinding.ActivityMainBinding
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.service.PlayerOverlayService
import com.acel.streamlivetool.ui.main.add_anchor.AddAnchorFragment
import com.acel.streamlivetool.ui.main.cookie.CookieFragment
import com.acel.streamlivetool.ui.main.group.GroupFragment
import com.acel.streamlivetool.ui.overlay.list.ListOverlayWindowManager
import com.acel.streamlivetool.ui.overlay.player.PlayerOverlayWindowManager
import com.acel.streamlivetool.ui.settings.SettingsActivity
import com.acel.streamlivetool.util.AnchorListUtil.getLivingAnchors
import com.acel.streamlivetool.util.PreferenceConstant
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import com.google.android.material.tabs.TabLayoutMediator
import permissions.dispatcher.*
import kotlin.collections.set
import kotlin.properties.Delegates

@RuntimePermissions
class MainActivity : AppCompatActivity() {
    private val mainFragment = GroupFragment.newInstance()
    private val addAnchorFragment by lazy { AddAnchorFragment.instance }
    private val displayPlatformPage by lazy {
        val platforms = mutableListOf<IPlatform>()
        val sortPlatformArray = MyApplication.application.resources.getStringArray(R.array.platform)
        val showablePlatformSet = defaultSharedPreferences.getStringSet(
            MyApplication.application.getString(R.string.pref_key_cookie_mode_platform_showable),
            setOf()
        )
        if (showablePlatformSet != null)
            sortPlatformArray.forEach {
                if (!showablePlatformSet.contains(it))
                    return@forEach
                val platform = PlatformDispatcher.getPlatformImpl(it)
                if (platform != null) {
                    if (platform.supportCookieMode)
                        platforms.add(platform)
                }
            }
        platforms.size > 0
    }
    private lateinit var binding: ActivityMainBinding

    object OnNewIntentAction {
        const val PREF_CHANGED = "pref_changed"
        const val PREF_PLATFORMS_CHANGED = "pref_platforms_changed"
        const val PREF_SHOW_IMAGE_CHANGED = "pref_show_image_changed"
    }

    val platforms by lazy {
        initPlatforms()
    }

    private fun initPlatforms(): MutableList<IPlatform> {
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
        return platforms
    }

    val platformFragments by lazy {
        initPlatformFragments()
    }

    private fun initPlatformFragments(): MutableMap<IPlatform, CookieFragment> {
        val map = mutableMapOf<IPlatform, CookieFragment>()
        platforms.forEach { platform ->
            map[platform] = CookieFragment.newInstance(platform.platform)
        }
        return map
    }

    private fun updatePlatformData() {
        platforms.clear()
        platforms.addAll(initPlatforms())

        platformFragments.clear()
        platformFragments.putAll(initPlatformFragments())
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent?.action) {
            OnNewIntentAction.PREF_CHANGED -> {
                val changes = intent.getStringArrayListExtra("changes")
                changes?.forEach {
                    when (it) {
                        OnNewIntentAction.PREF_PLATFORMS_CHANGED -> {
                            updatePlatformData()
                            (binding.viewPager.adapter as FragmentStateAdapter).notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    @Suppress("DEPRECATION")
    private fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(android.R.color.background_light, null)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setSupportActionBar(binding.toolbar)

//        binding.toolbar.setOnClickListener {
//            toolbarClickTime = System.currentTimeMillis()
//        }

        initViewPager()
        if (displayPlatformPage)
            TabLayoutMediator(
                binding.tabLayout,
                binding.viewPager
            ) { tab, position ->
                tab.text = if (position == 0) "主页" else platforms[position - 1].platformName
                tab.view.setOnClickListener {
                    tabViewClick = Pair(position, System.currentTimeMillis())
                }

                if (position != 0) {
                    tab.view.setOnLongClickListener {
                        showClearCookieAlert(position)
                        return@setOnLongClickListener true
                    }
//                    tab.setIcon(platforms[position - 1].iconRes)
                }
            }.attach()
    }

    private fun showClearCookieAlert(position: Int) {
        binding.viewPager.setCurrentItem(position, true)
        //清除cookie
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle(
                getString(
                    R.string.clear_platform_cookie_alert,
                    platforms[position - 1].platformName
                )
            )
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                platforms[position - 1].clearCookie()
                platformFragments[platforms[position - 1]]?.viewModel?.updateAnchorList()
            }
            .setNegativeButton(getString(R.string.no), null)
        dialogBuilder.show()
    }

    /**
     * 两次点击回到顶部
     * Pair<position,Time>
     */
    private var tabViewClick by Delegates.observable(Pair(0, 0L)) { _, old, new ->
        if (old.first == new.first) {
            if (new.second - old.second < 1000) {
                if (new.first == 0)
                    mainFragment.scrollToTop()
                else
                    platformFragments[platforms[new.first - 1]]?.scrollToTop()
            }
        }
    }

    private fun initViewPager() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return if (displayPlatformPage) platforms.size + 1 else 1
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> mainFragment
                    else -> platformFragments[platforms[position - 1]] as Fragment
                }
            }

        }.also {
            it.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
        }
        binding.viewPager.offscreenPageLimit = 1
    }

    @NeedsPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun showListOverlayWindow(anchorList: List<Anchor>) {
        ListOverlayWindowManager.instance.toggleShow(
            this,
            anchorList
        )
    }

    @NeedsPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun showPlayerOverlayWindow(anchor: Anchor, anchorList: List<Anchor>) {
        startPlayerOverlayService()
        val livingAnchors = getLivingAnchors(anchorList)
        PlayerOverlayWindowManager.instance.playList(anchor, livingAnchors)
    }

    private fun startPlayerOverlayService() {
        val intent = Intent(this, PlayerOverlayService::class.java)
        startService(intent)
    }

    private fun stopPlayerOverlayService() {
        val intent = Intent(this, PlayerOverlayService::class.java)
        stopService(intent)
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

    fun playStream(
        anchor: Anchor,
        list: List<Anchor>
    ) {
        showPlayerOverlayWindowWithPermissionCheck(anchor, list as MutableList<Anchor>)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
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

    /**
     * 两次点击退出
     */
    private var backPressedTime by Delegates.observable(0L) { _, old, new ->
        // 2次的时间间隔小于1秒就退出
        if (new - old < 1000) {
            finish()
        } else {
            toast("双击返回键退出")
        }
    }

    override fun onBackPressed() {
        backPressedTime = System.currentTimeMillis()
    }

    fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    fun gotoMainPage() {
        binding.viewPager.setCurrentItem(0, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayerOverlayService()
//        PlayerOverlayWindowManager.instance.destroy()
    }
}