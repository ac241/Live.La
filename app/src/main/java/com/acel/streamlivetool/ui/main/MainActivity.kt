package com.acel.streamlivetool.ui.main

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
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
import com.acel.streamlivetool.value.PreferenceVariable
import com.acel.streamlivetool.databinding.ActivityMainBinding
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.platform.base.AbstractPlatformImpl
import com.acel.streamlivetool.ui.custom.AlertDialogTool
import com.acel.streamlivetool.ui.custom.FloatingAvatar
import com.acel.streamlivetool.ui.main.add_eidt_anchor.AddAnchorFragment
import com.acel.streamlivetool.ui.main.add_eidt_anchor.EditAnchorFragment
import com.acel.streamlivetool.ui.main.cookie.CookieFragment
import com.acel.streamlivetool.ui.main.group.GroupFragment
import com.acel.streamlivetool.ui.main.player.PlayerFragment
import com.acel.streamlivetool.ui.main.player.PlayerServiceForegroundManager
import com.acel.streamlivetool.ui.settings.SettingsActivity
import com.acel.streamlivetool.util.AnchorClickAction
import com.acel.streamlivetool.util.CommonColor
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import com.acel.streamlivetool.manager.WifiManager
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.collections.set
import kotlin.properties.Delegates

class MainActivity : OverlayWindowActivity() {

    private lateinit var binding: ActivityMainBinding

    private val groupFragment = GroupFragment.newInstance()
    private val addAnchorFragment by lazy { AddAnchorFragment.instance }
    private var playerFragment: PlayerFragment? = null

    /**
     * 播放页面是否显示
     */
    var isPlayerFragmentShown = false

    /**
     * 显示的platforms
     */
    private val displayablePlatforms = mutableListOf<AbstractPlatformImpl>()

    /**
     * 平台fragments
     */
    val platformFragments = mutableMapOf<AbstractPlatformImpl, CookieFragment>()

    /**
     * 初始化平台
     */
    private fun initDisplayablePlatforms(platformsSet: Set<String>) {
        displayablePlatforms.clear()
        //平台顺序
        val sortPlatformArray = MyApplication.application.resources.getStringArray(R.array.platform)
        sortPlatformArray.forEach {
            if (platformsSet.contains(it))
                PlatformDispatcher.getPlatformImpl(it)?.let { it1 -> displayablePlatforms.add(it1) }
        }
        initPlatformFragments()
        binding.viewPager.adapter?.notifyDataSetChanged()
    }

    private fun initPlatformFragments() {
        platformFragments.clear()
        displayablePlatforms.forEach { platform ->
            platformFragments[platform] = CookieFragment.newInstance(platform.platform)
        }
    }


    companion object OnNewIntentAction {
        private const val PLAYER_FRAGMENT_NAME = "player_fragment"

        //---------open fragment
        const val ACTION_OPEN_FRAGMENT = "action_open_fragment"
        const val EXTRA_KEY_OPEN_FRAGMENT = "extra_key_fragment"
        const val EXTRA_VALUE_OPEN_PLAYER_FRAGMENT = "open_player_fragment"
        const val EXTRA_KEY_ANCHOR = "extra_key_anchor"
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent?.action) {
            ACTION_OPEN_FRAGMENT -> {
                when (intent.getStringExtra(EXTRA_KEY_OPEN_FRAGMENT)) {
                    EXTRA_VALUE_OPEN_PLAYER_FRAGMENT -> {
                        val anchor = intent.getParcelableExtra<Anchor>(EXTRA_KEY_ANCHOR)
                        if (anchor != null) {
                            startPlayerFragment(anchor, null)
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WifiManager.startListen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        /**
         * 后台时发出前台通知
         */
        lifecycle.addObserver(PlayerServiceForegroundManager(this))

        whiteSystemBar()
        CommonColor.bindResource(resources)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                window.attributes = this
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI()
        } else {
            if (isPlayerFragmentShown)
                blackSystemBar()
            else
                whiteSystemBar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CommonColor.unbindResource()
        WifiManager.stopListen()
    }

    private fun initView() {
        setSupportActionBar(binding.toolbar)
        initViewPager()
        initTabLayout()
        PreferenceVariable.apply {
            displayablePlatformSet.observe(this@MainActivity) {
                initDisplayablePlatforms(it)
            }
        }
    }

    private fun initViewPager() {
        binding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@MainActivity) {
                override fun getItemCount(): Int {
                    return this@MainActivity.displayablePlatforms.size + 1
                }

                override fun createFragment(position: Int): Fragment {
                    return when (position) {
                        0 -> groupFragment
                        else -> platformFragments[this@MainActivity.displayablePlatforms[position - 1]] as Fragment
                    }
                }
            }.also {
                it.stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.ALLOW
            }
            offscreenPageLimit = 1
        }
    }


    private fun initTabLayout() {
        TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager
        ) { tab, position ->
            tab.text =
                if (position == 0) "主页" else this.displayablePlatforms[position - 1].platformName
            tab.view.setOnClickListener {
                tabViewClick = Pair(position, System.currentTimeMillis())
            }

            if (position != 0) {
                tab.view.setOnLongClickListener {
                    showClearCookieAlert(position)
                    return@setOnLongClickListener true
                }
            }
        }.attach()
    }

    /**
     * 显示清除cookie对话框
     */
    private fun showClearCookieAlert(position: Int) {
        binding.viewPager.setCurrentItem(position, true)
        //清除cookie
        val dialogBuilder = AlertDialogTool.newAlertDialog(this)
            .setTitle(
                getString(
                    R.string.clear_platform_cookie_alert,
                    this.displayablePlatforms[position - 1].platformName
                )
            )
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                this.displayablePlatforms[position - 1].cookieManager.clearCookie()
                platformFragments[this.displayablePlatforms[position - 1]]?.viewModel?.updateAnchorList()
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
                    groupFragment.scrollToTop()
                else
                    platformFragments[this.displayablePlatforms[new.first - 1]]?.scrollToTop()
            }
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

    /**
     * 显示新增/搜索窗口
     */
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
            whiteSystemBar()
//            showSystemUI()
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
            startPlayerFragmentWithAnimate(anchor, anchorList, itemView)
        else
            AnchorClickAction.itemClick(context, anchor, anchorList)
    }

    private var lastItemClickTime = 0L
    private var itemClickSleepTime = 300L

    private fun startPlayerFragmentWithAnimate(
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
            startPlayerFragment(anchor, anchorList)
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                avatarMovingAnimate(view)
        }
    }

    private fun startPlayerFragment(
        anchor: Anchor,
        anchorList: List<Anchor>?
    ) {
        closePlayerFragment()
        playerFragment =
            PlayerFragment.newInstance(
                anchor,
                if (anchorList == null) null else ArrayList<Anchor>().apply { addAll(anchorList) }
            )

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            addToBackStack(PLAYER_FRAGMENT_NAME)
            setCustomAnimations(R.anim.fade_in, 0, 0, R.anim.fade_out)
            replace(binding.fragmentContainer.id, playerFragment!!)
            isPlayerFragmentShown = true
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

        //重新生成一个bitmap，防止item avatar变形
        val bitmap = avatar.drawable.toBitmap()

        if (binding.root.findViewById<ImageView>(R.id.float_avatar) == null)
            layoutInflater.inflate(R.layout.float_avatar, binding.root, true)
        val animateAvatar = binding.root.findViewById<FloatingAvatar>(R.id.float_avatar)
        animateAvatar.apply {
            setImageBitmap(bitmap)
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
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private val windowInsetsController by lazy { ViewCompat.getWindowInsetsController(binding.root) }

    @Suppress("DEPRECATION")
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
        if (supportFragmentManager.backStackEntryCount == 0) {
            isPlayerFragmentShown = false
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        }
    }

    @Suppress("DEPRECATION")
    private fun whiteSystemBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ResourcesCompat.getColor(resources, R.color.background_light, null)
        window.navigationBarColor =
            ResourcesCompat.getColor(resources, R.color.background_light, null)
        if (!isNightMode()) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
        windowInsetsController?.isAppearanceLightStatusBars = !isNightMode()
    }

    @Suppress("DEPRECATION")
    fun blackSystemBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
        } else {
            window.statusBarColor = Color.BLACK
        }
        window.navigationBarColor =
            ResourcesCompat.getColor(resources, R.color.background_light, null)

    }

    fun closePlayerFragment() {
        supportFragmentManager.popBackStack(PLAYER_FRAGMENT_NAME, 1)
    }

    fun checkFollowed(anchor: Anchor) {
        groupFragment.checkFollowed(anchor)
    }

    fun getPlayingAnchor(): Anchor? {
        return playerFragment?.viewModel?.anchor?.value
    }

    fun showEditAnchorFragment(anchor: Anchor) {
        EditAnchorFragment.getInstance(anchor).show(supportFragmentManager, "edit_anchor")
    }

}

