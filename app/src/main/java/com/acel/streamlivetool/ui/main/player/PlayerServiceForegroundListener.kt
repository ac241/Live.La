package com.acel.streamlivetool.ui.main.player

/**
 * @author acel
 * 用于显示/关闭前台通知
 */
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.service.PlayerService
import com.acel.streamlivetool.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


@Suppress("unused")
class PlayerServiceForegroundListener(mainActivity: MainActivity) : LifecycleObserver {
    private var mainActivity: MainActivity? = mainActivity
    private var isForeground = false

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        mainActivity?.apply {
            if (!isPlayerOverlayShown() && isPlayerFragmentShown())
                stopForeground()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        mainActivity?.apply {
            if (!isPlayerOverlayShown() && isPlayerFragmentShown())
                startForeground()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        stopForeground()
        mainActivity = null
    }

    private fun startForeground() {
        synchronized(isForeground) {
            if (!isForeground) {
                mainActivity?.getPlayingAnchor()?.let {
                    mainActivity?.lifecycleScope?.launch(Dispatchers.IO) {
                        val bitmap = it.avatar?.let { it1 ->
                            ImageLoader.getDrawable(mainActivity!!, it1)?.toBitmap()
                        }
                        if (bitmap != null) {
                            PlayerService.startWithForeground(
                                PlayerService.Companion.SourceType.PLAYER_FRAGMENT, it, bitmap
                            )
                        }
                    }
                }
            }
            isForeground = true
        }
    }

    private fun stopForeground() {
        if (isForeground) {
            PlayerService.stopForegroundService()
            isForeground = false
        }
    }
}