package com.acel.streamlivetool.ui.main.player

/**
 * @author acel
 * 用于显示/关闭前台通知
 */
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.service.PlayerService
import com.acel.streamlivetool.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Suppress("unused")
class PlayerServiceForegroundListener(mainActivity: MainActivity) : LifecycleObserver {
    private var avatarBitmap: Bitmap? = null
    private var mainActivity: MainActivity? = mainActivity
    private var isForeground = false
    private var notificationActionReceiver: NotificationActionReceiver? =
        NotificationActionReceiver()
    private var playerStatusReceiver: PlayerStatusReceiver? = PlayerStatusReceiver()

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
        avatarBitmap = null
        notificationActionReceiver = null
        playerStatusReceiver = null
    }

    private fun startForeground() {
        synchronized(isForeground) {
            if (!isForeground) {
                mainActivity?.getPlayingAnchor()?.let {
                    mainActivity?.lifecycleScope?.launch(Dispatchers.IO) {
                        avatarBitmap = it.avatar?.let { it1 ->
                            ImageLoader.getDrawable(mainActivity!!, it1)?.toBitmap()
                        }
                        avatarBitmap?.let { it1 ->
                            PlayerService.startWithForeground(
                                PlayerService.Companion.SourceType.PLAYER_FRAGMENT, it, it1
                            )
                            notificationActionReceiver?.register()
                            playerStatusReceiver?.register()
                        }
                    }
                }
            }
            isForeground = true
        }
    }

    @Synchronized
    private fun stopForeground() {
        if (isForeground) {
            PlayerService.stopForegroundService()
            notificationActionReceiver?.unregister()
            playerStatusReceiver?.unregister()
            isForeground = false
        }
    }

    private fun updateForeground(playOrPause: Int? = null) {
        mainActivity?.getPlayingAnchor()?.let {
            avatarBitmap?.let { it1 ->
                PlayerService.startWithForeground(
                    PlayerService.Companion.SourceType.PLAYER_FRAGMENT, it, it1, playOrPause
                )
            }
        }
    }

    inner class NotificationActionReceiver : BroadcastReceiver() {
        private var isRegistered = false
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.getIntExtra(PlayerService.KEY_PLAY_OR_PAUSE, -1)
            if (action != -1 && isForeground) {
                val changeAction = when (action) {
                    PlayerService.ACTION_PLAY ->
                        PlayerViewModel.PLAYER_STATUS_PLAY
                    PlayerService.ACTION_PAUSE ->
                        PlayerViewModel.PLAYER_STATUS_PAUSE
                    PlayerService.ACTION_STOP ->
                        PlayerViewModel.PLAYER_STATUS_STOP
                    else -> -1
                }

                if (changeAction != -1) {
                    MyApplication.application.sendBroadcast(
                        Intent(PlayerViewModel.BROADCAST_CHANGE_PLAYER_STATUS).apply {
                            putExtra(PlayerViewModel.KEY_PLAYER_STATUS, changeAction)
                        }
                    )
                    if (changeAction == PlayerViewModel.PLAYER_STATUS_STOP)
                        stopForeground()
                }
            }
        }

        fun register() {
            if (!isRegistered) {
                mainActivity?.registerReceiver(
                    this,
                    IntentFilter(PlayerService.BROADCAST_CHANGE_PLAYER_STATUS_ACTION)
                )
                isRegistered = true
            }
        }

        fun unregister() {
            if (isRegistered) {
                mainActivity?.unregisterReceiver(this)
                isRegistered = false
            }
        }
    }

    inner class PlayerStatusReceiver : BroadcastReceiver() {
        private var isRegistered = false

        override fun onReceive(context: Context?, intent: Intent) {
            val status = intent.getIntExtra(PlayerViewModel.KEY_PLAYER_STATUS, -1)
            if (status != -1 && isForeground) {
                when (status) {
                    PlayerViewModel.PLAYER_STATUS_PAUSE -> {
                        updateForeground(PlayerService.ACTION_PAUSE)
                    }
                    PlayerViewModel.PLAYER_STATUS_PLAY -> {
                        updateForeground(PlayerService.ACTION_PLAY)
                    }
                }
            }
        }

        fun register() {
            if (!isRegistered) {
                mainActivity?.registerReceiver(
                    this,
                    IntentFilter(PlayerViewModel.BROADCAST_PLAYER_STATUS)
                )
                isRegistered = true
            }

        }

        fun unregister() {
            if (isRegistered) {
                mainActivity?.unregisterReceiver(this)
                isRegistered = false
            }
        }
    }
}