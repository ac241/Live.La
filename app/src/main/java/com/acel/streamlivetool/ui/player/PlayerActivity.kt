package com.acel.streamlivetool.ui.player

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.acel.streamlivetool.R
import com.acel.streamlivetool.databinding.ActivityPlayerBinding
import com.acel.streamlivetool.net.ImageLoader

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding

    private val viewModel by viewModels<PlayerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeLiveData()
        initView()
        viewModel.setAnchorData(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { viewModel.setAnchorData(it) }
    }

    private fun initView() {
        binding.playerView.apply {
//            this.setControllerVisibilityListener {
//                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
//                    when (it) {
//                        View.VISIBLE ->
//                            showSystemUI()
//                        else ->
//                            hideSystemUI()
//                    }
//            }
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
    }

    private fun observeLiveData() {
        viewModel.apply {
            anchor.observe(this@PlayerActivity) {
                it.apply {
                    avatar?.let { it1 ->
                        ImageLoader.load(
                            this@PlayerActivity,
                            it1,
                            binding.avatar
                        )
                    }
                    binding.nickname.text = nickname
                    binding.include.typeName.text = typeName
                    binding.roomId.text = getString(R.string.room_id_format, showId)
                }
            }
            isPlaying.observe(this@PlayerActivity) {
                if (it)
                    binding.progressBar.visibility = View.GONE
                else
                    binding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    override fun onBackPressed() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else
            super.onBackPressed()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            if (hasFocus) hideSystemUI()
    }


    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
//        val controller = ViewCompat.getWindowInsetsController(window.decorView)
//        controller?.apply {
//            hide(WindowInsetsCompat.Type.systemBars())
//        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
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

    @Suppress("DEPRECATION")
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

}