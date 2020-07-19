package com.acel.streamlivetool.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.ActionClick.itemClick
import com.acel.streamlivetool.ui.cookie_mode.CookieModeActivity
import com.acel.streamlivetool.ui.group_mode.GroupModeActivity
import com.acel.streamlivetool.ui.group_mode.GroupModePresenter
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.item_list_overlay.view.*
import kotlinx.android.synthetic.main.item_recycler_anchor.view.anchor_name
import kotlinx.android.synthetic.main.item_recycler_anchor.view.anchor_title


class ListOverlayAdapter() : RecyclerView.Adapter<ListOverlayAdapter.ViewHolder>(),
    AnchorAdapterWrapper {
    constructor(context: Context, anchorList: List<Anchor>) : this() {
        if (context !is CookieModeActivity)
            throw Exception("only cookie mode")
        this.context = context
        this.anchorList = anchorList
    }

    constructor(
        context: Context, anchorList: List<Anchor>,
        anchorAttributeMap: MutableLiveData<MutableMap<String, AnchorAttribute>>
    ) : this() {
        if (context !is GroupModeActivity)
            throw Exception("only group mode")
        this.context = context
        this.anchorList = anchorList
        this.anchorAttributeMap = anchorAttributeMap
    }

    private lateinit var context: Context
    private lateinit var anchorList: List<Anchor>
    private var anchorAttributeMap: MutableLiveData<MutableMap<String, AnchorAttribute>>? = null


    private val platformNameMap: MutableMap<String, String> = mutableMapOf()
    private var mPosition: Int = -1
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_list_overlay, p0, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
        val anchor: Anchor = anchorList[p1]
        with(holder) {
            anchorAttributeMap?.value?.get(anchor.anchorKey()).let {
                this.title.text =
                    it?.title ?: "-"
                //直播状态
                if (it?.isLive != null) {
                    if (it.isLive) {
                        this.status.text = "直播中"
                        this.status.setTextColor(Color.GREEN)
                    } else {
                        this.status.text = "未直播"
                        this.status.setTextColor(Color.GRAY)
                    }
                } else {
                    this.status.text = ""
                }
            }
            //主播名
            this.anchorName.text = anchor.nickname
            //平台名
            var platformName: String? =
                platformNameMap[anchor.platform]
            if (platformName == null) {
                val resInt =
                    PlatformDispatcher.getPlatformImpl(anchor.platform)?.platformShowNameRes
                if (resInt != null)
                    platformName = context.getString(resInt)
            }
            this.platform.text = platformName ?: "未知平台"
            //item click
            this.itemView.setOnClickListener {
                itemClick(context, anchor)
            }
        }
    }

    override fun getItemCount(): Int = anchorList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val anchorName: TextView = itemView.anchor_name
        val platform: TextView = itemView.anchor_platform
        val status: TextView = itemView.anchor_status
        val title: TextView = itemView.anchor_title

    }

    override fun getLongClickPosition(): Int {
        return mPosition
    }

    override fun notifyAnchorsChange() {
        notifyDataSetChanged()
    }


}
