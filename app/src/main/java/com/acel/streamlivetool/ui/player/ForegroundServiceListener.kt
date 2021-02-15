package com.acel.streamlivetool.ui.player

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.acel.streamlivetool.service.PlayerService


@Suppress("unused")
class ForegroundServiceListener(activity: PlayerActivity) : LifecycleObserver {

    private var activity: PlayerActivity? = activity
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
        activity = null
    }

    private fun startForeground() {
        synchronized(isForeground){
        if (!isForeground) {
            activity?.apply {
//            if (viewModel.isPlaying()) {
                PlayerService.startWithForeground(
                    this,
                    PlayerService.Companion.SourceType.PLAYER_ACTIVITY
                )
//            }
            }
            isForeground = true
        }
        }
    }

    private fun stopForeground() {
        if (isForeground) {
            activity?.apply {
                PlayerService.stopForegroundService(this)
            }
            isForeground = false
        }
    }
}