package com.acel.streamlivetool.ui.player

/**
 * @author acel
 * 用于显示/关闭前台通知
 */
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.acel.streamlivetool.service.PlayerService
import com.acel.streamlivetool.ui.main.MainActivity
import org.mozilla.javascript.tools.jsc.Main


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
                PlayerService.startWithForeground(
                    PlayerService.Companion.SourceType.PLAYER_FRAGMENT
                )
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