package com.acel.streamlivetool.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorPlaceHolder
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.ActionClick.itemClick
import kotlinx.android.synthetic.main.item_overlay_list.view.*
import kotlinx.android.synthetic.main.item_recycler_anchor.view.anchor_name
import kotlinx.android.synthetic.main.item_recycler_anchor.view.anchor_title


class ListOverlayAdapter(val context: Context, val anchorList: List<Anchor>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AnchorAdapterWrapper {


    private val platformNameMap: MutableMap<String, String> = mutableMapOf()
    private var mPosition: Int = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_list_overlay, p0, false)
//        return ViewHolder(view)
        val holder: RecyclerView.ViewHolder
        when (viewType) {
            GraphicAnchorAdapter.VIEW_TYPE_LIVING ->
                holder = ViewHolderStatusGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_overlay_status_group, parent, false)
                )
            GraphicAnchorAdapter.VIEW_TYPE_SLEEPING ->
                holder = ViewHolderStatusGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_overlay_status_group, parent, false)
                )
            else ->
                holder = ViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_overlay_list, parent, false)
                )
        }
        return holder
    }

    override fun getItemViewType(position: Int): Int {
        return when (anchorList[position]) {
            AnchorPlaceHolder.anchorIsLiving ->
                GraphicAnchorAdapter.VIEW_TYPE_LIVING
            AnchorPlaceHolder.anchorNotLiving ->
                GraphicAnchorAdapter.VIEW_TYPE_SLEEPING
            else ->
                GraphicAnchorAdapter.VIEW_TYPE_NORMAL
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderStatusGroup) {
            when (anchorList[position]) {
                AnchorPlaceHolder.anchorIsLiving ->
                    holder.statusText.text = context.getString(R.string.is_living)
                AnchorPlaceHolder.anchorNotLiving ->
                    holder.statusText.text = context.getString(R.string.not_living)
            }
            return
        }
        holder as ViewHolder
        val anchor: Anchor = anchorList[position]
        with(holder) {
            this.title.text =
                anchor.title ?: "-"
            //直播状态
            if (!anchorList.contains(AnchorPlaceHolder.anchorIsLiving)
                && !anchorList.contains(AnchorPlaceHolder.anchorIsLiving)
            ) {
                holder.status.visibility = View.VISIBLE
                if (anchor.status) {
                    this.status.text = "直播中"
                    this.status.setTextColor(Color.GREEN)
                } else {
                    this.status.text = "未直播"
                    this.status.setTextColor(Color.GRAY)
                }
            } else
                holder.status.visibility = View.GONE

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

    override fun setScrolling(boolean: Boolean) {

    }
}
