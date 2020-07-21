package com.acel.streamlivetool.ui.overlay

import android.content.Context
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import com.acel.streamlivetool.base.MyApplication

abstract class AbsOverlayWindow {
    private val applicationContext = MyApplication.application.applicationContext
    private val windowManager =
        MyApplication.application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val outMetrics = DisplayMetrics().also { windowManager.defaultDisplay.getMetrics(it) }
    private val widthPixels = outMetrics.widthPixels
    private val heightPixels = outMetrics.heightPixels
    abstract val layoutId: Int
    abstract val widthDp: Float
    abstract val heightDp: Float
    abstract val x: Int
    abstract val y: Int
    val layoutParams by lazy { WindowManager.LayoutParams() }
    private lateinit var mLayout: View
    var isShown: Boolean = false

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
        layoutParams.x = x
        layoutParams.y = y
        layoutParams.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
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
            private var x = 0
            private var y = 0
            private var xOffset = 0
            private var yOffset = 0

            override fun onTouch(p0: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = event.rawX.toInt()
                        y = event.rawY.toInt()
                        xOffset = event.x.toInt()
                        yOffset = event.y.toInt()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val nowX = event.rawX
                        val nowY = event.rawY
                        if (nowX >= 0 && nowY >= 0) {
                            val movedX = nowX - x
                            val movedY = nowY - y
                            val newX = layoutParams.x + movedX
                            val newY = layoutParams.y + movedY
                            val checkX = newX >= 0 && newX < widthPixels - mLayout.width
                            if (checkX) {
                                layoutParams.x = (newX).toInt()
                                x = nowX.toInt()
                            }
                            val checkY = newY >= 0 && newY < heightPixels - mLayout.height
                            if (checkY) {
                                layoutParams.y = (newY).toInt()
                                y = nowY.toInt()
                            }
                            // 更新悬浮窗控件布局
                            if (checkX || checkY)
                                windowManager.updateViewLayout(p0, layoutParams)
                        }
                    }
                }
                p0.performClick()
                return false
            }
        })
    }
}