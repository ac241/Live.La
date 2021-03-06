package com.acel.streamlivetool.ui.main.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.Danmu
import com.acel.streamlivetool.bean.StreamingLive
import com.acel.streamlivetool.manager.PlayerManager
import com.acel.streamlivetool.platform.PlatformDispatcher.platformImpl
import com.acel.streamlivetool.util.AnchorListUtil.removeSection
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.AppUtil.mainThread
import com.acel.streamlivetool.util.ToastUtil.toastOnMainThread
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.properties.Delegates

class PlayerViewModel : ViewModel() {

    companion object {
        const val BROADCAST_PLAYER_STATUS = "broadcast_player_action"
        const val BROADCAST_CHANGE_PLAYER_STATUS = "broadcast_change_player_status"
        const val KEY_PLAYER_STATUS = "KEY_PLAYER_STATUS"
        const val PLAYER_STATUS_PLAY = 5
        const val PLAYER_STATUS_PAUSE = 6
        const val PLAYER_STATUS_STOP = 7
    }

    private var changePlayerStatusReceiver: ChangePlayerStatusReceiver? =
        ChangePlayerStatusReceiver().apply {
            register()
        }

    //----------主播相关------------
    val anchor = MutableLiveData<Anchor?>()
    val anchorList = MutableLiveData(mutableListOf<Anchor>())

    //当前主播在列表中的位置
    val anchorPosition = MutableLiveData(-1)

    //更新主播详情
    val anchorDetails = MutableLiveData<Anchor?>()


    //----------播放器相关------------
    /**
     * 播放器状态
     */
    enum class PlayerStatus { IDLE, PLAYING, LOADING, BUFFERING, PAUSE, ENDED, ERROR }

    private val playerManager = PlayerManager().apply {
        setListener(object : PlayerManager.Listener {
            override fun onStatusChange(
                playerStatus: PlayerManager.PlayerStatus,
                message: String?
            ) {
                when (playerStatus) {
                    PlayerManager.PlayerStatus.BUFFERING -> {
                        this@PlayerViewModel.playerStatus.postValue(PlayerStatus.BUFFERING)
                    }
                    PlayerManager.PlayerStatus.IDLE -> {
                        this@PlayerViewModel.playerStatus.postValue(PlayerStatus.IDLE)
                    }
                    PlayerManager.PlayerStatus.PLAYING -> {
                        playEndedTimes = 0
                        this@PlayerViewModel.playerStatus.postValue(PlayerStatus.PLAYING)
                        sendStatusBroadcast(PLAYER_STATUS_PLAY)
                    }
                    PlayerManager.PlayerStatus.LOADING -> {
                        this@PlayerViewModel.playerStatus.postValue(PlayerStatus.LOADING)
                    }
                    PlayerManager.PlayerStatus.PAUSE -> {
                        this@PlayerViewModel.playerStatus.postValue(PlayerStatus.PAUSE)
                        sendStatusBroadcast(PLAYER_STATUS_PAUSE)
                    }
                    PlayerManager.PlayerStatus.ENDED -> {
                        playEndedTimes++
                        this@PlayerViewModel.playerStatus.postValue(PlayerStatus.ENDED)
                    }
                    PlayerManager.PlayerStatus.ERROR -> {
                        this@PlayerViewModel.playerStatus.postValue(PlayerStatus.ERROR)
                        if (message != null)
                            playerMessage.postValue(message)
                        sendStatusBroadcast(PLAYER_STATUS_STOP)
                    }
                }
            }

            override fun onResolutionChange(resolution: Pair<Int, Int>) {
                videoResolution.postValue(resolution)
            }
        })
    }

    private fun sendStatusBroadcast(status: Int) {
        val intent = Intent(BROADCAST_PLAYER_STATUS).apply {
            putExtra(KEY_PLAYER_STATUS, status)
        }
        MyApplication.application.sendBroadcast(intent)
    }

    val player: Player = playerManager.player

    //展示用的播放器信息
    val playerMessage = MutableLiveData("")
    val playerStatus = MutableLiveData(PlayerStatus.IDLE)
    val isPlaying get() = playerStatus.value == PlayerStatus.PLAYING
    val videoResolution = MutableLiveData(Pair(0, 0))

    //流质量
    val currentQuality = MutableLiveData<StreamingLive.Quality?>()
    val qualityList = MutableLiveData<List<StreamingLive.Quality?>>()

    //连接弹幕失败次数
    var playEndedTimes: Int by Delegates.observable(0) { _, _, new ->
        if (new in (1..3)) {
            playerMessage.postValue("播放已断开尝试重连。")
//            setMessage("播放已断开，尝试重连中...")
            replay()
        }
        if (new > 3) {
            playerMessage.postValue("播放已经停止...$new")
//            setMessage("播放已经停止...$new")
            playerManager.stop(true)
            sendStatusBroadcast(PLAYER_STATUS_STOP)
        }
    }

    //----------弹幕相关------------
    /**
     * 弹幕状态
     */
    enum class DanmuState {
        IDLE, CONNECTING, START, STOP, ERROR
    }

    /**
     * 弹幕客户端
     */
    private val danmuManager = DanmuManager(viewModelScope).apply {
        setListener(object : DanmuManager.DanmuListener {
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

            override fun onError(reason: String, errorType: DanmuManager.ErrorType) {
                mainThread {
                    danmuStatus.value = Pair(DanmuState.ERROR, "fail:$reason")
                }
                if (errorType == DanmuManager.ErrorType.NORMAL)
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

    val danmuList = MutableLiveData(Collections.synchronizedList(LinkedList<Danmu>()))
    val danmuStatus = MutableLiveData(Pair(DanmuState.IDLE, ""))

    //待发送的弹幕
    private val preEmitDanmuList: MutableList<Danmu> =
        Collections.synchronizedList(mutableListOf<Danmu>())

    //连接弹幕失败次数
    private var danmuErrorTimes: Int by Delegates.observable(0) { _, _, new ->
        if (new in (1..3))
            restartDanmu("连接失败，尝试重连...$new")
        if (new > 3)
            danmuStatus.postValue(Pair(DanmuState.ERROR, "重连失败，已断开..."))
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
     * 设置主播数据并且播放
     */
    internal fun setAnchorDataAndPlay(anchor: Anchor, list: List<Anchor>?) {
        resetData()
        anchorList.apply {
            if (list != null) {
                anchorList.value?.clear()
                val temp = removeSection(list)
                anchorList.value?.apply {
                    addAll(temp)
                }
                anchorList.postValue(value)
            }
        }
//        val anchor = list?.get(index) ?: return
        preparePlay(anchor)
    }

    private fun resetData() {
        playerManager.stop(true)
        playerMessage.value = ""
        playerStatus.value = PlayerStatus.IDLE
        anchor.value = null
        videoResolution.value = Pair(0, 0)
        anchor.value = null
        anchorList.value = anchorList.value?.apply { clear() }
        anchorPosition.value = -1
        anchorDetails.value = null
        currentQuality.value = null
        qualityList.value = null
        danmuList.value = danmuList.value?.apply { clear() }
        danmuStatus.value = Pair(DanmuState.IDLE, "")
        preEmitDanmuList.clear()
        danmuErrorTimes = 0
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
                    val streamingLive =
                        anc.platformImpl()?.streamingLiveModule?.getStreamingLive(anc, quality)
                    val url = streamingLive?.url
                    if (url != null && url.isNotEmpty()) {
                        playerManager.play(url)
                        currentQuality.postValue(streamingLive.currentQuality)
                        qualityList.postValue(streamingLive.qualityList)
                    } else {
                        playerStatus.postValue(PlayerStatus.ERROR)
                        playerMessage.postValue("获取直播流失败。（empty）")
                        stopPlay()
                    }
                }.onFailure {
                    playerStatus.postValue(PlayerStatus.ERROR)
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

    override fun onCleared() {
        playerManager.stop()
        playerManager.release()
        danmuManager.release()
        changePlayerStatusReceiver?.unregister()
        changePlayerStatusReceiver = null
        super.onCleared()
    }

    private fun stopPlay() {
        mainThread {
            playerManager.stop(true)
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
        val result = danmuManager.start(anchor)
        if (result)
            clearDanmuList()
    }

    /**
     *  结束弹幕
     */
    fun stopDanmu(reason: String) {
        danmuManager.stop(reason)
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
        danmuManager.restart(message)
    }

    /**
     * 获取主播信息，更新数据
     */
    fun getAnchorDetails(anchor: Anchor) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val a = anchor.platformImpl()?.anchorModule?.getAnchor(anchor)
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

    inner class ChangePlayerStatusReceiver : BroadcastReceiver() {
        private var isRegistered = false
        override fun onReceive(context: Context?, intent: Intent) {
            val status = intent.getIntExtra(KEY_PLAYER_STATUS, -1)
            if (status == -1)
                return
            when (status) {
                PLAYER_STATUS_PLAY -> {
                    if (!playerManager.isPlaying())
                        replay()
                }
                PLAYER_STATUS_PAUSE -> {
                    stopPlay()
                    sendStatusBroadcast(PLAYER_STATUS_PAUSE)
                }
                PLAYER_STATUS_STOP -> {
                    stopPlay()
                }
            }
        }

        fun register() {
            if (!isRegistered) {
                MyApplication.application.registerReceiver(
                    this,
                    IntentFilter(BROADCAST_CHANGE_PLAYER_STATUS)
                )
                isRegistered = true
            }
        }

        fun unregister() {
            if (isRegistered) {
                MyApplication.application.unregisterReceiver(this)
                isRegistered = false
            }
        }
    }

}

