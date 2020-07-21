package com.acel.streamlivetool.ui.overlay

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.AppUtil.startApp
import com.acel.streamlivetool.util.ToastUtil
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class PlayerOverlayWindowManager {
    companion object {
        val instance by lazy { PlayerOverlayWindowManager() }
    }

    var nowAnchor: Anchor? = null
    var lastAnchor: Anchor? = null
    var isShown = false
    private val applicationContext: Context = MyApplication.application.applicationContext
    private val playerOverlayWindow: AbsOverlayWindow =
        PlayerOverlayWindow.instance.create().also { it.setMovable() }
    private val playerOverlayView: View? = playerOverlayWindow.getLayout()
    private val playerOverlay: SimpleExoPlayer? =
        SimpleExoPlayer.Builder(applicationContext).build()
    private val exoPlayerView: PlayerView? =
        playerOverlayView?.findViewById(R.id.btn_player_overlay_video_view)
    private val processBar: ProgressBar? =
        playerOverlayView?.findViewById(R.id.player_overlay_process_bar)

    init {
        exoPlayerView?.player = playerOverlay
        exoPlayerView?.useController = false
        playerOverlay?.playWhenReady = true
        playerOverlay?.addListener(object : com.google.android.exoplayer2.Player.EventListener {
            override fun onIsPlayingChanged(isPlaying: kotlin.Boolean) {
                if (isPlaying)
                    processBar?.visibility = android.view.View.GONE
                else
                    processBar?.visibility = android.view.View.VISIBLE
            }
        })

        //关闭按钮
        val btnClose =
            playerOverlayView?.findViewById<ImageView>(R.id.btn_player_overlay_close)
        btnClose?.setOnClickListener {
            remove()
        }
        //改变大小按钮
        val resizeBtn =
            playerOverlayView?.findViewById<ImageView>(R.id.btn_player_overlay_resize)
        resizeBtn?.setOnClickListener {
            (playerOverlayWindow as PlayerOverlayWindow).changeWindowSize(applicationContext)
        }
        //打开APP按钮
        val btnStartApp =
            playerOverlayView?.findViewById<ImageView>(R.id.btn_player_overlay_start_app)
        btnStartApp?.setOnClickListener {
            nowAnchor?.let { anchor ->
                startApp(applicationContext, anchor)
                remove()
            }
        }
    }


    /**
     * 创建Player悬浮窗
     */
    internal fun show() {
        playerOverlayWindow.show()
        isShown = true
    }

    /**
     * 移除Player悬浮窗
     */
    internal fun remove() {
        playerOverlay?.stop()
        playerOverlayWindow.remove()
        isShown = false

    }

    internal fun toggle() {
        if (isShown) {
            remove()
        } else {
            show()
        }
    }

    internal fun play(anchor: Anchor) {
        lastAnchor = nowAnchor
        nowAnchor = anchor
        if (isShown) {
            playAnchorSteaming(anchor)
        } else {
            show()
            playAnchorSteaming(anchor)
        }
    }

    /**
     * 播放流
     */
    private fun playAnchorSteaming(anchor: Anchor) {
        MainExecutor.execute {
            val url =
                PlatformDispatcher.getPlatformImpl(anchor.platform)
                    ?.getStreamingLiveUrl(anchor)
            if (url == null || url.isEmpty()) {
                nowAnchor = lastAnchor
                runOnUiThread {
                    ToastUtil.toast("bad stream url")
                    remove()
                }
                return@execute
            }
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                applicationContext,
                Util.getUserAgent(applicationContext, "com.acel.streamlivetool")
            )
            val uri = Uri.parse(url)
            val videoSource: MediaSource

            videoSource = if (url.contains(".m3u8"))
                HlsMediaSource.Factory(dataSourceFactory).setAllowChunklessPreparation(true)
                    .createMediaSource(uri)
            else
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uri)
            runOnUiThread {
                playerOverlay?.prepare(videoSource)
            }
        }
    }
}