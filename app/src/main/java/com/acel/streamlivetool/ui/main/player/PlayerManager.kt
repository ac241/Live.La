package com.acel.streamlivetool.ui.main.player

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

    private fun setStatus(playerStatus: PlayerStatus) {
        this.playerStatus = playerStatus
        listener?.onStatusChange(playerStatus)
    }

    private fun setMessage(message: String) {
        listener?.onMessage(message)
    }

    private fun setResolution(width: Int, height: Int) {
        val resolution = Pair(width, height)
        videoResolution = resolution
        listener?.onResolutionChange(resolution)
    }

    fun stop(reset: Boolean = false) {
        player.stop(reset)
    }

    fun release() {
        player.release()
    }

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
//                        playerStatus.postValue(PlayerViewModel.PlayerState.PLAYING)
                        setStatus(PlayerStatus.PLAYING)
                    }
                }

                override fun onIsLoadingChanged(isLoading: Boolean) {
                    super.onIsLoadingChanged(isLoading)
                    if (isLoading)
                        setStatus(PlayerStatus.LOADING)
//                        playerStatus.postValue(PlayerViewModel.PlayerState.LOADING)
                }

                override fun onPlayerError(error: ExoPlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()
//                    playerMessage.postValue("播放失败。")
                    setMessage("播放失败。")
                    setStatus(PlayerStatus.ERROR)
//                    playerStatus.postValue(PlayerViewModel.PlayerState.ERROR)
                }

                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)
                    when (state) {
                        Player.STATE_BUFFERING ->
                            setStatus(PlayerStatus.BUFFERING)
                        Player.STATE_ENDED -> {
                            setStatus(PlayerStatus.ENDED)
//                            playerStatus.postValue(PlayerViewModel.PlayerState.ENDED)
//                        playerMessage.postValue("播放结束。")
//                        stop()
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
//                    videoResolution.postValue(Pair(width, height))
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
            setStatus(PlayerStatus.ERROR)
            setMessage("exception:${e.message}")
            e.printStackTrace()
        }

    }

    interface Listener {
        fun onStatusChange(playerStatus: PlayerStatus)
        fun onMessage(message: String)
        fun onResolutionChange(resolution: Pair<Int, Int>)
    }

}

