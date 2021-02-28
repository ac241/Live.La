package com.acel.streamlivetool.ui.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.Danmu
import com.acel.streamlivetool.bean.StreamingLive
import com.acel.streamlivetool.platform.PlatformDispatcher.platformImpl
import com.acel.streamlivetool.util.AnchorListUtil.removeGroup
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.AppUtil.mainThread
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.ToastUtil.toastOnMainThread
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.properties.Delegates

class PlayerViewModel : ViewModel() {

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
                        playerStatus.postValue(PlayerState.IS_PLAYING)
                        playEndedTimes = 0
                    }
                }

                override fun onIsLoadingChanged(isLoading: Boolean) {
                    super.onIsLoadingChanged(isLoading)
                    if (isLoading)
                        playerStatus.postValue(PlayerState.IS_LOADING)
                }

                override fun onPlayerError(error: ExoPlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()
                    playerMessage.postValue("播放失败。")
                    playerStatus.postValue(PlayerState.IS_ERROR)
                }

                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)
                    if (playbackState == Player.STATE_ENDED) {
                        playerStatus.postValue(PlayerState.IS_ENDED)
//                        playerMessage.postValue("播放结束。")
//                        stop()
                        playEndedTimes++
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
                    videoResolution.postValue(Pair(width, height))
                }
            })
        }
    }

    //连接弹幕失败次数
    var playEndedTimes: Int by Delegates.observable(0) { _, _, new ->
        if (new in (1..3)) {
            playerMessage.postValue("播放已断开尝试重连。")
            replay()
        }
        if (new > 3) {
            playerMessage.postValue("播放已经停止...$new")
            player.stop(true)
        }
    }


    //展示用的播放器信息
    val playerMessage = MutableLiveData("")
    val playerStatus = MutableLiveData(PlayerState.IS_IDLE)
    val isPlaying get() = playerStatus.value == PlayerState.IS_PLAYING
    val videoResolution = MutableLiveData(Pair(0, 0))

    val anchor = MutableLiveData<Anchor>()
    val anchorList = MutableLiveData(mutableListOf<Anchor>())

    //当前主播在列表中的位置
    val anchorPosition = MutableLiveData(-1)

    //更新主播详情
    val anchorDetails = MutableLiveData<Anchor>()

    //流质量
    val currentQuality = MutableLiveData<StreamingLive.Quality?>()
    val qualityList = MutableLiveData<List<StreamingLive.Quality?>>()

    val danmuList = MutableLiveData(Collections.synchronizedList(LinkedList<Danmu>()))
    val danmuStatus = MutableLiveData(Pair(DanmuState.IDLE, ""))

    //待发送的弹幕
    private val preEmitDanmuList: MutableList<Danmu> =
        Collections.synchronizedList(mutableListOf<Danmu>())

    //连接弹幕失败次数
    var danmuErrorTimes: Int by Delegates.observable(0) { _, _, new ->
        if (new in (1..3))
            restartDanmu("意外连接断开，尝试重连。")
        if (new > 3)
            danmuStatus.postValue(Pair(DanmuState.ERROR, "发生错误断开..."))
    }

    /**
     * 弹幕客户端
     */
    private val danmuClient = DanmuClient(viewModelScope).apply {
        setListener(object : DanmuClient.DanmuListener {
            override fun onNewDanmu(danmu: Danmu) {
                addDanmu(danmu)
            }

            override fun onConnecting(message: String) {
                mainThread {
                    danmuStatus.value = Pair(DanmuState.CONNECTING, message)
                }
            }

            override fun onStop(reason: String) {
                mainThread {
                    danmuStatus.value = Pair(DanmuState.STOP, "stop:$reason")
                }
            }

            override fun onError(reason: String, errorType: DanmuClient.ErrorType) {
                mainThread {
                    danmuStatus.value = Pair(DanmuState.ERROR, "fail:$reason")
                }
                if (errorType != DanmuClient.ErrorType.NOT_SUPPORT)
                    danmuErrorTimes++
            }

            override fun onStart() {
                mainThread {
                    danmuStatus.value = Pair(DanmuState.START, "弹幕链接成功")
                }
                danmuErrorTimes = 0
            }
        })
    }

    /**
     * 新增弹幕
     */
    private fun addDanmu(danmu: Danmu) {
        danmuList.value?.apply {
            if (size >= 200)
                removeFirst()
            danmuList.value?.add(danmu)
        }
        danmuList.postValue(danmuList.value)
        preEmitDanmuList.add(danmu)
    }

    /**
     * 播放器状态
     */
    enum class PlayerState {
        IS_IDLE,
        IS_PLAYING,
        IS_LOADING,
        IS_ENDED,
        IS_ERROR
    }

    /**
     * 弹幕状态
     */
    enum class DanmuState {
        IDLE, CONNECTING, RECONNECTING, START, STOP, ERROR, RELEASE
    }

    /**
     * 设置主播数据并且播放
     */
    internal fun setAnchorDataAndPlay(intent: Intent) {
        val index = intent.getIntExtra("index", -1)
        val list = intent.getParcelableArrayListExtra<Anchor>("list")
        anchorList.apply {
            if (list != null) {
                anchorList.value?.clear()
                val temp = removeGroup(list)
                anchorList.value?.apply {
                    addAll(temp)
                }
                anchorList.postValue(value)
            }
        }
        val anchor = list?.get(index) ?: return
        preparePlay(anchor)
    }

    /**
     * 准备播放
     */
    private fun preparePlay(anchor: Anchor) {
        if (anchor == this.anchor.value)
            return
        clearQualityInfo()
        startDanmu(anchor)
        this.anchor.postValue(anchor)
        anchorPosition.postValue(anchorList.value?.indexOf(anchor) ?: -1)
        getStreamUrlAndPlay(anchor)
    }

    /**
     * 清楚清晰度信息
     */
    private fun clearQualityInfo() {
        currentQuality.postValue(null)
        qualityList.postValue(null)
    }

    /**
     *  获取流并且播放
     */
    private fun getStreamUrlAndPlay(anchor: Anchor?, quality: StreamingLive.Quality? = null) {
        anchor?.let { anc ->
            stopPlay()
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    val streamingLive = anc.platformImpl()?.getStreamingLive(anc, quality)
                    val url = streamingLive?.url
                    if (url != null && url.isNotEmpty()) {
                        play(url)
                        currentQuality.postValue(streamingLive.currentQuality)
                        qualityList.postValue(streamingLive.qualityList)
                    } else {
                        playerStatus.postValue(PlayerState.IS_ERROR)
                        playerMessage.postValue("获取直播流失败。（empty）")
                        stopPlay()
                    }
                }.onFailure {
                    playerStatus.postValue(PlayerState.IS_ERROR)
                    playerMessage.postValue("获取直播流失败。（error）")
                    it.printStackTrace()
                    stopPlay()
                }
            }
        }
    }

    /**
     * 重新播放
     */
    internal fun replay() {
        currentQuality.value.let {
            if (it == null) {
                getStreamUrlAndPlay(anchor.value)
            } else
                playWithAssignQuality(it)
        }
    }

    /**
     * 播放流
     */
    private fun play(url: String) {
        mainThread {
            player.stop(true)
        }
        viewModelScope.runCatching {
            if (url.isEmpty()) {
                mainThread {
                    player.stop()
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
                            .createMediaSource(MediaItem.fromUri(uri))
                    else ->
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(uri))
                }
            mainThread {
                player.setMediaSource(videoSource)
                player.prepare()
            }
        }.onFailure {
            mainThread {
                player.stop(true)
            }
            it.printStackTrace()
        }

    }

    override fun onCleared() {
        player.stop()
        player.release()
        danmuClient.release()
        super.onCleared()
    }

    private fun stopPlay() {
        mainThread {
            player.stop(true)
        }
    }

    /**
     * 播放列表中指定位置的主播
     */
    fun playInList(position: Int) {
        anchorList.value?.get(position)?.let { preparePlay(it) }
    }

    internal fun startDanmu() = anchor.value?.let { startDanmu(it) }

    /**
     * 开启弹幕
     */
    private fun startDanmu(anchor: Anchor) {
        val result = danmuClient.start(anchor)
        if (result)
            clearDanmuList()
    }

    /**
     *  结束弹幕
     */
    fun stopDanmu(reason: String) {
        danmuClient.stop(reason)
    }

    /**
     * 清楚弹幕列表
     */
    private fun clearDanmuList() {
        danmuList.postValue(danmuList.value?.apply { clear() })
    }

    /**
     * 重启弹幕
     */
    fun restartDanmu(message: String) {
        danmuClient.restart(message)
    }

    /**
     * 获取主播信息，更新数据
     */
    fun getAnchorDetails(anchor: Anchor) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val a = anchor.platformImpl()?.getAnchor(anchor)
                if (a != null)
                    anchorDetails.postValue(a)
                else
                    toastOnMainThread("更新主播数据失败")
            }.onFailure {
                it.printStackTrace()
                toastOnMainThread("更新主播数据失败。")
            }
        }
    }

    /**
     * 以指定清晰度播放
     */
    fun playWithAssignQuality(quality: StreamingLive.Quality) {
        getStreamUrlAndPlay(anchor.value, quality)
    }

    /**
     * 打开当前主播对应的app直播间
     */
    fun startAppForCurrentAnchor(context: Context) {
        anchor.value?.let { AppUtil.startApp(context, it) }
    }

    fun stopAll() {
        stopPlay()
        stopDanmu("stop all")
    }

    /**
     * 获取待发送的弹幕
     */
    @Synchronized
    fun getPreEmitDanmu(): Danmu? {
        return if (preEmitDanmuList.size > 0) {
            val danmu = preEmitDanmuList[0]
            preEmitDanmuList.removeAt(0)
            danmu
        } else
            null
    }

}

