package com.acel.streamlivetool.ui.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.annotation.CallSuper
import com.acel.streamlivetool.base.MyApplication
import kotlin.math.abs


abstract class AbsOverlayWindow {
    private var applicationContext: Context? = MyApplication.application.applicationContext
    private var windowManager: WindowManager? =
        MyApplication.application.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var displayWidthPixels = 0
    private var displayHeightPixels = 0

    private val layoutParams = WindowManager.LayoutParams().apply {
        flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        gravity = Gravity.TOP or Gravity.START

        @Suppress("DEPRECATION")
        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE
        format = PixelFormat.RGBA_8888
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    private val defaultX: Int = 0
    private val defaultY: Int = 0

    private val defaultWidth: Int = 200
    private val defaultHeight: Int = 200

    lateinit var rootView: View
    var isShown: Boolean = false
    private var isCreated: Boolean = false

    private val configurationChangeBroadcastReceiver = ConfigurationChangeBroadcastReceiver()

    /**
     * @return 创建window时的root view
     */
    abstract fun onCreateWindow(): View

    @Synchronized
    private fun create() {
        if (!isCreated) {
            resetDisplayPixels()
            val view = onCreateWindow()
            layoutParams.apply {
                x = defaultX
                y = defaultY
                width = defaultWidth
                height = defaultHeight
            }
            rootView = view
            makeWindowMovable()
            isCreated = true
            onWindowCreated()
        }
    }

    open fun onWindowCreated() {}

    @Synchronized
    fun show() {
        if (!isCreated)
            create()
        if (!isShown)
            windowManager?.addView(rootView, layoutParams)
        isShown = true
        configurationChangeBroadcastReceiver.register()
        onWindowShowed()
    }

    open fun onWindowShowed() {}

    @Synchronized
    fun hide() {
        if (isShown)
            windowManager?.removeView(rootView)
        isShown = false
        configurationChangeBroadcastReceiver.unregister()
        onWindowHide()
    }

    open fun onWindowHide() {}

    @CallSuper
    open fun release() {
        hide()
        applicationContext = null
        windowManager = null
    }

    /**
     * 更新长宽
     */
    fun resize(width: Int, height: Int) {
        layoutParams.apply {
            this.width = width
            this.height = height
        }
        fixPosition()
    }


    /**
     * 使在touch时可跟随移动
     */
    private fun makeWindowMovable() {
        rootView.setOnTouchListener(object : View.OnTouchListener {
            var lastRawX = 0f
            var lastRawY = 0f
            var downX = 0f
            var downY = 0f
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastRawX = event.rawX
                        lastRawY = event.rawY
                        downX = event.rawX
                        downY = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val offsetX = event.rawX - lastRawX
                        val offsetY = event.rawY - lastRawY
                        layoutParams.x += offsetX.toInt()
                        layoutParams.y += offsetY.toInt()
                        lastRawX += offsetX
                        lastRawY += offsetY
                        updateView()
                    }
                    MotionEvent.ACTION_UP -> {
//                        resetWindowMetrics()
                        fixPosition()
                        updateView()
                        if (abs(event.rawX - downX) < 5 && abs(event.rawY - downY) < 5)
                            view.performClick()
                    }
                }
                return true
            }
        })
    }

    private fun updateView() {
        if (isShown)
            windowManager?.updateViewLayout(rootView, layoutParams)
    }

    /**
     * 修正窗口位置
     */
    private fun fixPosition() {
        layoutParams.apply {
            when {
                x < 0 ->
                    x = 0
                x > displayWidthPixels - layoutParams.width -> {
                    x = displayWidthPixels - layoutParams.width
                }
            }
            when {
                y < (0) ->
                    y = 0
                y > displayHeightPixels - layoutParams.height ->
                    y = displayHeightPixels - layoutParams.height
            }
        }
        updateView()
    }

    /**
     * 重设屏幕长宽
     */
    private fun resetDisplayPixels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager!!.currentWindowMetrics.bounds.apply {
                displayWidthPixels = width()
                displayHeightPixels = height()
            }
        } else {
            displayWidthPixels = applicationContext!!.resources.displayMetrics.widthPixels
            displayHeightPixels = applicationContext!!.resources.displayMetrics.heightPixels
        }
        fixPosition()
    }

    /**
     * 屏幕旋转接收器
     */
    inner class ConfigurationChangeBroadcastReceiver : BroadcastReceiver() {
        private var registered = false
        override fun onReceive(context: Context, intent: Intent?) {
            if (!isShown)
                return
            resetDisplayPixels()
        }

        fun register() {
            if (!registered)
                MyApplication.application.registerReceiver(
                    this,
                    IntentFilter("android.intent.action.CONFIGURATION_CHANGED")
                )
            registered = true
        }

        fun unregister() {
            if (registered)
                MyApplication.application.unregisterReceiver(this)
            registered = false
        }
    }
}