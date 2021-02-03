package com.acel.streamlivetool.ui.player

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.ToastUtil.toast
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    internal val player by lazy {
        SimpleExoPlayer.Builder(MyApplication.application).build()
    }.also {
        it.value.apply {
            playWhenReady = true
            addListener(object : Player.EventListener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    this@PlayerViewModel.isPlaying.postValue(isPlaying)
                }

                override fun onLoadingChanged(isLoading: Boolean) {
                    super.onLoadingChanged(isLoading)
                    this@PlayerViewModel.isPlaying.postValue(false)
                }

                override fun onPlayerError(error: ExoPlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()
                    errorMessage.postValue("播放失败。")
                }
            })
        }
    }

    val anchor = MutableLiveData<Anchor>()
    val anchorList = MutableLiveData(mutableListOf<Anchor>())
//    private val streamLink = MutableLiveData<String>()
    val isPlaying = MutableLiveData(false)
    val errorMessage = MutableLiveData("")
    internal fun setAnchorData(intent: Intent) {
        val index = intent.getIntExtra("index", -1)
        val list = intent.getParcelableArrayListExtra<Anchor>("list")
        anchorList.apply {
            if (list != null) {
                anchorList.value?.clear()
                anchorList.value?.addAll(list)
                anchorList.postValue(value)
            }
        }
        val anchor = list?.get(index)
        preparePlay(anchor)
    }

    private fun preparePlay(anchor: Anchor?) {
        if (anchor == this.anchor.value)
            return
        this.anchor.postValue(anchor)
        getStreamUrlAndPlay(anchor)
    }

    private fun getStreamUrlAndPlay(anchor: Anchor?) {
        anchor?.let { a ->
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    val url = PlatformDispatcher.getPlatformImpl(a)
                        ?.getStreamingLiveUrl(a)
                    if (url != null && url.isNotEmpty())
                        play(url)
                    else
                        errorMessage.postValue("获取直播流失败。（empty）")
                }.onFailure {
                    errorMessage.postValue("获取直播流失败。（error）")
                    it.printStackTrace()
                }
            }
        }
    }

    internal fun replay() {
        getStreamUrlAndPlay(anchor.value)
    }

    /**
     * 播放流
     */
    private fun play(url: String) {
        AppUtil.mainThread {
            player.stop(true)
        }
        viewModelScope.runCatching {
            if (url.isEmpty()) {
                AppUtil.mainThread {
                    player.stop(false)
                    toast("直播流为空")
                }
                return@runCatching
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
                            .createMediaSource(uri)
                    else ->
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(uri)
                }
            AppUtil.mainThread {
                player.prepare(videoSource)
            }
        }.onFailure {
            AppUtil.mainThread {
                player.stop(true)
            }
            it.printStackTrace()
        }

    }

    override fun onCleared() {
        super.onCleared()
        player.stop()
        player.release()
    }

    fun stopPlay() {
        player.stop()
    }
}