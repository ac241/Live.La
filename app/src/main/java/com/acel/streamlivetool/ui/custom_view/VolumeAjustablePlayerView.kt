/**
 * @author acel
 * 2021/2/13
 */
package com.acel.streamlivetool.ui.custom_view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.exoplayer2.ui.PlayerView
import kotlin.math.abs

class VolumeAjustablePlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PlayerView(context, attrs, defStyleAttr) {

    private var downX = 0f
    private var downY = 0f
    private var lastMoveY = 0f
    private var handleVolume = false
    private val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
    private val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                if (downX > width / 2)
                    handleVolume = true
            }
            MotionEvent.ACTION_MOVE -> {
                //调整音量
                if (handleVolume) {
                    //当前音量 + (移动差值/view高度) * 最大音量
                    val offsetY = -(event.y - if (lastMoveY == 0f) downY else lastMoveY)
                    val percent = offsetY / height / 2
                    val nowVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    var volume = (nowVolume + percent * maxVolume).toInt()
                    //如果音量差小于5不处理，即每次步进/步退5
                    if (abs(volume - nowVolume) < 5)
                        return true
                    when (volume) {
                        in (0..150) -> {
                        }
                        else -> {
                            if (volume < 0)
                                volume = 0
                            if (volume > maxVolume)
                                volume = maxVolume
                        }
                    }
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        volume,
                        AudioManager.FLAG_SHOW_UI
                    )
                    //记录上次调整时的Y
                    lastMoveY = if (lastMoveY == 0f) downY else event.y
                }
            }
            MotionEvent.ACTION_UP -> {
                if (abs(event.y - downY) < 5)
                    performClick()
                //重置参数
                resetHandleVolume()
            }
        }
        return true
    }

    private fun resetHandleVolume() {
        lastMoveY = 0f
        handleVolume = false
    }
}