package com.acel.streamlivetool.ui.player

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.OverlayWindowActivity
import com.acel.streamlivetool.base.showPlayerOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.databinding.ActivityPlayerBinding
import com.acel.streamlivetool.net.ImageLoader.loadImage
import com.acel.streamlivetool.platform.PlatformDispatcher.platformImpl
import com.acel.streamlivetool.ui.custom_view.addItemWhiteTextColor
import com.acel.streamlivetool.ui.custom_view.blackAlphaPopupMenu
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import com.acel.streamlivetool.util.toPx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser

/**
 * [PlayerFragment]
 */
@Deprecated("use PlayerFragment")
class WindowActivity : OverlayWindowActivity() {
    private var danmuTextSize: Float = 16f.toPx()
    private lateinit var binding: ActivityPlayerBinding

    internal val viewModel by viewModels<PlayerViewModel>()
    internal var fullScreen = MutableLiveData(false)
    internal var landscape = false
    private val orientationEventListener by lazy { OrientationEventListener(this) }

    private var emitDanmuJob: Job? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        observeLiveData()
//        lifecycle.addObserver(ForegroundListener())
        viewModel.setAnchorDataAndPlay(intent)

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
        //自动切换屏幕
        lifecycle.addObserver(orientationEventListener)
    }

    private val danmakuContext = DanmakuContext.create().apply {
        setMaximumVisibleSizeInScreen(0)
        setMaximumLines(mapOf(Pair(BaseDanmaku.TYPE_SCROLL_RL, 10)))
    }

    private val danmakuParser = object : BaseDanmakuParser() {
        override fun parse(): IDanmakus {
            return Danmakus()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { viewModel.setAnchorDataAndPlay(it) }
    }

    /**
     * 初始化view
     */
    @SuppressLint("InflateParams")
    private fun initView() {
        //如果是横屏则切换
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            landscapeFullScreen()

        //播放view 设置controller
        binding.playerView.apply {
            setControllerVisibilityListener {
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    if (it == View.GONE)
                        hideSystemUI()
            }
            player = viewModel.player
            hideController()
            findViewById<View>(R.id.btn_replay)?.setOnClickListener {
                viewModel.replay()
                it.animate().setDuration(1000).rotationBy(360f).start()
            }
            findViewById<View>(R.id.btn_zoom).setOnClickListener {
                zoomClick()
            }

            findViewById<TextView>(R.id.current_quality).apply {
                setOnClickListener {
                    val qualityList = viewModel.qualityList.value
                    if (qualityList != null && qualityList.isNotEmpty()) {
                        val popupMenu =
                            blackAlphaPopupMenu(this@WindowActivity, this)
                        popupMenu.menu.apply {
                            viewModel.qualityList.value?.forEach { quality ->
                                addItemWhiteTextColor(quality?.description)
                                    .setOnMenuItemClickListener {
                                        quality?.let { viewModel.playWithAssignQuality(quality) }
                                        true
                                    }
                            }
                        }
                        popupMenu.show()
                    } else {
                        toast("没有更多的清晰度可以选择。")
                    }
                }
            }
            findViewById<ImageView>(R.id.controller_setting).setOnClickListener {
                val view = LayoutInflater.from(this@WindowActivity)
                    .inflate(R.layout.popup_player_controller_setting, null, false)
                val popupWindow = PopupWindow(this@WindowActivity)
                popupWindow.apply {
                    contentView = view
                    width = ViewGroup.LayoutParams.WRAP_CONTENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    isOutsideTouchable = true
                    setBackgroundDrawable(null)
                    showAsDropDown(it, 0, 10)
                }
                val danmuAlpha = view.findViewById<SeekBar>(R.id.seek_bar_danmu_alpha)
                danmuAlpha.apply {
                    progress = (binding.danmakuView.alpha * 100).toInt()
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        var alpha = 0f
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            alpha = progress / 100f
                            binding.danmakuView.alpha = alpha
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            defaultSharedPreferences.edit()
                                .putFloat(getString(R.string.key_danmu_alpha), alpha).apply()
                            popupWindow.dismiss()
                        }
                    })
                }

            }
        }

        //icon图标
        binding.platformIcon.apply {
            setOnClickListener {
                val popupMenu = PopupMenu(this@WindowActivity, this)
                popupMenu.menu.apply {
                    add("打开app").setOnMenuItemClickListener {
                        viewModel.startAppForCurrentAnchor(this@WindowActivity)
                        viewModel.stopAll()
                        true
                    }
                    add("悬浮窗").setOnMenuItemClickListener {
                        viewModel.anchor.value?.let { it1 ->
                            showPlayerOverlayWindowWithPermissionCheck(
                                it1, viewModel.anchorList.value
                            )
                            finish()
                        }
                        true
                    }
                }
                popupMenu.show()
            }
        }

        //弹幕view
        binding.danmakuView.apply {
            enableDanmakuDrawingCache(true)
            setCallback(object : DrawHandler.Callback {
                override fun prepared() {
                    start()
                }

                override fun updateTimer(timer: DanmakuTimer?) {
                }

                override fun danmakuShown(danmaku: BaseDanmaku?) {
                }

                override fun drawingFinished() {
                }

            })
            prepare(danmakuParser, danmakuContext)
            alpha = defaultSharedPreferences.getFloat(getString(R.string.key_danmu_alpha), 0.6f)
        }

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                return if (position == 0)
                    DanmuListFragment.newInstance()
                else
                    AnchorListFragment.newInstance()
            }
        }

        binding.danmuNotice.setOnClickListener {
            viewModel.restartDanmu("重新连接弹幕服务器")
        }

        viewModel.danmuStatus.observe(this) {
            binding.danmuNotice.text = it.second
            if (it.first == PlayerViewModel.DanmuState.START)
                startEmitDanmuJob()
            if (it.first == PlayerViewModel.DanmuState.ERROR || it.first == PlayerViewModel.DanmuState.STOP) {
                stopEmitDanmuJob()
                binding.danmakuView.clearDanmakusOnScreen()
            }
        }

    }

    private fun zoomClick() {
        if (!fullScreen.value!!)
            fullScreen()
        else {
            normalScreen()
        }
    }

    /**
     * 竖屏常规
     */
    private fun normalScreen() {
        if (fullScreen.value!!) {
            if (landscape) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                binding.playerView.layoutParams.apply {
                    this as ConstraintLayout.LayoutParams
                    height = 0
                    width = 0
                    dimensionRatio = "16:9"
                    binding.playerView.layoutParams = this
                }
            } else {
                val start = binding.playerView.height
                val to = binding.root.width * 9 / 16
                val animatorHelper = PlayerViewAnimateFromInToOutHelper(binding.playerView)
                ObjectAnimator.ofInt(animatorHelper, "height", start, to)
                    .setDuration(200).start()
            }
            showSystemUI()
            fullScreen.value = false
            landscape = false
            disableOrientationListener()
        }
    }

    /**
     * 全屏播放
     */
    private fun fullScreen() {
        val resolution = viewModel.videoResolution.value
        if (resolution != null) {
            if (resolution.first < resolution.second) {
                portraitFullScreen()
            } else {
                landscapeFullScreen()
            }
        } else {
            landscapeFullScreen()
        }
    }

    /**
     * 横屏全屏
     */
    private fun landscapeFullScreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        binding.playerView.layoutParams.apply {
            height = ConstraintLayout.LayoutParams.MATCH_PARENT
            width = ConstraintLayout.LayoutParams.MATCH_PARENT
            binding.playerView.layoutParams = this
        }
        hideSystemUI()
        fullScreen.value = true
        landscape = true
        enableOrientationListener()
    }

    /**
     * 竖屏全屏
     */
    private fun portraitFullScreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val start = binding.playerView.height
        val to = binding.root.height
        val animateHelper = PlayerViewAnimateFromOutToInHelper(binding.playerView)
        ObjectAnimator.ofInt(animateHelper, "height", start, to)
            .setDuration(200).start()

//        binding.playerView.layoutParams.apply {
//            this as ConstraintLayout.LayoutParams
//            height = ConstraintLayout.LayoutParams.MATCH_PARENT
//            width = ConstraintLayout.LayoutParams.MATCH_PARENT
//            dimensionRatio = null
//            binding.playerView.layoutParams = this
//        }
        navigationBlack()
        fullScreen.value = true
        landscape = false
    }

    inner class PlayerViewAnimateFromOutToInHelper(val view: View) {
        fun getHeight(): Int {
            return view.layoutParams.height
        }

        @Keep
        fun setHeight(int: Int) {
            val param = view.layoutParams.apply {
                width = ConstraintLayout.LayoutParams.MATCH_PARENT
                height = int
            }
            view.layoutParams = param
        }
    }

    inner class PlayerViewAnimateFromInToOutHelper(val view: View) {
        fun getHeight(): Int {
            return view.layoutParams.height
        }

        @Keep
        fun setHeight(int: Int) {
            val param = view.layoutParams.apply {
                width = ConstraintLayout.LayoutParams.MATCH_PARENT
                height = int
            }
            view.layoutParams = param
        }
    }

    /**
     * 开启屏幕方向监听
     */
    private fun enableOrientationListener() {
        orientationEventListener.enable()
        lifecycle.addObserver(orientationEventListener)
    }

    /**
     * 关闭屏幕方向监听
     */
    private fun disableOrientationListener() {
        orientationEventListener.disable()
        lifecycle.removeObserver(orientationEventListener)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                normalScreen()
                danmuTextSize = 16f.toPx()
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                landscapeFullScreen()
                danmuTextSize = 18f.toPx()
            }
            else -> {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDanmu()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopDanmu("activity stop")
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.danmakuView.release()
    }

    /**
     * 发送弹幕
     */
    private fun emitDanma(msg: String) {
        lifecycleScope.launch {
            val danmaku =
                danmakuContext.mDanmakuFactory?.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL)?.apply {
                    text = msg
                    padding = 3
                    time = binding.danmakuView.currentTime.plus(1200)
                    textSize = danmuTextSize
                    textColor = Color.WHITE
                    textShadowColor = Color.BLACK
                    priority = 1
                }
            danmaku?.let {
                binding.danmakuView.addDanmaku(it)
            }
        }
    }

    /**
     * 监听liveData
     */
    private fun observeLiveData() {
        viewModel.apply {
            anchor.observe(this@WindowActivity) {
                if (it != null) {
                    displayAnchorDetail(it)
                    viewModel.getAnchorDetails(it)
                }
            }
            anchorDetails.observe(this@WindowActivity) {
                if (it != null) {
                    displayAnchorDetail(it)
                }
            }

            playerStatus.observe(this@WindowActivity) {
                if (it != null)
                    when (it) {
                        PlayerViewModel.PlayerState.IS_PLAYING -> {
                            binding.progressBar.visibility = View.GONE
                            binding.errorMsg.visibility = View.GONE
                            binding.playerView.keepScreenOn = true
                        }
                        PlayerViewModel.PlayerState.IS_LOADING ->
                            binding.progressBar.visibility = View.VISIBLE
                        PlayerViewModel.PlayerState.IS_ENDED -> {
                            binding.playerView.keepScreenOn = false
                        }
                        PlayerViewModel.PlayerState.IS_IDLE,
                        PlayerViewModel.PlayerState.IS_ERROR -> {
                            binding.progressBar.visibility = View.GONE
                            binding.playerView.keepScreenOn = false
                        }
                    }
            }
            playerMessage.observe(this@WindowActivity) {
                if (it.isNotEmpty() && !viewModel.isPlaying) {
                    binding.errorMsg.apply {
                        visibility = View.VISIBLE
                        text = it
                        fadeIn()
                    }
                    binding.progressBar.visibility = View.GONE
                }
            }
            currentQuality.observe(this@WindowActivity) {
                binding.playerView.findViewById<TextView>(R.id.current_quality).text =
                    it?.description ?: getString(R.string.no_video_quality_to_choose)
            }
            videoResolution.observe(this@WindowActivity) {
                setZoomButtonImage()
            }
        }
        fullScreen.observe(this) {
            setZoomButtonImage()
        }
    }

    private fun setZoomButtonImage() {
        val resolution = viewModel.videoResolution.value
        if (resolution != null) {
            if (!fullScreen.value!!)
                binding.playerView.findViewById<ImageView>(R.id.btn_zoom)
                    .setImageResource(if (resolution.first < resolution.second) R.drawable.ic_full_screen_portrait else R.drawable.ic_full_screen_landscape)
            else
                binding.playerView.findViewById<ImageView>(R.id.btn_zoom)
                    .setImageResource(R.drawable.ic_zoom_out)
        } else
            binding.playerView.findViewById<ImageView>(R.id.btn_zoom)
                .setImageResource(R.drawable.ic_full_screen_landscape)

    }

    /**
     * 开启推送弹幕线程
     */
    @Synchronized
    private fun startEmitDanmuJob() {
        stopEmitDanmuJob()
        emitDanmuJob = lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                viewModel.getPreEmitDanmu()?.let {
                    emitDanma(it.msg)
                }
                delay(20)
            }
        }
    }

    /**
     * 结束推送弹幕线程
     */
    private fun stopEmitDanmuJob() {
        emitDanmuJob?.cancel()
        emitDanmuJob = null
    }

    /**
     * 显示主播信息
     */
    private fun displayAnchorDetail(it: Anchor) {
        it.apply {
            it.avatar?.let { it1 ->
                binding.avatar.loadImage(it1)
                binding.playerView.findViewById<ImageView>(R.id.controller_avatar).loadImage(it1)
            }
            binding.nickname.text = nickname
            binding.playerView.findViewById<TextView>(R.id.controller_nickname).text = nickname
            binding.include.typeName.text = typeName
            binding.roomId.text = getString(R.string.room_id_format, showId)
            binding.title.text = title
            binding.playerView.findViewById<TextView>(R.id.controller_title).text = title
            platformImpl()?.iconRes?.let { res ->
                binding.platformIcon.setImageResource(res)
            }
        }
    }

    /**
     * 淡入显示
     */
    private fun View.fadeIn() {
        alpha = 0f
        animate().setDuration(500).alpha(1f)
    }

    override fun onBackPressed() {
        if (fullScreen.value!!)
            normalScreen()
        else
            super.onBackPressed()
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    @Suppress("DEPRECATION")
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_VISIBLE)
    }

    /**
     * 黑色底部导航栏
     */
    private fun navigationBlack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = Color.BLACK
        }
    }

}