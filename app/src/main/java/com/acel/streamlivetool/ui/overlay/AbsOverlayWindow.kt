package com.acel.streamlivetool.ui.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import com.acel.streamlivetool.base.MyApplication
import kotlin.math.abs

abstract class AbsOverlayWindow {
    private val applicationContext = MyApplication.application.applicationContext
    private val windowManager =
            MyApplication.application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var outMetrics = DisplayMetrics().also { windowManager.defaultDisplay.getMetrics(it) }
    private var widthPixels = outMetrics.widthPixels
    private var heightPixels = outMetrics.heightPixels
    abstract val layoutId: Int
    abstract val widthDp: Float
    abstract val heightDp: Float
    abstract val defaultX: Int
    abstract val defaultY: Int
    val layoutParams by lazy { WindowManager.LayoutParams() }
    private lateinit var mLayout: View
    var isShown: Boolean = false


    init {
        ConfigurationChangeBroadcastReceiver().register()
    }

    @Suppress("DEPRECATION")
    fun create(): AbsOverlayWindow {
        val layout = LayoutInflater.from(applicationContext).inflate(layoutId, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams.format = PixelFormat.RGBA_8888
        layoutParams.width = dp2px(widthDp).toInt()
        layoutParams.height = dp2px(heightDp).toInt()
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = defaultX
        layoutParams.y = defaultY
        layoutParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        this.mLayout = layout
        show()
        return this
    }

    fun show() {
        if (isShown) {
            windowManager.updateViewLayout(mLayout, layoutParams)

        } else {
            windowManager.addView(mLayout, layoutParams)
            isShown = true
        }
    }

    fun remove() {
        val windowManager =
                applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (isShown) {
            windowManager.removeView(mLayout)
            isShown = false
        }
    }

    fun getLayout(): View {
        return mLayout
    }

    protected fun dp2px(dp: Float): Float =
            (0.5f + dp * Resources.getSystem().displayMetrics.density)


    fun setMovable() {
        mLayout.setOnTouchListener(object : View.OnTouchListener {
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
                        windowManager.updateViewLayout(view, layoutParams)
                    }
                    MotionEvent.ACTION_UP -> {
                        setWindowSize()
                        layoutParams.apply {
                            when {
                                x < 0 ->
                                    x = 0
                                x > widthPixels - mLayout.width ->
                                    x = widthPixels - mLayout.width
                            }
                            when {
                                y < (0 - getStatusBarHeight()) ->
                                    y = 0 - getStatusBarHeight()
                                y > heightPixels - mLayout.height ->
                                    y = heightPixels - mLayout.height - getStatusBarHeight()
                            }
                        }
                        windowManager.updateViewLayout(view, layoutParams)
                        if (abs(event.rawX - downX) < 5 && abs(event.rawY - downY) < 5)
                            view.performClick()
                    }
                }
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        x = event.rawX.toInt()
//                        y = event.rawY.toInt()
//                        xOffset = event.x.toInt()
//                        yOffset = event.y.toInt()
//                        downX = event.rawX.toInt()
//                        downY = event.rawY.toInt()
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        val nowX = event.rawX
//                        val nowY = event.rawY
//                        if (nowX >= 0 && nowY >= 0) {
//                            val movedX = nowX - x
//                            val movedY = nowY - y
//                            var newX = layoutParams.x + movedX
//                            //如果越界，移动到X最大
//                            if (newX > widthPixels - mLayout.width)
//                                newX = (widthPixels - mLayout.width).toFloat()
//                            var newY = layoutParams.y + movedY
//                            //如果越界，移动到Y最大
//                            if (newY > heightPixels - mLayout.height)
//                                newY = (heightPixels - mLayout.height).toFloat()
//                            val checkX =
//                                newX >= 0 && newX <= widthPixels - mLayout.width
//                            if (checkX) {
//                                layoutParams.x = (newX).toInt()
//                                x = nowX.toInt()
//                            }
//                            val checkY =
//                                newY >= 0 && newY <= heightPixels - mLayout.height
//                            if (checkY) {
//                                layoutParams.y = (newY).toInt()
//                                y = nowY.toInt()
//                            }
//                            // 更新悬浮窗控件布局
//                            if (checkX || checkY)
//                                windowManager.updateViewLayout(view, layoutParams)
//                        }
//                    }
//                    MotionEvent.ACTION_UP -> {
//                        if (abs(event.rawX - downX) < 5 && abs(event.rawY - downY) < 5)
//                            view.performClick()
//                    }
//                }
                return true
            }
        })
    }

    inner class ConfigurationChangeBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!isShown)
                return
            val nowWidth = widthPixels
            val nowHeight = heightPixels
            setWindowSize()

            layoutParams.apply {
                x = widthPixels * x / nowWidth
                y = heightPixels * y / nowHeight

                when {
                    x < 0 ->
                        x = 0
                    x > widthPixels - mLayout.width ->
                        x = widthPixels - mLayout.width
                }
                when {
                    y < (0 - getStatusBarHeight()) ->
                        y = 0 - getStatusBarHeight()
                    y > heightPixels - mLayout.height ->
                        y = heightPixels - mLayout.height - getStatusBarHeight()
                }
            }
            windowManager.updateViewLayout(mLayout, layoutParams)
        }

        fun register() {
            MyApplication.application.registerReceiver(
                    ConfigurationChangeBroadcastReceiver(),
                    IntentFilter("android.intent.action.CONFIGURATION_CHANGED")
            )
        }
    }

    private fun setWindowSize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.apply {
                widthPixels = width()
                heightPixels = height()
            }
        } else {
            outMetrics = DisplayMetrics().also {
                windowManager.defaultDisplay.getMetrics(it)
            }
            widthPixels = outMetrics.widthPixels
            heightPixels = outMetrics.heightPixels
        }
    }

    private fun getStatusBarHeight(): Int {
        val resources = MyApplication.application.resources
        val resourceId = MyApplication.application.resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }
}