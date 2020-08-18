package com.acel.streamlivetool.ui.overlay

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.MutableLiveData
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

    //横屏参数
    private val defaultWidthLand = 240F
    private val defaultHeightLand = 135F
    private val landSizeMultipleList = listOf(1F, 1.4F, 1.7F)

    //竖屏参数
    private val defaultWidthVertical = 135F
    private val defaultHeightVertical = 240F
    private val verticalMultipleList = listOf(1F, 1.7F, 2.5F)

    private var nowSizeMultipleIndex = 0
    private var nowResolution: Pair<Float, Float> = Pair(defaultWidthLand, defaultHeightLand)
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
    private val controllerView = containerView?.controllerView

    private var nowPlayList: List<Anchor>? = null
    private val nowAnchor = MutableLiveData<Anchor>().also {
        it.observeForever { anchor ->
            controllerView?.textView_controller_title?.text = anchor.nickname
            if (nowPlayList != null) {
                nowPlayList?.apply {
                    if (size > 1) {
                        when (indexOf(anchor)) {
                            -1 ->
                                hideListProgressController()
                            0 ->
                                playListFirstOne()
                            size - 1 ->
                                playListLastOne()
                            else ->
                                playListCenterOne()
                        }
                    } else {
                        hideListProgressController()
                    }
                }
            } else {
                hideListProgressController()
            }
        }
    }

    private fun playListCenterOne() {
        showListProgressController()
        controllerView?.btn_player_overlay_previous?.setImageResource(R.drawable.ic_controller_previous_enable)
        controllerView?.btn_player_overlay_next?.setImageResource(R.drawable.ic_controller_next_enable)
    }

    private fun playListLastOne() {
        showListProgressController()
        nextUnable()
    }

    private fun playListFirstOne() {
        showListProgressController()
        previousUnable()
    }

    private fun previousEnable() {
        controllerView?.btn_player_overlay_previous?.apply {
            setImageResource(R.drawable.ic_controller_previous_enable)
            isEnabled = true
        }
    }

    private fun previousUnable() {
        controllerView?.btn_player_overlay_previous?.apply {
            setImageResource(R.drawable.ic_controller_previous_unable)
            isEnabled = false
        }
    }

    private fun nextEnable() {
        controllerView?.btn_player_overlay_next?.apply {
            setImageResource(R.drawable.ic_controller_next_enable)
            isEnabled = true
        }
    }

    private fun nextUnable() {
        controllerView?.btn_player_overlay_next?.apply {
            setImageResource(R.drawable.ic_controller_next_unable)
            isEnabled = false
        }
    }


    private fun showListProgressController() {
        previousEnable()
        nextEnable()
        controllerView?.textView_controller_progress?.text =
            "${nowPlayList?.indexOf(nowAnchor.value)?.plus(1)}/${nowPlayList?.size}"
    }

    private fun hideListProgressController() {
        controllerView?.btn_player_overlay_next?.visibility = View.GONE
        controllerView?.btn_player_overlay_previous?.visibility = View.GONE
    }

    //控制器自动隐藏时间
    private val controllerHideTime = 3000L

    //上次控制器交互时间
    private var lastControllerInteraction = 0L

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
                    val defaultWidth = if (land) defaultWidthLand else defaultWidthVertical
                    newWidth = defaultWidth
                    newHeight = height / (width / defaultWidth)
                    nowResolution = Pair(newWidth, newHeight)
                    resizeWindowWithMultiple(
                        MyApplication.application,
                        newWidth,
                        newHeight
                    )
                }
            }
            )
        }

        containerView?.setOnClickListener {
            controllerView?.apply {
                when (visibility) {
                    View.VISIBLE ->
                        visibility = View.GONE
                    View.GONE -> {
                        visibility = View.VISIBLE
                        hideControllerDelay()
                    }
                }
            }
        }
        /**
         * 控制器功能
         */

        //关闭按钮
        controllerView?.btn_player_overlay_close?.setOnClickListener {
            hideControllerDelay()
            remove()
        }
        //改变大小按钮
        controllerView?.btn_player_overlay_resize?.setOnClickListener {
            hideControllerDelay()
            changeMultiple()
        }
        //打开APP按钮
        controllerView?.btn_player_overlay_start_app?.setOnClickListener {
            hideControllerDelay()
            nowAnchor.value?.let { anchor ->
                startApp(MyApplication.application, anchor)
                remove()
            }
        }
        //播放上一个anchor
        controllerView?.btn_player_overlay_previous?.setOnClickListener {
            playPrevious()
        }
        //播放下一个anchor
        controllerView?.btn_player_overlay_next?.setOnClickListener {
            playNext()
        }
    }

    private fun hideControllerDelay() {
        lastControllerInteraction = System.currentTimeMillis()
        containerView?.controllerView?.apply {
            postDelayed({
                if (System.currentTimeMillis() - lastControllerInteraction >= controllerHideTime - 20)
                    visibility = View.GONE
            }, controllerHideTime)
        }
    }

    /**
     *  改变窗口大小倍数
     */
    private fun changeMultiple() {
        nowSizeMultipleIndex =
            if (nowSizeMultipleIndex < landSizeMultipleList.size - 1)
                nowSizeMultipleIndex + 1
            else 0
        resizeWindowWithMultiple(
            MyApplication.application,
            nowResolution.first,
            nowResolution.second
        )
    }

    private fun resizeWindowWithMultiple(context: Context, width: Float, height: Float) {
        val land = nowResolution.first > nowResolution.second
        val multiple =
            if (land) landSizeMultipleList[nowSizeMultipleIndex]
            else verticalMultipleList[nowSizeMultipleIndex]
        resizeWindow(context, width * multiple, height * multiple)
    }

    private fun resizeWindow(context: Context, width: Float, height: Float) {
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
        containerView?.controllerView?.visibility = View.GONE
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

    private fun play(anchor: Anchor) {
        nowAnchor.postValue(anchor)
        if (isShown) {
            playAnchorSteaming(anchor)
        } else {
            show()
            playAnchorSteaming(anchor)
        }
    }

    internal fun playList(anchor: Anchor, list: List<Anchor>) {
        nowPlayList = list
        play(anchor)
    }

    private fun playPrevious() {
        nowPlayList?.apply {
            var nowIndex = this.indexOf(nowAnchor.value)
            if (nowIndex > 0) {
                play(this[--nowIndex])
            }
        }
    }

    private fun playNext() {
        nowPlayList?.apply {
            var nowIndex = this.indexOf(nowAnchor.value)
            if (nowIndex < size - 1) {
                play(this[++nowIndex])
            }
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
                runOnUiThread {
                    ToastUtil.toast("bad stream url")
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