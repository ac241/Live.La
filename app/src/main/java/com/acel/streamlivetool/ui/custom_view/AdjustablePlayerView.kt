/**
 * @author acel
 * 2021/2/13
 */
package com.acel.streamlivetool.ui.custom_view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.provider.Settings
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.WindowManager
import com.google.android.exoplayer2.ui.PlayerView
import kotlin.math.abs


class AdjustablePlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PlayerView(context, attrs, defStyleAttr) {

    companion object {
        private const val HANDLE_TYPE_NULL = -1
        private const val HANDLE_TYPE_LIGHT = 1
        private const val HANDLE_TYPE_VOLUME = 2
    }

    private var adjustListener: AdjustListener? = null


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        (context as Activity).window.apply {
            attributes = attributes.apply {
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
        }
        adjustListener = null
    }

    private var Context.brightness
        get() = (this as Activity).window.attributes.screenBrightness
        set(value) {
            (this as Activity).window.attributes = window.attributes.apply {
                screenBrightness = value.fixBrightness()
            }
        }

    private fun Float.fixBrightness(): Float {
        return when {
            this >= 1.0F -> WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
            0 < this && this < 1.0F -> this
            this <= 0 -> WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF
            else -> -1.0F
        }
    }

    private val screenBrightness =
        Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
    private var maxBrightness: Int = 255
    private var currentBrightness: Float = 0f

    init {
        if (android.os.Build.BRAND == "Xiaomi")
            maxBrightness = 4095
        currentBrightness = screenBrightness.toFloat() / maxBrightness
    }

    private var downX = 0f
    private var downY = 0f
    private var lastMoveY = 0f
    private var handleType = HANDLE_TYPE_NULL

    private val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
    private val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                if (downX < width * 3 / 10)
                    handleType = HANDLE_TYPE_LIGHT
                if (downX > width * 7 / 10)
                    handleType = HANDLE_TYPE_VOLUME
            }
            MotionEvent.ACTION_MOVE -> {
                if (handleType == HANDLE_TYPE_NULL)
                    return true
                val offsetY = -(event.y - if (lastMoveY == 0f) downY else lastMoveY)

                when (handleType) {
                    HANDLE_TYPE_LIGHT -> {
                        val offsetPercent = offsetY / height * 1.5f
//                        if (abs(offsetPercent) > 0.05)
//                            return true
                        val value = currentBrightness + offsetPercent
                        context.brightness = value
                        currentBrightness = context.brightness
                        adjustListener?.onAdjust(
                            AdjustType.BRIGHTNESS,
                            (currentBrightness * 100).toInt()
                        )
                        lastMoveY = if (lastMoveY == 0f) downY else event.y
                    }

                    HANDLE_TYPE_VOLUME -> {
                        val offsetPercent = offsetY / height / 2
                        val nowVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        //当前音量 + (移动差值/view高度) * 最大音量
                        var volume = (nowVolume + offsetPercent * maxVolume).toInt()
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
                            AudioManager.FLAG_PLAY_SOUND
                        )

                        adjustListener?.onAdjust(
                            AdjustType.VOLUME,
                            (volume.toFloat() / maxVolume * 100).toInt()
                        )
                        //记录上次调整时的Y
                        lastMoveY = if (lastMoveY == 0f) downY else event.y
                    }
                }

            }
            MotionEvent.ACTION_UP -> {
                if (abs(event.y - downY) < 5)
                    performClick()
                //重置参数
                resetHandle()
                adjustListener?.onCancel()
            }
        }
        return true
    }


    fun setOnAdjustListener(listener: AdjustListener) {
        this.adjustListener = listener
    }

    interface AdjustListener {
        fun onAdjust(type: AdjustType, progress: Int)
        fun onCancel()
    }

    private fun resetHandle() {
        lastMoveY = 0f
        handleType = HANDLE_TYPE_NULL
    }


}