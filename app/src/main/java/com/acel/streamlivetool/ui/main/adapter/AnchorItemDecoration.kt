package com.acel.streamlivetool.ui.main.adapter

import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R

class AnchorItemDecoration(private val iconDrawable: Drawable) :
    RecyclerView.ItemDecoration() {
    private var floatingLivingView: View? = null
    private var floatingNotLivingView: View? = null
    private var nowFloatingView: View? = null
    private var orientation = Configuration.ORIENTATION_PORTRAIT
    private var newOrientation = Configuration.ORIENTATION_PORTRAIT

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val adapter = parent.adapter as AnchorAdapter
        val layoutManager = parent.layoutManager as GridLayoutManager
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        if (firstVisiblePosition == RecyclerView.NO_POSITION)
            return

        initFloatingView(parent)

        var offsetY = 0f
        nowFloatingView = floatingLivingView
        if (firstVisiblePosition == 0) {
            if (adapter.getItemViewType(0) == VIEW_TYPE_SECTION_NOT_LIVING)
                nowFloatingView = floatingNotLivingView
        } else {
            val notLivingPosition = adapter.getNotLivingSectionPosition()
            val notLivingSectionView =
                layoutManager.findViewByPosition(notLivingPosition)
            if (notLivingSectionView != null) {
                if (notLivingSectionView.top < nowFloatingView!!.height && notLivingSectionView.top > 0) {
                    offsetY = notLivingSectionView.top.toFloat() - nowFloatingView!!.height
                } else if (notLivingSectionView.top <= 0)
                    nowFloatingView = floatingNotLivingView
            } else {
                if (notLivingPosition < firstVisiblePosition)
                    nowFloatingView = floatingNotLivingView
            }
        }
        c.save()
        c.translate(parent.paddingStart.toFloat(), offsetY)
        nowFloatingView?.draw(c)
        c.restore()
    }

    @Synchronized
    private fun initFloatingView(parent: RecyclerView) {
        if (floatingLivingView == null) {
            floatingLivingView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_section_living, parent, false).apply {
                    floatingLivingView?.measureAndLayoutFloatingSection(parent)
                    iconDrawable.setBounds(0, 0, 40, 40)
                    findViewById<TextView>(R.id.status)?.apply {
                        setCompoundDrawables(null, null, iconDrawable, null)
                    }
                }
            floatingNotLivingView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_section_not_living, parent, false).apply {
                    floatingLivingView?.measureAndLayoutFloatingSection(parent)
                    iconDrawable.setBounds(0, 0, 40, 40)
                    findViewById<TextView>(R.id.status)?.apply {
                        setCompoundDrawables(null, null, iconDrawable, null)
                    }
                }
        } else {
            val result1 = floatingLivingView?.measureAndLayoutFloatingSection(parent)!!
            val result2 = floatingNotLivingView?.measureAndLayoutFloatingSection(parent)!!
            if (result1 && result2) {
                orientation = newOrientation
            }
        }
    }

    private fun View.measureAndLayoutFloatingSection(parent: RecyclerView): Boolean {
        val contrastView =
            (parent.layoutManager as GridLayoutManager).findViewByPosition(0)
        contrastView?.let {
            measure(it.width, it.height)
            layout(0, 0, it.width, it.height)
            return true
        }
        return false
    }

    fun setOrientation(newOrientation: Int) {
        this.newOrientation = newOrientation
    }
}