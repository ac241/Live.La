package com.acel.streamlivetool.ui.adapter

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R

class AnchorRecyclerViewOnScrollListener : RecyclerView.OnScrollListener() {
    companion object {
        const val STATUS_GROUP_TITLE_LIVING = 998
        const val STATUS_GROUP_TITLE_NOT_LIVING = 999
    }

    private var nowTitle = 0
    private var nowTitlePosition = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val first = recyclerView.getChildAt(0)
        val viewPosition = recyclerView.getChildAdapterPosition(first)
        (recyclerView.parent.parent as View).findViewById<FrameLayout>(R.id.group_title_wrapper)
            .apply {
                when (first.tag) {
                    STATUS_GROUP_TITLE_LIVING -> {
                        findViewById<TextView>(R.id.status_living).visibility = View.GONE
                        findViewById<TextView>(R.id.status_not_living).visibility =
                            View.GONE
                        nowTitle = STATUS_GROUP_TITLE_LIVING
                        nowTitlePosition = viewPosition
                    }
                    STATUS_GROUP_TITLE_NOT_LIVING -> {
                        findViewById<TextView>(R.id.status_living).visibility = View.GONE
                        findViewById<TextView>(R.id.status_not_living).visibility =
                            View.GONE
                        nowTitle = STATUS_GROUP_TITLE_NOT_LIVING
                        nowTitlePosition = viewPosition
                    }
                    else ->
                        when (nowTitle) {
                            STATUS_GROUP_TITLE_LIVING -> {
                                findViewById<TextView>(R.id.status_living).visibility = View.VISIBLE
                                findViewById<TextView>(R.id.status_not_living).visibility =
                                    View.GONE
                            }
                            STATUS_GROUP_TITLE_NOT_LIVING -> {
                                if (viewPosition > nowTitlePosition) {
                                    findViewById<TextView>(R.id.status_living).visibility =
                                        View.GONE
                                    findViewById<TextView>(R.id.status_not_living).visibility =
                                        View.VISIBLE
                                }else{
                                    findViewById<TextView>(R.id.status_living).visibility = View.VISIBLE
                                    findViewById<TextView>(R.id.status_not_living).visibility =
                                        View.GONE
                                }
                            }
                        }
                }
            }
    }
}