package com.acel.streamlivetool.ui.overlay.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.overlay.AbsOverlayWindow
import kotlinx.android.synthetic.main.layout_overlay_list.view.*

class ListOverlayWindow : AbsOverlayWindow() {
    @SuppressLint("InflateParams")
    override fun onCreateWindow(): View {
        return LayoutInflater.from(MyApplication.application)
            .inflate(R.layout.layout_overlay_list, null, false)
    }

    private val displayPixels = run {
        val width = MyApplication.application.resources.displayMetrics.widthPixels
        val height = MyApplication.application.resources.displayMetrics.heightPixels
        if (width < height)
            Pair(width, height)
        else
            Pair(height, width)
    }

    private var anchorList: List<Anchor>? = null

    override fun onWindowCreated() {
        resize(displayPixels.first / 2, (displayPixels.first / 2 * 1.5f).toInt())
        rootView.recycler_view.apply {
            alpha = 0.7f
            layoutManager = LinearLayoutManager(MyApplication.application)
//            addOnScrollListener(AnchorGroupingListener())
            adapter =
                anchorList?.let { ListOverlayAdapter(context, it) }
        }
        rootView.btn_list_overlay_close.setOnClickListener {
            hide()
        }
    }

    fun show(anchorList: List<Anchor>) {
        this.anchorList = anchorList
        show()
    }
}