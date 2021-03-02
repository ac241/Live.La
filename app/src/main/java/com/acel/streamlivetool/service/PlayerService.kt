/*
 * Copyright (c) 2020.
 * @author acel
 */

package com.acel.streamlivetool.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.util.MainExecutor


//@RuntimePermissions
class PlayerService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        initNotificationChannel()
    }

    private fun initNotificationChannel() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //26以上设置channel
                val playerChannel = NotificationChannel(
                    PLAYER_NOTIFICATION_CHANNEL_ID,
                    PLAYER_NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableLights(false) //如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
                    setShowBadge(false) //是否显示角标
                    setSound(null, null)
                    lockscreenVisibility = Notification.VISIBILITY_SECRET
                }
                createNotificationChannel(playerChannel)
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val source = intent.getSerializableExtra("source") as SourceType? ?: SourceType.NULL
        setForeground(source)
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 前台服务
     */
    private fun setForeground(source: SourceType) {
        startForeground(
            source.getNotificationId(), foreGroundNotification(source)
        )
    }

    private fun foreGroundNotification(sourceType: SourceType): Notification? {

        val intent = when (sourceType) {
            SourceType.PLAYER_FRAGMENT ->
                Intent(this, SourceType.PLAYER_FRAGMENT.getActivityClass()).apply {
                    action = MainActivity.ACTION_OPEN_FRAGMENT
                    putExtra(
                        MainActivity.EXTRA_KEY_OPEN_FRAGMENT,
                        MainActivity.OPEN_PLAYER_FRAGMENT
                    )
                }
            else -> Intent()
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, PLAYER_NOTIFICATION_CHANNEL_ID)
//            .setContentTitle("直播啦")
            .setContentText("${sourceType.getName()}播放中")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent).build()
    }

    companion object {

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
                    PLAYER_FRAGMENT -> "播放器"
                    PLAYER_OVERLAY -> "悬浮窗播放器"
                    else -> ""
                }
            }

            fun getNotificationId(): Int {
                return when (this) {
                    PLAYER_FRAGMENT -> PLAYER_ACTIVITY_NOTIFICATION_ID
                    PLAYER_OVERLAY -> PLAYER_OVERLAY_NOTIFICATION_ID
                    else -> PLAYER_DEFAULT_NOTIFICATION_ID
                }
            }
        }

        private const val PLAYER_DEFAULT_NOTIFICATION_ID: Int = 2000
        private const val PLAYER_ACTIVITY_NOTIFICATION_ID: Int = 2001
        private const val PLAYER_OVERLAY_NOTIFICATION_ID: Int = 2002
        private const val PLAYER_NOTIFICATION_CHANNEL_ID: String = "notification_channel_id"
        private const val PLAYER_NOTIFICATION_CHANNEL_NAME: String = "播放器前台服务"

        @JvmStatic
        fun startWithForeground(type: SourceType?) {
            val intent = Intent(MyApplication.application, PlayerService::class.java)
            when (type) {
                SourceType.PLAYER_FRAGMENT ->
                    intent.putExtra("source", SourceType.PLAYER_FRAGMENT)
                SourceType.PLAYER_OVERLAY ->
                    intent.putExtra("source", SourceType.PLAYER_OVERLAY)
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