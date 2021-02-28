package com.acel.streamlivetool.ui.player

/**
 * @author acel
 * 用于显示/关闭前台通知
 */
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.acel.streamlivetool.service.PlayerService


@Suppress("unused")
class ForegroundListener : LifecycleObserver {

    private var isForeground = false

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        stopForeground()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        startForeground()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        stopForeground()
    }

    private fun startForeground() {
        synchronized(isForeground) {
            if (!isForeground) {
                PlayerService.startWithForeground(
                    PlayerService.Companion.SourceType.PLAYER_ACTIVITY
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