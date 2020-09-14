package com.acel.streamlivetool.ui.overlay.list

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.main.adapter.AnchorGroupingListener


class ListOverlayWindowManager {
    companion object {
        val instance by lazy { ListOverlayWindowManager() }
    }

    private var isShown = false
    private val applicationContext: Context = MyApplication.application.applicationContext
    private val listOverlayWindow by lazy {
        ListOverlayWindow.instance.create().also { it.setMovable() }
    }
    private val listOverlayView: View? = listOverlayWindow.getLayout()
    private val recyclerViewListOverlay: RecyclerView? =
        listOverlayView?.findViewById(R.id.recycler_view)

    init {
        recyclerViewListOverlay?.alpha = 0.7f
        recyclerViewListOverlay?.layoutManager = LinearLayoutManager(applicationContext)
        recyclerViewListOverlay?.addOnScrollListener(AnchorGroupingListener())

        //关闭按钮
        val btnClose = listOverlayView?.findViewById<ImageView>(R.id.btn_list_overlay_close)
        btnClose?.setOnClickListener {
            remove()
        }
    }

    /**
     * 创建List悬浮窗
     */
    private fun show(
        context: Context,
        anchorList: List<Anchor>
    ) {
        recyclerViewListOverlay?.adapter =
            ListOverlayAdapter(
                context,
                anchorList
            )
        listOverlayWindow.show()
        isShown = true
    }

    /**
     * 移除List悬浮窗
     */
    private fun remove() {
        listOverlayWindow.remove()
        isShown = false
    }

    internal fun toggleShow(
        context: Context,
        anchorList: List<Anchor>
    ) {
        if (isShown) {
            remove()
        } else {
            show(context, anchorList)
        }
    }
}