package com.acel.streamlivetool.ui.custom_view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.GridView

class MyGridView : GridView {
    private var mLastY = 0f
    private var mLastX = 0f

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleRes: Int) : super(
        context,
        attributeSet,
        defStyleRes
    )

//    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
//        val x = ev.x
//        val y = ev.y
//        when (ev.action) {
//            MotionEvent.ACTION_DOWN -> {
//                parent.requestDisallowInterceptTouchEvent(true)
//            }
//            MotionEvent.ACTION_MOVE -> {
//                val deltaX = x - mLastX
//                val deltaY = y - mLastY
//                if (deltaX > deltaY)
//                    parent.requestDisallowInterceptTouchEvent(false)
//            }
//            MotionEvent.ACTION_UP -> {
//
//            }
//        }
//        mLastX = x
//        mLastY = y
//        return super.dispatchTouchEvent(ev)
//    }
}