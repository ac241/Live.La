/*
 * Copyright (c) 2020.
 * @author acel
 * 用于给anchor分组：直播中、未直播
 */


package com.acel.streamlivetool.ui.main.adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.util.CommonColor

@Deprecated("已使用itemDecoration替代")
class AnchorGroupingListener : RecyclerView.OnScrollListener() {
    companion object {
        const val STATUS_GROUP_TITLE_LIVING = 996
        const val STATUS_GROUP_TITLE_NOT_LIVING = 997
        const val STATUS_LIVING = 998
        const val STATUS_NOT_LIVING = 999
    }

    private val livingTitle = MyApplication.application.getString(R.string.is_living)

    @SuppressLint("ResourceType")
    private val livingColor = CommonColor.livingColor ?: 0

    private val notLivingTitle = MyApplication.application.getString(R.string.not_living)

    @SuppressLint("ResourceType")
    private val notLivingColor = CommonColor.notLivingColor ?: 0
//    Color.parseColor(MyApplication.application.getString(R.color.item_secondary_text_color))

    private var nowTitle = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (recyclerView.childCount != 0) {
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
    }

    private fun FrameLayout.hideTitle(tag: Any) {
        findViewById<TextView>(R.id.status).visibility = View.GONE
        nowTitle = tag as Int
    }

    private fun FrameLayout.showNotLivingTitle() {
        findViewById<TextView>(R.id.status).apply {
            this.visibility = View.VISIBLE
            this.text = notLivingTitle
            this.setTextColor(notLivingColor)
        }
        nowTitle = STATUS_NOT_LIVING
    }

    private fun FrameLayout.showLivingTitle() {
        findViewById<TextView>(R.id.status).apply {
            this.visibility = View.VISIBLE
            this.text = livingTitle
            this.setTextColor(livingColor)
        }
        nowTitle = STATUS_LIVING
    }
}