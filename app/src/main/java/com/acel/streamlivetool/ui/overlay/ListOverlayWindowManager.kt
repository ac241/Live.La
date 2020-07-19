package com.acel.streamlivetool.ui.overlay

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.ui.adapter.ListOverlayAdapter


class ListOverlayWindowManager {
    companion object {
        val instance by lazy { ListOverlayWindowManager() }
    }

    var isShown = false
    private val applicationContext: Context = MyApplication.application.applicationContext
    private val listOverlayWindow by lazy {
        ListOverlayWindow.instance.create().also { it.setMovable() }
    }
    private val listOverlayView: View? = listOverlayWindow.getLayout()
    private val recyclerViewListOverlay: RecyclerView? =
        listOverlayView?.findViewById(R.id.recycler_view_list_overlay)

    init {
        recyclerViewListOverlay?.layoutManager = LinearLayoutManager(applicationContext)
        //关闭按钮
        val btnClose = listOverlayView?.findViewById<ImageView>(R.id.btn_list_overlay_close)
        btnClose?.setOnClickListener {
            remove()
        }
    }

    /**
     * 创建List悬浮窗
     */
    internal fun show(
        context: Context,
        anchorList: List<Anchor>,
        anchorAttributeMap: MutableLiveData<MutableMap<String, AnchorAttribute>>?
    ) {

        recyclerViewListOverlay?.adapter =
            if (anchorAttributeMap != null)
                ListOverlayAdapter(context, anchorList, anchorAttributeMap)
            else
                ListOverlayAdapter(context, anchorList)

        listOverlayWindow.show()
        isShown = true
    }

    /**
     * 移除List悬浮窗
     */
    internal fun remove() {
        listOverlayWindow.remove()
        isShown = false
    }

    internal fun toggleShow(
        context: Context,
        anchorList: List<Anchor>,
        anchorAttributeMap: MutableLiveData<MutableMap<String, AnchorAttribute>>? = null
    ) {
        if (isShown) {
            remove()
        } else {
            show(context, anchorList, anchorAttributeMap)
        }
    }
}