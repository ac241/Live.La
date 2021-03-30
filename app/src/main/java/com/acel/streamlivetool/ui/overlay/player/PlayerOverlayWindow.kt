package com.acel.streamlivetool.ui.overlay.player

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.manager.PlayerManager
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.platform.PlatformDispatcher.getIconDrawable
import com.acel.streamlivetool.platform.PlatformDispatcher.platformImpl
import com.acel.streamlivetool.service.PlayerService
import com.acel.streamlivetool.ui.overlay.AbsOverlayWindow
import com.acel.streamlivetool.util.AnchorClickAction
import com.acel.streamlivetool.util.ToastUtil.toast
import kotlinx.android.synthetic.main.layout_overlay_player.view.*
import kotlinx.android.synthetic.main.layout_overlay_player_controller_view.view.*
import kotlinx.coroutines.*

class PlayerOverlayWindow : AbsOverlayWindow() {

    private val displayPixels = run {
        val width = MyApplication.application.resources.displayMetrics.widthPixels
        val height = MyApplication.application.resources.displayMetrics.heightPixels
        if (width < height)
            Pair(width, height)
        else
            Pair(height, width)
    }

    private val sizeRateRange = arrayOf(0.50f, 0.65f, 0.80f, 1f)
    private var currentSizeRate = sizeRateRange[0]
    private fun nextSizeRate() {
        val index = sizeRateRange.indexOf(currentSizeRate)
        currentSizeRate = if (index == -1 || index >= sizeRateRange.size - 1)
            sizeRateRange[0]
        else
            sizeRateRange[index + 1]
    }

    private var videoResolution = Pair(1920, 1080)

    private val playerManager = PlayerManager().apply {
        setListener(object : PlayerManager.Listener {
            override fun onStatusChange(
                    playerStatus: PlayerManager.PlayerStatus,
                    message: String?
            ) {
                when (playerStatus) {
                    PlayerManager.PlayerStatus.BUFFERING -> {
                        rootView.progress_bar.visibility = View.VISIBLE
                    }
                    PlayerManager.PlayerStatus.IDLE -> {
                    }
                    PlayerManager.PlayerStatus.PLAYING -> {
                        rootView.progress_bar.visibility = View.INVISIBLE
                        rootView.error_msg.visibility = View.INVISIBLE
                        startForegroundService()
                    }
                    PlayerManager.PlayerStatus.LOADING -> {
                        rootView.progress_bar.visibility = View.VISIBLE
                    }
                    PlayerManager.PlayerStatus.PAUSE -> {
                    }
                    PlayerManager.PlayerStatus.ENDED -> {
                        rootView.error_msg.apply {
                            visibility = View.VISIBLE
                            text = "播放已结束。"
                        }
                    }
                    PlayerManager.PlayerStatus.ERROR -> {
                        rootView.error_msg.apply {
                            visibility = View.VISIBLE
                            text = message
                        }
                    }
                }
            }

            override fun onResolutionChange(resolution: Pair<Int, Int>) {
                videoResolution = resolution
                resizeWindow()
            }
        })
    }

    private fun resizeWindow() {
        val landscape = videoResolution.first > videoResolution.second
        val width: Int
        val height: Int
        if (landscape) {
            width = (currentSizeRate * displayPixels.first).toInt()
            height = (width.toFloat() * videoResolution.second / videoResolution.first).toInt()
        } else {
            height = (currentSizeRate * displayPixels.first).toInt()
            width = (height.toFloat() * videoResolution.first / videoResolution.second).toInt()
        }
        resize(width, height)
    }

    private val anchorObserver = Observer<Anchor> { anchor ->
        getStreamingLiveAndPlay(anchor)
        rootView.controller_include.apply {
            overlay_controller_title.text = anchor.title
            overlay_controller_nickname.text = anchor.nickname
            btn_start_app.setImageDrawable(anchor.getIconDrawable())
        }
    }

    private val currentAnchor = MutableLiveData<Anchor>().also {
        it.observeForever(anchorObserver)
    }

    var anchorList: List<Anchor>? = null

    fun play(anchor: Anchor, list: List<Anchor>?) {
        show()
        anchorList = list
        currentAnchor.postValue(anchor)
    }

    private fun getStreamingLiveAndPlay(anchor: Anchor) {
        GlobalScope.launch(Dispatchers.IO) {
            val url = anchor.platformImpl()?.streamingLiveModule?.getStreamingLive(anchor)
                    ?: return@launch
            playUrl(url.url)
        }
    }

    private fun playUrl(url: String) = playerManager.play(url)

    @SuppressLint("InflateParams")
    override fun onCreateWindow(): View {
        return LayoutInflater.from(MyApplication.application)
                .inflate(R.layout.layout_overlay_player, null, false)
    }

    override fun onWindowHide() {
        playerManager.stop(true)
        PlayerService.stopForegroundService()
    }

    override fun onWindowShowed() {
        super.onWindowShowed()
        startForegroundService()
    }

    private fun startForegroundService() {
        currentAnchor.value?.let { anchor ->
            runBlocking {
                val bitmap = withContext(Dispatchers.IO) {
                    anchor.avatar?.let {
                        ImageLoader.getDrawable(MyApplication.application, it)?.toBitmap()
                    } ?: ResourcesCompat
                            .getDrawable(MyApplication.application.resources, R.mipmap.ic_launcher, null)
                            ?.toBitmap()
                }
                bitmap?.let {
                    PlayerService.startForegroundService(
                            PlayerService.Companion.SourceType.PLAYER_OVERLAY, anchor, it
                    )
                }
            }
        }
    }

    override fun onWindowCreated() {
        resizeWindow()
        rootView.apply {
            setOnClickListener {
                when (controller_include.visibility) {
                    View.INVISIBLE -> {
                        controller_include.visibility = View.VISIBLE
                    }
                    else -> {
                        controller_include.visibility = View.INVISIBLE
                    }
                }
            }

            btn_player_overlay_video_view.apply {
                player = playerManager.player
                useController = false
            }

            controller_include.apply {
                btn_close.setOnClickListener {
                    hide()
                }

                btn_resize.setOnClickListener {
                    nextSizeRate()
                    resizeWindow()
                }

                btn_start_app.setOnClickListener {
                    hide()
                    currentAnchor.value?.let { it1 ->
                        AnchorClickAction.startInnerPlayer(
                                MyApplication.application, it1
                        )
                    }
                }

                btn_previous.setOnClickListener {
                    previous()
                }
                btn_next.setOnClickListener {
                    next()
                }
                btn_replay.setOnClickListener {
                    it.animate().rotationBy(-360F).setDuration(1000L).start()
                    currentAnchor.value?.let { it1 -> getStreamingLiveAndPlay(it1) }
                }
            }

        }
    }

    private fun List<Anchor>.next(): Anchor? {
        currentAnchor.value?.let {
            val index = indexOf(it)
            return when {
                index == -1 -> null
                index >= size - 1 -> null
                else -> get(index + 1)
            }
        }
        return null
    }

    private fun List<Anchor>.previous(): Anchor? {
        currentAnchor.value?.let {
            val index = indexOf(it)
            return when {
                index == -1 -> null
                index <= 0 -> null
                else -> get(index - 1)
            }
        }
        return null
    }


    private fun next() {
        val anchor = anchorList?.next()
        if (anchor == null)
            toast("no more")
        else
            currentAnchor.postValue(anchor)
    }

    private fun previous() {
        val anchor = anchorList?.previous()
        if (anchor == null) {
            toast("no more")
        } else
            currentAnchor.postValue(anchor)
    }

    override fun release() {
        super.release()
        playerManager.release()
        currentAnchor.removeObserver(anchorObserver)
    }
}