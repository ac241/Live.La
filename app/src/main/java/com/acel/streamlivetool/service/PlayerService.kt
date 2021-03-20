/*
 * Copyright (c) 2020.
 * @author acel
 */

package com.acel.streamlivetool.service

import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.util.MainExecutor


//@RuntimePermissions
class PlayerService : Service() {
    private var anchor: Anchor? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        initNotificationChannel()
    }

    private fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).apply {
                //26以上设置channel
                val playerChannel =
                    NotificationChannel(
                        PLAYER_NOTIFICATION_CHANNEL_ID,
                        PLAYER_NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        enableLights(false) //如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
                        setShowBadge(false) //是否显示角标
                        setSound(null, null)
                        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    }
                val playerOverlayChannel =
                    NotificationChannel(
                        PLAYER_OVERLAY_NOTIFICATION_CHANNEL_ID,
                        PLAYER_OVERLAY_NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        enableLights(false) //如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
                        setShowBadge(false) //是否显示角标
                        setSound(null, null)
                        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    }
                createNotificationChannel(playerChannel)
                createNotificationChannel(playerOverlayChannel)
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        anchor = intent.getParcelableExtra(KEY_ANCHOR)
        val source = intent.getSerializableExtra(KEY_SOURCE) as SourceType? ?: SourceType.NULL
        var playOrPause: Int? = null
        if (source == SourceType.PLAYER_FRAGMENT)
            playOrPause = intent.getIntExtra(KEY_PLAY_OR_PAUSE, ACTION_PLAY)
        setForeground(source, playOrPause)
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 前台服务
     */
    private fun setForeground(source: SourceType, playOrPause: Int?) {
        startForeground(
            source.getNotificationId(), foreGroundNotification(source, playOrPause)
        )
    }

    private val playerRemoteView = RemoteViews(
        MyApplication.application.packageName,
        R.layout.layout_player_notification
    )

    private fun foreGroundNotification(sourceType: SourceType, playOrPause: Int?): Notification? {
        val intent = when (sourceType) {
            SourceType.PLAYER_FRAGMENT ->
                Intent(this, SourceType.PLAYER_FRAGMENT.getActivityClass()).apply {
                    action = MainActivity.ACTION_OPEN_FRAGMENT
                    putExtra(
                        MainActivity.EXTRA_KEY_OPEN_FRAGMENT,
                        MainActivity.EXTRA_VALUE_OPEN_PLAYER_FRAGMENT
                    )
                }
            else -> Intent()
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            FLAG_UPDATE_CURRENT
        )
        val builder = when (sourceType) {
            SourceType.PLAYER_FRAGMENT ->
                NotificationCompat.Builder(this, PLAYER_NOTIFICATION_CHANNEL_ID).apply {
                    setSmallIcon(R.mipmap.ic_launcher)

                    playerRemoteView.setTextViewText(R.id.nickname, anchor?.nickname)
                    playerRemoteView.setTextViewText(R.id.title, anchor?.title)
                    playerRemoteView.setImageViewBitmap(R.id.avatar, tempStoragePlayingAnchorAvatar)
                    if (playOrPause != null && playOrPause != ACTION_PAUSE) {
                        playerRemoteView.setImageViewResource(
                            R.id.btn_play_or_pause,
                            R.drawable.ic_baseline_pause_24
                        )
                    } else {
                        playerRemoteView.setImageViewResource(
                            R.id.btn_play_or_pause,
                            R.drawable.ic_baseline_play_arrow_24
                        )
                    }
                    setCustomContentView(playerRemoteView)

                    setBtnOnClickListener(playOrPause)
                }
            SourceType.PLAYER_OVERLAY ->
                NotificationCompat.Builder(this, PLAYER_OVERLAY_NOTIFICATION_CHANNEL_ID).apply {
                    setLargeIcon(tempStorageOverlayPlayingAnchorAvatar)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setSmallIcon(
                            IconCompat.createWithBitmap(tempStorageOverlayPlayingAnchorAvatar)
                        )
                    } else
                        setSmallIcon(R.mipmap.ic_launcher)
                    setContentText("${anchor?.nickname} ${sourceType.getName()}播放中")
                    setContentIntent(pendingIntent)
                }
            else -> NotificationCompat.Builder(this, PLAYER_NOTIFICATION_CHANNEL_ID)
        }

        return builder.build()
    }

    private fun setBtnOnClickListener(playOrPause: Int?) {
        val intent =
            Intent(BROADCAST_CHANGE_PLAYER_STATUS_ACTION).putExtra(
                KEY_PLAY_OR_PAUSE,
                when (playOrPause) {
                    ACTION_PLAY -> ACTION_PAUSE
                    else -> ACTION_PLAY
                }
            )

        val pendingChangePlayerStatus =
            PendingIntent.getBroadcast(
                MyApplication.application, REQUEST_CODE_PLAY_OR_PAUSE, intent, FLAG_UPDATE_CURRENT
            )
        playerRemoteView.setOnClickPendingIntent(
            R.id.btn_play_or_pause,
            pendingChangePlayerStatus
        )

        intent.putExtra(KEY_PLAY_OR_PAUSE, ACTION_STOP)

        val pendingClose =
            PendingIntent.getBroadcast(
                MyApplication.application, REQUEST_CODE_CLOSE, intent, FLAG_UPDATE_CURRENT
            )

        playerRemoteView.setOnClickPendingIntent(
            R.id.btn_close,
            pendingClose
        )
    }

    companion object {
        var tempStoragePlayingAnchorAvatar: Bitmap? = null
        var tempStorageOverlayPlayingAnchorAvatar: Bitmap? = null

        enum class SourceType(val value: Int) {
            NULL(-1),
            PLAYER_FRAGMENT(1),
            PLAYER_OVERLAY(2);

            fun getActivityClass(): Class<out Activity>? {
                return when (this) {
                    PLAYER_FRAGMENT -> MainActivity::class.java
                    else -> null
                }
            }

            fun getName(): String {
                return when (this) {
                    PLAYER_FRAGMENT -> ""
                    PLAYER_OVERLAY -> "悬浮窗"
                    else -> ""
                }
            }

            fun getNotificationId(): Int {
                return when (this) {
                    PLAYER_FRAGMENT -> PLAYER_FRAGMENT_NOTIFICATION_ID
                    PLAYER_OVERLAY -> PLAYER_OVERLAY_NOTIFICATION_ID
                    else -> PLAYER_DEFAULT_NOTIFICATION_ID
                }
            }
        }

        private const val PLAYER_DEFAULT_NOTIFICATION_ID: Int = 2000
        private const val PLAYER_FRAGMENT_NOTIFICATION_ID: Int = 2001
        private const val PLAYER_OVERLAY_NOTIFICATION_ID: Int = 2002
        private const val PLAYER_NOTIFICATION_CHANNEL_ID: String = "player_foreground_channel_id"
        private const val PLAYER_NOTIFICATION_CHANNEL_NAME: String = "播放器前台服务"
        private const val PLAYER_OVERLAY_NOTIFICATION_CHANNEL_ID: String =
            "player_overlay_notification_channel_id"
        private const val PLAYER_OVERLAY_NOTIFICATION_CHANNEL_NAME: String = "悬浮窗前台服务"

        const val ACTION_PLAY: Int = 1
        const val ACTION_PAUSE: Int = 2
        const val ACTION_STOP: Int = 3
        private const val REQUEST_CODE_PLAY_OR_PAUSE = 7
        private const val REQUEST_CODE_CLOSE = 8

        private const val KEY_ANCHOR = "key_anchor"
        private const val KEY_SOURCE = "key_source"
        const val KEY_PLAY_OR_PAUSE: String = "key_play_or_pause"
//        private const val KEY_PLAY_OR_PAUSE = "key_play_or_pause"

        const val BROADCAST_CHANGE_PLAYER_STATUS_ACTION = "broadcast_change_player_status_action"

        @JvmStatic
        fun startWithForeground(
            type: SourceType?,
            anchor: Anchor,
            avatar: Bitmap,
            playOrPause: Int? = null
        ) {
            val intent = Intent(MyApplication.application, PlayerService::class.java)
            intent.putExtra(KEY_ANCHOR, anchor)
            when (type) {
                SourceType.PLAYER_FRAGMENT -> {
                    tempStoragePlayingAnchorAvatar = avatar
                    intent.putExtra(KEY_SOURCE, SourceType.PLAYER_FRAGMENT)
                    intent.putExtra(KEY_SOURCE, SourceType.PLAYER_FRAGMENT)
                    if (playOrPause != null)
                        intent.putExtra(KEY_PLAY_OR_PAUSE, playOrPause)
                }
                SourceType.PLAYER_OVERLAY -> {
                    tempStorageOverlayPlayingAnchorAvatar = avatar
                    intent.putExtra(KEY_SOURCE, SourceType.PLAYER_OVERLAY)
                }
                else -> {
                }
            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(intent)
//            } else
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                MyApplication.application.startForegroundService(intent)
            else
                MyApplication.application.startService(intent)
        }

        @JvmStatic
        fun stopForegroundService() {
            MainExecutor.execute {
                Thread.sleep(1000)
                val intent = Intent(MyApplication.application, PlayerService::class.java)
                MyApplication.application.stopService(intent)
            }
        }
    }
}