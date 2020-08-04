package com.acel.streamlivetool.ui.overlay

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.AppUtil.startApp
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.ToastUtil
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoListener
import kotlinx.android.synthetic.main.layout_player_overlay.view.*


class PlayerOverlayWindowManager {
    companion object {
        val instance by lazy { PlayerOverlayWindowManager() }
    }

    private val defaultSizeBigger = 240F
    private val defaultSizeSmaller = 135F
    private val sizeMultipleList = listOf(1F, 1.4F, 1.7F)
    private var nowSizeMultiple = sizeMultipleList[0]
    private var nowResolution: Pair<Float, Float> = Pair(defaultSizeBigger, defaultSizeSmaller)
    private var nowAnchor: Anchor? = null
    private var lastAnchor: Anchor? = null
    private var isShown = false
    private val playerOverlayWindow: AbsOverlayWindow =
        PlayerOverlayWindow.instance.create().also { it.setMovable() }
    private val containerView: View? = playerOverlayWindow.getLayout()
    private val exoPlayerView: PlayerView? =
        containerView?.findViewById(R.id.btn_player_overlay_video_view)
    private val processBar: ProgressBar? =
        containerView?.findViewById(R.id.player_overlay_process_bar)
    private val player: SimpleExoPlayer? =
        SimpleExoPlayer.Builder(MyApplication.application).build()

    init {
        exoPlayerView?.player = player
        exoPlayerView?.useController = false
        player?.playWhenReady = true
        player?.apply {
            addListener(object : com.google.android.exoplayer2.Player.EventListener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying)
                        processBar?.visibility = View.GONE
                    else
                        processBar?.visibility = View.VISIBLE
                }
            })
            addVideoListener(object : VideoListener {
                override fun onVideoSizeChanged(
                    width: Int,
                    height: Int,
                    unappliedRotationDegrees: Int,
                    pixelWidthHeightRatio: Float
                ) {
                    super.onVideoSizeChanged(
                        width,
                        height,
                        unappliedRotationDegrees,
                        pixelWidthHeightRatio
                    )
                    val land = width > height
                    val newWidth: Float
                    val newHeight: Float
                    if (land) {
                        newWidth = defaultSizeBigger
                        newHeight = height / (width / defaultSizeBigger)
                    } else {
                        newWidth = defaultSizeSmaller
                        newHeight = height / (width / defaultSizeSmaller)
                    }
                    changeWindowSize(
                        MyApplication.application,
                        newWidth * nowSizeMultiple,
                        newHeight * nowSizeMultiple
                    )
                    nowResolution = Pair(newWidth, newHeight)
                }
            }

            )
        }

        containerView?.setOnClickListener {
            containerView.controllerView.apply {
                when (visibility) {
                    View.VISIBLE ->
                        visibility = View.GONE
                    View.GONE ->
                        visibility = View.VISIBLE
                }
            }
        }

        //关闭按钮
        val btnClose =
            containerView?.findViewById<ImageView>(R.id.btn_player_overlay_close)
        btnClose?.setOnClickListener {
            remove()
        }
        //改变大小按钮
        val resizeBtn =
            containerView?.findViewById<ImageView>(R.id.btn_player_overlay_resize)
        resizeBtn?.setOnClickListener {
            changeWindowSizeMultiple()
        }
        //打开APP按钮
        val btnStartApp =
            containerView?.findViewById<ImageView>(R.id.btn_player_overlay_start_app)
        btnStartApp?.setOnClickListener {
            nowAnchor?.let { anchor ->
                startApp(MyApplication.application, anchor)
                remove()
            }
        }
    }

    /**
     *  改变窗口大小倍数
     */
    private fun changeWindowSizeMultiple() {
        val nowIndex = sizeMultipleList.indexOf(nowSizeMultiple)
        nowSizeMultiple = if (nowIndex < sizeMultipleList.size - 1) sizeMultipleList[nowIndex + 1]
        else sizeMultipleList[0]
        changeWindowSize(
            MyApplication.application,
            nowResolution.first * nowSizeMultiple,
            nowResolution.second * nowSizeMultiple
        )

    }

    private fun changeWindowSize(context: Context, width: Float, height: Float) {
        (playerOverlayWindow as PlayerOverlayWindow).changeWindowSize(
            context, width, height
        )
    }


    /**
     * 创建Player悬浮窗
     */
    private fun show() {
        playerOverlayWindow.show()
        isShown = true
    }

    /**
     * 移除Player悬浮窗
     */
    private fun remove() {
        player?.stop()
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
            Log.d("playAnchorSteaming", "$url")
            if (url == null || url.isEmpty()) {
                nowAnchor = lastAnchor
                runOnUiThread {
                    ToastUtil.toast("bad stream url")
                    remove()
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
            runOnUiThread {
                player?.prepare(videoSource)
            }
        }
    }
}