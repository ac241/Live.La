package com.acel.streamlivetool.ui.player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.databinding.ActivityPlayerBinding
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.ToastUtil
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding

    private val viewModel by viewModels<PlayerViewModel>()

    private val player by lazy { SimpleExoPlayer.Builder(this).build() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeLiveData()
        initView()
        play(intent)
    }

    private fun play(intent: Intent) {
        val index = intent.getIntExtra("index", -1)
//        val anchor = intent.getParcelableExtra<Anchor>("anchor")
        val list = intent.getParcelableArrayListExtra<Anchor>("list")
        val anchor = list?.get(index)
        viewModel.preparePlay(anchor)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { play(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    private fun initView() {
        binding.playerView.apply {
            player = this@PlayerActivity.player
            useController = false
        }
        player.playWhenReady = true
    }

    private fun observeLiveData() {
        viewModel.apply {
            streamLink.observe(this@PlayerActivity) {
                play(it)
            }
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
        }
    }

    /**
     * 播放流
     */
    private fun play(url: String) {
        player.stop(true)
        MainExecutor.execute {
            try {
                Log.d("playAnchorSteaming", url)
                if (url.isEmpty()) {
                    AppUtil.runOnUiThread {
                        player.stop(true)
//                        showPlayFailedMsg("直播流为空")
                        ToastUtil.toast("直播流为空")
                    }
                    return@execute
                }
                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                    MyApplication.application,
                    Util.getUserAgent(MyApplication.application, "com.acel.streamlivetool")
                )
                val uri = Uri.parse(url)
                val videoSource: MediaSource

                videoSource = if (url.contains(".m3u8"))
                    HlsMediaSource.Factory(dataSourceFactory).setAllowChunklessPreparation(true)
                        .createMediaSource(uri)
                else
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri)
                AppUtil.runOnUiThread {
                    player.prepare(videoSource)
                }
            } catch (e: Exception) {
                AppUtil.runOnUiThread {
                    player.stop(true)
//                    showPlayFailedMsg("获取直播流失败")
                    ToastUtil.toast("获取直播流失败：${e.javaClass.name}")
                }
                e.printStackTrace()
            }
        }
    }
}