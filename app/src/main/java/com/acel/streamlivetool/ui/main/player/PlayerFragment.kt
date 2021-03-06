package com.acel.streamlivetool.ui.main.player

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.Keep
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.showPlayerOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.databinding.FragmentPlayerBinding
import com.acel.streamlivetool.net.ImageLoader.loadImage
import com.acel.streamlivetool.platform.PlatformDispatcher.platformImpl
import com.acel.streamlivetool.ui.custom_view.AdjustType
import com.acel.streamlivetool.ui.custom_view.AdjustablePlayerView
import com.acel.streamlivetool.ui.custom_view.addItemWhiteTextColor
import com.acel.streamlivetool.ui.custom_view.blackAlphaPopupMenu
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.util.ToastUtil
import com.acel.streamlivetool.util.defaultSharedPreferences
import com.acel.streamlivetool.util.toPx
import kotlinx.coroutines.*
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser

class PlayerFragment : Fragment() {
    private lateinit var binding: FragmentPlayerBinding
    internal val viewModel by activityViewModels<PlayerViewModel>()

    internal var fullScreen = MutableLiveData(false)
    internal var landscape = false
    private val orientationEventListener by lazy { OrientationEventListener(this) }

    private var emitDanmuJob: Job? = null

    private var danmuDefaultTextSize: Float = DANMU_DEFAULT_TEXT_SIZE

    private val danmakuContext = DanmakuContext.create().apply {
        setMaximumVisibleSizeInScreen(0)
        setMaximumLines(mapOf(Pair(BaseDanmaku.TYPE_SCROLL_RL, 10)))
    }

    private val danmakuParser = object : BaseDanmakuParser() {
        override fun parse(): IDanmakus {
            return Danmakus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (requireActivity() as MainActivity).blackStatusBar()
        (requireActivity() as MainActivity).showSystemUI()

        arguments?.let { bundle ->
            val anchor = bundle.getParcelable<Anchor>("anchor")
            val anchorList = bundle.getParcelableArrayList<Anchor>("anchor_list")
            anchor?.let {
                viewModel.setAnchorDataAndPlay(it, anchorList)
            }
        }

        lifecycleScope.launchWhenCreated {
            //等待500确保弹幕开启
            delay(300)
            if (
                viewModel.danmuStatus.value?.first != PlayerViewModel.DanmuState.CONNECTING &&
                viewModel.danmuStatus.value?.first != PlayerViewModel.DanmuState.START
            )
                viewModel.startDanmu()
        }

        lifecycle.addObserver(orientationEventListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observeLiveData()
    }

    /**
     * 初始化view
     */
    @SuppressLint("InflateParams")
    private fun initView() {

        //播放view 设置controller
        binding.playerView.apply {
            setOnAdjustListener(object : AdjustablePlayerView.AdjustListener {
                override fun onAdjust(type: AdjustType, progress: Int) {
                    binding.adjustPrompt.showProgress(type, progress)
                }

                override fun onCancel() {
                    binding.adjustPrompt.hide()
                }
            })
            setControllerVisibilityListener {
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    if (it == View.GONE)
                        (requireActivity() as MainActivity).hideSystemUI()
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
                            blackAlphaPopupMenu(requireContext(), this)
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
                        ToastUtil.toast("没有更多的清晰度可以选择。")
                    }
                }
            }
            findViewById<ImageView>(R.id.controller_setting).setOnClickListener {
                val view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.popup_player_controller_setting, null, false)
                val popupWindow = PopupWindow(requireContext())
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
                val popupMenu = PopupMenu(requireContext(), this)
                popupMenu.menu.apply {
                    add("打开app").setOnMenuItemClickListener {
                        viewModel.startAppForCurrentAnchor(requireContext())
                        viewModel.stopAll()
                        true
                    }
                    add("悬浮窗").setOnMenuItemClickListener {
                        viewModel.anchor.value?.let { it1 ->
                            viewModel.stopAll()
                            val activity = requireActivity() as MainActivity
                            activity.showPlayerOverlayWindowWithPermissionCheck(
                                it1, viewModel.anchorList.value
                            )
                            activity.closePlayerFragment()
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
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
            (requireActivity() as MainActivity).showSystemUI()
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
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        binding.playerView.layoutParams.apply {
            height = ConstraintLayout.LayoutParams.MATCH_PARENT
            width = ConstraintLayout.LayoutParams.MATCH_PARENT
            binding.playerView.layoutParams = this
        }
        (requireActivity() as MainActivity).hideSystemUI()
        fullScreen.value = true
        landscape = true
        enableOrientationListener()
    }

    /**
     * 竖屏全屏
     */
    private fun portraitFullScreen() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
        (requireActivity() as MainActivity).navigationBlack()
        fullScreen.value = true
        landscape = false
    }

    @Suppress("unused")
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

    @Suppress("unused")
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
                danmuDefaultTextSize = DANMU_DEFAULT_TEXT_SIZE
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                landscapeFullScreen()
                danmuDefaultTextSize = DANMU_LANDSCAPE_TEXT_SIZE
            }
            else -> {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDanmu()

        //如果是横屏则切换
        lifecycleScope.launch(Dispatchers.Default) {
            //等待100，以免出现未隐藏navigation bar的问题
            delay(100)
            withContext(Dispatchers.Main) {
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                        landscapeFullScreen()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopDanmu("离开")
    }

    override fun onDestroy() {
        viewModel.stopAll()
        binding.danmakuView.release()
        super.onDestroy()
//        normalScreen()
        (requireActivity() as MainActivity).onPlayerFragmentDestroy()
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
                    textSize = danmuDefaultTextSize
                    textColor = Color.WHITE
//                    textShadowColor = Color.BLACK
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
            anchor.observe(viewLifecycleOwner) {
                if (it != null) {
                    displayAnchorDetail(it)
                    viewModel.getAnchorDetails(it)
                }
            }
            anchorDetails.observe(viewLifecycleOwner) {
                if (it != null && it == viewModel.anchor.value)
                    displayAnchorDetail(it)
            }

            playerStatus.observe(viewLifecycleOwner) {
                if (it != null)
                    when (it) {
                        PlayerViewModel.PlayerStatus.PLAYING -> {
                            binding.progressBar.visibility = View.GONE
                            binding.errorMsg.visibility = View.GONE
                            binding.playerView.keepScreenOn = true
                        }
                        PlayerViewModel.PlayerStatus.LOADING,
                        PlayerViewModel.PlayerStatus.BUFFERING ->
                            binding.progressBar.visibility = View.VISIBLE
                        PlayerViewModel.PlayerStatus.ENDED -> {
                            binding.playerView.keepScreenOn = false
                        }
                        PlayerViewModel.PlayerStatus.IDLE,
                        PlayerViewModel.PlayerStatus.ERROR -> {
                            binding.progressBar.visibility = View.GONE
                            binding.playerView.keepScreenOn = false
                        }
                        else -> {
                        }
                    }
            }
            playerMessage.observe(viewLifecycleOwner) {
                if (it.isNotEmpty() && !viewModel.isPlaying) {
                    binding.errorMsg.apply {
                        visibility = View.VISIBLE
                        text = it
                        fadeIn()
                    }
                    binding.progressBar.visibility = View.GONE
                }
            }
            currentQuality.observe(viewLifecycleOwner) {
                binding.playerView.findViewById<TextView>(R.id.current_quality).text =
                    it?.description ?: getString(R.string.no_video_quality_to_choose)
            }
            videoResolution.observe(viewLifecycleOwner) {
                setZoomButtonImage()
            }
            danmuStatus.observe(viewLifecycleOwner) {
                binding.danmuNotice.text = it.second
                if (it.first == PlayerViewModel.DanmuState.START)
                    startEmitDanmuJob()
                if (it.first == PlayerViewModel.DanmuState.ERROR || it.first == PlayerViewModel.DanmuState.STOP) {
                    stopEmitDanmuJob()
                    binding.danmakuView.clearDanmakusOnScreen()
                }
            }
        }
        fullScreen.observe(viewLifecycleOwner) {
            setZoomButtonImage()
        }
    }

    private fun setZoomButtonImage() {
        val resolution = viewModel.videoResolution.value
        if (resolution != null) {
            if (!fullScreen.value!!)
                binding.playerView.findViewById<ImageView>(R.id.btn_zoom)
                    .setImageResource(if (resolution.first < resolution.second) R.drawable.ic_full_screen_portrait else R.drawable.ic_maximize)
            else
                binding.playerView.findViewById<ImageView>(R.id.btn_zoom)
                    .setImageResource(R.drawable.ic_minimize)
        } else
            binding.playerView.findViewById<ImageView>(R.id.btn_zoom)
                .setImageResource(R.drawable.ic_maximize)

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
     * 淡入显示
     */
    private fun View.fadeIn() {
        alpha = 0f
        animate().setDuration(500).alpha(1f)
    }

//    override fun onBackPressed() {
//        if (fullScreen.value!!)
//            normalScreen()
//        else
//            super.onBackPressed()
//    }

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

    fun showAvatar() {
        binding.avatar.visibility = View.VISIBLE
    }

    fun handleBackPressed(): Boolean {
        return if (fullScreen.value!!) {
            normalScreen()
            true
        } else false
    }

    companion object {
        //        private val instance by lazy { PlayerFragment() }
        val DANMU_DEFAULT_TEXT_SIZE = 16f.toPx()
        val DANMU_LANDSCAPE_TEXT_SIZE = 20f.toPx()

        @JvmStatic
        fun newInstance(anchor: Anchor, anchorList: ArrayList<Anchor>?) = PlayerFragment().apply {
            arguments = Bundle().apply {
                putParcelable("anchor", anchor)
                putParcelableArrayList("anchor_list", anchorList)
            }
        }
//        : PlayerFragment {
//            return instance.apply {
//                arguments = Bundle().apply {
//                    putParcelable("anchor", anchor)
//                    putParcelableArrayList("anchor_list", anchorList)
//                }
//            }
//        }
    }
}