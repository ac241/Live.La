package com.acel.streamlivetool.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.view.*

abstract class AbsOverlayWindow {
    abstract val layoutId: Int
    abstract val widthDp: Float
    abstract val heightDp: Float
    abstract val x: Int
    abstract val y: Int
    val layoutParams by lazy { WindowManager.LayoutParams() }
    private var mLayout: View? = null
    var isShown: Boolean = false

    @Suppress("DEPRECATION")
    fun create(context: Context): AbsOverlayWindow {
        val layout = LayoutInflater.from(context).inflate(layoutId, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams.format = PixelFormat.RGBA_8888
        layoutParams.width = dp2px(widthDp).toInt()
        layoutParams.height = dp2px(heightDp).toInt()
        layoutParams.x = x
        layoutParams.y = y
        layoutParams.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        this.mLayout = layout
        show(context)
        return this
    }

    fun show(context: Context) {
        val windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (isShown) {
            windowManager.updateViewLayout(mLayout, layoutParams)
        } else {
            windowManager.addView(mLayout, layoutParams)
            isShown = true
        }
    }

    fun remove(context: Context) {
        val windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (isShown) {
            windowManager.removeView(mLayout)
            isShown = false
        }
    }

    fun getLayout(): View? {
        return mLayout
    }

    protected fun dp2px(dp: Float): Float =
        (0.5f + dp * Resources.getSystem().displayMetrics.density)

    @SuppressLint("ClickableViewAccessibility")
    fun setMovable(windowManager: WindowManager) {
        mLayout ?: throw Exception("window did not created")
        mLayout?.setOnTouchListener(object : View.OnTouchListener {
            private var x = 0
            private var y = 0
            override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = event.rawX.toInt()
                        y = event.rawY.toInt()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val nowX = event.rawX
                        val nowY = event.rawY
                        val movedX = nowX - x
                        val movedY = nowY - y
                        x = nowX.toInt()
                        y = nowY.toInt()
                        layoutParams.x = (layoutParams.x + movedX).toInt()
                        layoutParams.y = (layoutParams.y + movedY).toInt()
                        // 更新悬浮窗控件布局
                        windowManager.updateViewLayout(p0, layoutParams)
                    }
                }
                return false
            }
        })
    }
}