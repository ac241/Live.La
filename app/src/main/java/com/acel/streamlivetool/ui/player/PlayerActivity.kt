package com.acel.streamlivetool.ui.player

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.databinding.ActivityPlayerBinding
import com.acel.streamlivetool.net.ImageLoader.loadImage
import com.acel.streamlivetool.platform.PlatformDispatcher.platformImpl
import kotlinx.coroutines.launch
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding

    internal val viewModel by viewModels<PlayerViewModel>()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeLiveData()
        initView()
        lifecycle.addObserver(ForegroundServiceListener(this))
        viewModel.setAnchorData(intent)

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
//        lifecycle.addObserver(MyOrientationEventListener(this))
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
        intent?.let { viewModel.setAnchorData(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.screenRotation()
    }

    private fun initView() {
        binding.playerView.apply {
            setControllerVisibilityListener {
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    if (it == View.GONE)
                        hideSystemUI()
            }
            player = viewModel.player
            keepScreenOn = true
            hideController()
            findViewById<View>(R.id.btn_replay)?.setOnClickListener {
                viewModel.replay()
                it.animate().setDuration(1000).rotationBy(-360f).start()
            }
            findViewById<View>(R.id.btn_zoom).setOnClickListener {
                requestedOrientation = when (resources.configuration.orientation) {
                    Configuration.ORIENTATION_PORTRAIT ->
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else ->
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }
//        binding.platformIcon?.setOnClickListener {
//            viewModel.anchor.value?.let { it1 -> AppUtil.startApp(this, it1) }
//            viewModel.stopPlay()
//        }

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
            alpha = 0.8f
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
            viewModel.restartDanmu()
        }
        viewModel.danmuStatus.observe(this) {
            binding.danmuNotice.text = it.second
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDanmuFromActivity()
    }


    override fun onDestroy() {
        super.onDestroy()
        binding.danmakuView.release()
    }

    private fun addDanmaku(msg: String) {
        lifecycleScope.launch {
            val danmaku =
                danmakuContext.mDanmakuFactory?.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL)?.apply {
                    text = msg
                    padding = 3
                    time = binding.danmakuView.currentTime.plus(1200)
                    textSize = 16f.toPx()
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
     * sp转px的方法。
     */
    private fun Float.toPx(): Float {
        val fontScale = resources.displayMetrics.scaledDensity
        return (this * fontScale + 0.5f)
    }

    private fun observeLiveData() {
        viewModel.apply {
            anchor.observe(this@PlayerActivity) {
                displayAnchorDetail(it)
                viewModel.getAnchorDetails(it)
            }
            anchorDetails.observe(this@PlayerActivity) {
                displayAnchorDetail(it)
            }

            playerStatus.observe(this@PlayerActivity) {
                if (it != null)
                    when (it) {
                        PlayerViewModel.PlayerState.IS_PLAYING -> {
                            binding.progressBar.visibility = View.GONE
                            binding.errorMsg.visibility = View.GONE
                        }
                        PlayerViewModel.PlayerState.IS_LOADING ->
                            binding.progressBar.visibility = View.VISIBLE
                        PlayerViewModel.PlayerState.IS_ENDED, PlayerViewModel.PlayerState.IS_IDLE, PlayerViewModel.PlayerState.IS_ERROR ->
                            binding.progressBar.visibility = View.GONE
                    }
            }
            errorMessage.observe(this@PlayerActivity) {
                if (it.isNotEmpty() && viewModel.playerStatus.value == PlayerViewModel.PlayerState.IS_ERROR) {
                    binding.errorMsg.apply {
                        visibility = View.VISIBLE
                        text = it
                        fadeIn()
                    }
                    binding.progressBar.visibility = View.GONE
                }
            }
            danmuString.observe(this@PlayerActivity) {
                addDanmaku(it)
            }
        }
    }

    private fun displayAnchorDetail(it: Anchor) {
        it.apply {
            it.avatar?.let { it1 -> binding.avatar.loadImage(it1) }
            binding.nickname.text = nickname
            binding.include.typeName.text = typeName
            binding.roomId.text = getString(R.string.room_id_format, showId)
            binding.title.text = title
            platformImpl()?.iconRes?.let { res ->
                binding.platformIcon.setImageResource(res)
            }
        }
    }


    private fun View.fadeIn() {
        alpha = 0f
        animate().setDuration(500).alpha(1f)
    }

    override fun onBackPressed() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else
            super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.playerView.layoutParams.apply {
                height = ConstraintLayout.LayoutParams.MATCH_PARENT
                width = ConstraintLayout.LayoutParams.MATCH_PARENT
            }
            hideSystemUI()
        } else {
            binding.playerView.layoutParams.apply {
                height = 0
                width = 0
            }
            showSystemUI()
        }
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

}