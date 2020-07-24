package com.acel.streamlivetool.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication

class AnchorListAddTitleListener : RecyclerView.OnScrollListener() {
    companion object {
        const val STATUS_GROUP_TITLE_LIVING = 996
        const val STATUS_GROUP_TITLE_NOT_LIVING = 997
        const val STATUS_LIVING = 998
        const val STATUS_NOT_LIVING = 999
    }

    private val livingTitle = MyApplication.application.getString(R.string.is_living)

    @SuppressLint("ResourceType")
    private val livingColor =
        Color.parseColor(MyApplication.application.getString(R.color.colorPrimary))
    private val notLivingTitle = MyApplication.application.getString(R.string.not_living)

    @SuppressLint("ResourceType")
    private val notLivingColor =
        Color.parseColor(MyApplication.application.getString(R.color.lightDark))
    private var nowTitle = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val first = recyclerView.getChildAt(0)
        (recyclerView.parent.parent as View).findViewById<FrameLayout>(R.id.group_title_wrapper)
            .apply {
                if (nowTitle != first.tag)
                    when (first.tag) {
                        STATUS_GROUP_TITLE_LIVING, STATUS_GROUP_TITLE_NOT_LIVING -> {
                            hideTitle(first.tag)
                        }
                        STATUS_LIVING -> {
                            showLivingTitle()
                        }
                        STATUS_NOT_LIVING -> {
                            showNotLivingTitle()
                        }
                    }
            }
    }

    private fun FrameLayout.hideTitle(tag: Any) {
        findViewById<TextView>(R.id.status_living).visibility = View.GONE
        nowTitle = tag as Int
    }

    private fun FrameLayout.showNotLivingTitle() {
        findViewById<TextView>(R.id.status_living).apply {
            this.visibility = View.VISIBLE
            this.text = notLivingTitle
            this.setTextColor(notLivingColor)
        }
        nowTitle = STATUS_NOT_LIVING
    }

    private fun FrameLayout.showLivingTitle() {
        findViewById<TextView>(R.id.status_living).apply {
            this.visibility = View.VISIBLE
            this.text = livingTitle
            this.setTextColor(livingColor)
        }
        nowTitle = STATUS_LIVING
    }
}