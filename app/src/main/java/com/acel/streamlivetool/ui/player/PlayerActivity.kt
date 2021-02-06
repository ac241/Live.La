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
import com.acel.streamlivetool.R
import com.acel.streamlivetool.databinding.ActivityPlayerBinding
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.AppUtil

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
//        lifecycle.addObserver(MyOrientationEventListener(this))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { viewModel.setAnchorData(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.setKeepData()
    }

    private fun initView() {
        binding.playerView.apply {
            this.setControllerVisibilityListener {
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    if (it == View.GONE) {
                        hideSystemUI()
                    }
            }
            player = viewModel.player
            keepScreenOn = true
            findViewById<View>(R.id.btn_replay).setOnClickListener {
                viewModel.replay()
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
        binding.platformIcon?.setOnClickListener {
            viewModel.anchor.value?.let { it1 -> AppUtil.startApp(this, it1) }
            viewModel.stopPlay()
        }
        binding.listView?.apply {
            adapter = viewModel.anchorList.value?.let { PlayerListAdapter(this@PlayerActivity, it) }
        }
    }

    private fun observeLiveData() {
        viewModel.apply {
            anchor.observe(this@PlayerActivity) {
                it.apply {
                    avatar?.let { it1 ->
                        binding.avatar?.let { it2 ->
                            ImageLoader.load(this@PlayerActivity, it1, it2)
                        }
                    }
                    binding.nickname?.text = nickname
                    binding.include?.typeName?.text = typeName
                    binding.roomId?.text = getString(R.string.room_id_format, showId)
                    binding.title?.text = title
                    PlatformDispatcher.getPlatformImpl(it)?.iconRes?.let { res ->
                        binding.platformIcon?.setImageResource(res)
                    }
                }
            }
            anchorList.observe(this@PlayerActivity) {
                binding.listView?.adapter?.notifyDataSetChanged()
            }
            anchorPosition.observe(this@PlayerActivity) {
                binding.listView?.adapter?.apply {
                    this as PlayerListAdapter
                    setChecked(it)
                }
            }
            status.observe(this@PlayerActivity) {
                if (it != null)
                    when (it) {
                        PlayerViewModel.State.IS_PLAYING -> {
                            binding.progressBar.visibility = View.GONE
                            binding.errorMsg.visibility = View.GONE
                        }
                        PlayerViewModel.State.IS_LOADING ->
                            binding.progressBar.visibility = View.VISIBLE
                        PlayerViewModel.State.IS_ENDED, PlayerViewModel.State.IS_IDLE, PlayerViewModel.State.IS_ERROR ->
                            binding.progressBar.visibility = View.GONE
                    }


            }
            errorMessage.observe(this@PlayerActivity) {
                if (it.isNotEmpty() && viewModel.status.value == PlayerViewModel.State.IS_ERROR) {
                    binding.errorMsg.apply {
                        visibility = View.VISIBLE
                        text = it
                        fadeIn()
                    }
                    binding.progressBar.visibility = View.GONE
                }
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (hasFocus) hideSystemUI()
        }
    }


    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
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

//    @Suppress("DEPRECATION")
//    private fun showSystemUI() {
//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
//    }

}