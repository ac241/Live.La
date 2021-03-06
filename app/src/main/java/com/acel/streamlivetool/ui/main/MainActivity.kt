package com.acel.streamlivetool.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.base.OverlayWindowActivity
import com.acel.streamlivetool.base.showPlayerOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.databinding.ActivityMainBinding
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.custom_view.FloatingAvatar
import com.acel.streamlivetool.ui.main.add_anchor.AddAnchorFragment
import com.acel.streamlivetool.ui.main.cookie.CookieFragment
import com.acel.streamlivetool.ui.main.group.GroupFragment
import com.acel.streamlivetool.ui.main.player.PlayerFragment
import com.acel.streamlivetool.ui.main.player.PlayerServiceForegroundListener
import com.acel.streamlivetool.ui.settings.SettingsActivity
import com.acel.streamlivetool.util.AnchorClickAction
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.collections.set
import kotlin.properties.Delegates

class MainActivity : OverlayWindowActivity() {

    private var playerFragment: PlayerFragment? = null
    private val mainFragment = GroupFragment.newInstance()
    private val addAnchorFragment by lazy { AddAnchorFragment.instance }
    private val displayPlatformPage by lazy {
        val platforms = mutableListOf<IPlatform>()
        val sortPlatformArray = MyApplication.application.resources.getStringArray(R.array.platform)
        val displayablePlatformSet = defaultSharedPreferences.getStringSet(
            MyApplication.application.getString(R.string.pref_key_cookie_mode_platform_showable),
            setOf()
        )
        if (displayablePlatformSet != null)
            sortPlatformArray.forEach {
                if (!displayablePlatformSet.contains(it))
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
    private var isPlayerFragmentShown = false
    fun isPlayerFragmentShown() = isPlayerFragmentShown

    companion object OnNewIntentAction {
        private const val PLAYER_FRAGMENT_NAME = "player_fragment"

        //--------pref changes
        const val ACTION_PREF_CHANGES = "main_new_intent"
        const val EXTRA_KEY_PREF_CHANGES = "key_pref_changes"
        const val PREF_PLATFORMS_CHANGED = "pref_platforms_changed"
        const val PREF_SHOW_IMAGE_CHANGED = "pref_show_image_changed"

        //---------open fragment
        const val ACTION_OPEN_FRAGMENT = "action_open_fragment"
        const val EXTRA_KEY_OPEN_FRAGMENT = "extra_key_fragment"
        const val OPEN_PLAYER_FRAGMENT = "open_player_fragment"
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
            ACTION_PREF_CHANGES -> {
                val changes = intent.getStringArrayListExtra(EXTRA_KEY_PREF_CHANGES)
                changes?.forEach {
                    when (it) {
                        PREF_PLATFORMS_CHANGED -> {
                            updatePlatformData()
                            (binding.viewPager.adapter as FragmentStateAdapter).notifyDataSetChanged()
                        }
                    }
                }
            }
            ACTION_OPEN_FRAGMENT -> {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        lifecycle.addObserver(PlayerServiceForegroundListener(this))
        whiteStatusBar()
    }

    private fun init() {
        setSupportActionBar(binding.toolbar)
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
        binding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@MainActivity) {
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
                it.stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.ALLOW
            }
            offscreenPageLimit = 1
        }
    }

    fun playStreamOverlay(
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
        if (playerFragment == null)
            backPressed()
        else {
            playerFragment?.apply {
                val result = handleBackPressed()
                if (!result)
                    backPressed()
            }
        }
    }

    private fun backPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            whiteStatusBar()
            supportFragmentManager.popBackStack()
        } else {
            backPressedTime = System.currentTimeMillis()
        }
    }

    fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    fun gotoMainPage() {
        binding.viewPager.setCurrentItem(0, true)
    }

    fun itemClick(itemView: View, context: MainActivity, anchor: Anchor, anchorList: List<Anchor>) {
        val action = defaultSharedPreferences.getString(
            MyApplication.application.getString(R.string.pref_key_item_click_action), ""
        )
        if (action == context.getString(R.string.string_inner_player))
            startPlayerFragment(anchor, anchorList, itemView)
        else
            AnchorClickAction.itemClick(context, anchor, anchorList)
    }

    private var lastItemClickTime = 0L
    private var itemClickSleepTime = 300L

    private fun startPlayerFragment(
        anchor: Anchor,
        anchorList: List<Anchor>,
        view: View,
    ) {
        synchronized(lastItemClickTime) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastItemClickTime < itemClickSleepTime) {
                return
            }
            lastItemClickTime = currentTime
            playerFragment =
                PlayerFragment.newInstance(anchor, ArrayList<Anchor>().apply { addAll(anchorList) })

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                addToBackStack(PLAYER_FRAGMENT_NAME)
                setCustomAnimations(R.anim.fade_in, 0, 0, R.anim.fade_out)
                replace(binding.fragmentContainer.id, playerFragment!!)
                isPlayerFragmentShown = true
            }
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                avatarMovingAnimate(view)
        }
    }

    /**
     * 头像移动至fragment的动画
     */
    private fun avatarMovingAnimate(view: View) {
        val avatar = view.findViewById<ImageView>(R.id.grid_anchor_avatar)
        val avatarLocation = IntArray(2)
        avatar.getLocationInWindow(avatarLocation)

        val rootLocation = IntArray(2)
        binding.root.getLocationInWindow(rootLocation)

        //复制一个drawable，防止item avatar变形
        val drawable = avatar.drawable.toBitmap()

        if (binding.root.findViewById<ImageView>(R.id.float_avatar) == null)
            layoutInflater.inflate(R.layout.float_avatar, binding.root, true)
        val animateAvatar = binding.root.findViewById<FloatingAvatar>(R.id.float_avatar)
        animateAvatar.apply {
            setImageBitmap(drawable)
            val targetDimens = context.resources.getDimension(R.dimen.player_fragment_avatar)
            val targetX = resources.getDimension(R.dimen.player_fragment_avatar_margin_start)
            val targetY = resources.run {
                binding.root.width * 9 / 16 + getDimension(R.dimen.player_fragment_title) +
                        getDimension(R.dimen.player_fragment_avatar_margin_top)
            }
            move(
                startLocation = Pair(
                    avatarLocation[0].toFloat(),
                    avatarLocation[1].toFloat() - rootLocation[1]
                ),
                targetLocation = Pair(targetX, targetY),
                startDimens = Pair(avatar.width.toFloat(), avatar.height.toFloat()),
                targetDimens = Pair(targetDimens, targetDimens),
                duration = 300L,
                doOnCancel = {
                    binding.root.removeView(animateAvatar)
                },
                doOnEnd = {
                    binding.root.removeView(animateAvatar)
                    playerFragment!!.showAvatar()
                }
            )
        }
    }

    @Suppress("DEPRECATION")
    internal fun hideSystemUI() {
        val opt = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        window.decorView.systemUiVisibility =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                (opt or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            } else {
                opt
            }
    }

    private val windowInsetsController by lazy { ViewCompat.getWindowInsetsController(binding.root) }

    internal fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_VISIBLE)
//        windowInsetsController?.apply { show(WindowInsetsCompat.Type.systemBars()) }
    }

    /**
     * 黑色底部导航栏
     */
    internal fun navigationBlack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = Color.BLACK
        }
    }

    fun onPlayerFragmentDestroy() {
//        showSystemUI()
//        whiteStatusBar()
        if (supportFragmentManager.backStackEntryCount == 0) {
            isPlayerFragmentShown = false
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        }
    }

    private fun whiteStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(android.R.color.background_light, null)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    fun blackStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = Color.TRANSPARENT
            } else {
                window.statusBarColor = Color.BLACK
            }
            window.navigationBarColor = Color.TRANSPARENT
        }
    }

    fun closePlayerFragment() {
        playerFragment?.let { supportFragmentManager.popBackStack(PLAYER_FRAGMENT_NAME, 1) }
    }

    fun checkFollowed(anchor: Anchor) {
        mainFragment.checkFollowed(anchor)
    }

}