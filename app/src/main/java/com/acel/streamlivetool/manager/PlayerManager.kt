package com.acel.streamlivetool.manager

import android.net.Uri
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.ToastUtil
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoListener

class PlayerManager {
    /**
     * 播放器状态
     */
    enum class PlayerStatus { IDLE, PLAYING, LOADING, BUFFERING, PAUSE, ENDED, ERROR }

    private var videoResolution: Pair<Int, Int>? = null
    private var playerStatus = PlayerStatus.IDLE
    private var listener: Listener? = null

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    private fun setStatus(playerStatus: PlayerStatus, message: String? = null) {
        this.playerStatus = playerStatus
        listener?.onStatusChange(playerStatus, message)
    }

    private fun setResolution(width: Int, height: Int) {
        val resolution = Pair(width, height)
        videoResolution = resolution
        listener?.onResolutionChange(resolution)
    }

    fun pause() {
        setStatus(PlayerStatus.PAUSE)
        player.pause()
    }

    fun stop(reset: Boolean = false) {
        player.stop(reset)
    }

    fun release() {
        player.release()
    }

    fun isPlaying() = player.isPlaying

    /**
     * 播放器
     */
    internal val player by lazy {
        SimpleExoPlayer.Builder(MyApplication.application).build()
    }.also {
        it.value.apply {
            playWhenReady = true
            addListener(object : Player.EventListener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    if (isPlaying) {
                        setStatus(PlayerStatus.PLAYING)
                    }
                }

                override fun onIsLoadingChanged(isLoading: Boolean) {
                    super.onIsLoadingChanged(isLoading)
                    if (isLoading)
                        setStatus(PlayerStatus.LOADING)
                }

                override fun onPlayerError(error: ExoPlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()
                    setStatus(PlayerStatus.ERROR, "播放失败。")
                }

                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)
                    when (state) {
                        Player.STATE_BUFFERING ->
                            setStatus(PlayerStatus.BUFFERING)
                        Player.STATE_ENDED -> {
                            setStatus(PlayerStatus.ENDED)
                        }
                        Player.STATE_IDLE -> {
                        }
                        Player.STATE_READY -> {
                            if (playWhenReady)
                                setStatus(PlayerStatus.PAUSE)
                            else
                                setStatus(PlayerStatus.PLAYING)
                        }
                    }
                }
            })
            addVideoListener(object : VideoListener {
                override fun onVideoSizeChanged(
                    width: Int,
                    height: Int,
                    unappliedRotationDegrees: Int,
                    pixelWidthHeightRatio: Float
                ) {
                    setResolution(width, height)
                }
            })
        }
    }


    /**
     * 播放流
     */
    internal fun play(url: String) {
        AppUtil.mainThread {
            stop(true)
            player.playWhenReady = true
        }
        try {
            if (url.isEmpty()) {
                AppUtil.mainThread {
                    player.stop()
                    ToastUtil.toast("直播流为空")
                }
                return
            }
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                MyApplication.application,
                Util.getUserAgent(MyApplication.application, "com.acel.streamlivetool")
            )
            val uri = Uri.parse(url)
            val videoSource: MediaSource

            videoSource =
                when {
                    url.contains(".m3u8") ->
                        HlsMediaSource.Factory(dataSourceFactory)
                            .setAllowChunklessPreparation(true)
                            .createMediaSource(MediaItem.fromUri(uri))
                    else ->
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(uri))
                }
            AppUtil.mainThread {
                player.setMediaSource(videoSource)
                player.prepare()
            }
        } catch (e: Exception) {
            AppUtil.mainThread {
                stop(true)
            }
            setStatus(PlayerStatus.ERROR, "exception:${e.message}")
            e.printStackTrace()
        }

    }

    interface Listener {
        fun onStatusChange(playerStatus: PlayerStatus, message: String?)
        fun onResolutionChange(resolution: Pair<Int, Int>)
    }

}

