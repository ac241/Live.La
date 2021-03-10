package com.acel.streamlivetool.ui.main.adapter

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R

class AnchorItemDecoration(private val iconDrawable: Drawable? = null) : RecyclerView.ItemDecoration() {
    private var floatingLivingView: View? = null
    private var floatingNotLivingView: View? = null
    private var nowFloatingView: View? = null

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val adapter = parent.adapter as AnchorAdapter
        val layoutManager = parent.layoutManager as StaggeredGridLayoutManager
        val firstVisible = layoutManager.findFirstVisibleItemPositions(null)
        val firstVisiblePosition = firstVisible[0]
        if (firstVisiblePosition == RecyclerView.NO_POSITION)
            return

        initFloatingView(parent)

        if (firstVisiblePosition != 0) {
            nowFloatingView = floatingLivingView
            var offsetY = 0f
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

            c.save()
            c.translate(parent.paddingStart.toFloat(), offsetY)
            nowFloatingView?.draw(c)
            c.restore()
        }
    }

    private fun initFloatingView(parent: RecyclerView) {
        if (floatingLivingView == null) {
            floatingLivingView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_section_living, parent, false).apply {
                    val sectionView =
                        (parent.layoutManager as StaggeredGridLayoutManager).findViewByPosition(0)
                    sectionView?.let {
                        measure(it.width, it.height)
                        layout(0, 0, it.width, it.height)

                    }

                    val drawable = iconDrawable ?: ResourcesCompat.getDrawable(
                        parent.context.resources,
                        R.drawable.ic_home_page, null
                    )
                    drawable?.setBounds(0, 0, 40, 40)
                    findViewById<TextView>(R.id.status)?.apply {
                        setCompoundDrawables(null, null, drawable, null)
                    }
                }
            floatingNotLivingView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_section_not_living, parent, false).apply {
                    val sectionView =
                        (parent.layoutManager as StaggeredGridLayoutManager).findViewByPosition(0)
                    sectionView?.let {
                        measure(it.width, it.height)
                        layout(0, 0, it.width, it.height)
                    }
                    val drawable = iconDrawable ?: ResourcesCompat.getDrawable(
                        parent.context.resources,
                        R.drawable.ic_home_page, null
                    )
                    drawable?.setBounds(0, 0, 40, 40)
                    findViewById<TextView>(R.id.status)?.apply {
                        setCompoundDrawables(null, null, drawable, null)
                    }
                }
        }
    }
}