package com.acel.streamlivetool.ui.overlay.list

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.main.adapter.AnchorSection
import com.acel.streamlivetool.ui.main.adapter.VIEW_TYPE_ANCHOR
import com.acel.streamlivetool.ui.main.adapter.VIEW_TYPE_SECTION_LIVING
import com.acel.streamlivetool.ui.main.adapter.VIEW_TYPE_SECTION_NOT_LIVING
import com.acel.streamlivetool.util.AnchorClickAction.itemClick
import kotlinx.android.synthetic.main.item_overlay_list.view.*

class ListOverlayAdapter(val context: Context, val anchorList: List<Anchor>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder: RecyclerView.ViewHolder
        when (viewType) {
            VIEW_TYPE_SECTION_LIVING ->
                holder =
                    ViewHolderSection(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_overlay_section_living, parent, false)
                    )
            VIEW_TYPE_SECTION_NOT_LIVING ->
                holder =
                    ViewHolderSection(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_overlay_section_not_living, parent, false)
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
            AnchorSection.ANCHOR_SECTION_LIVING ->
                VIEW_TYPE_SECTION_LIVING
            AnchorSection.ANCHOR_SECTION_NOT_LIVING ->
                VIEW_TYPE_SECTION_NOT_LIVING
            else ->
                VIEW_TYPE_ANCHOR
        }
    }


    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderSection)
            return
        holder as ViewHolder
        val anchor: Anchor = anchorList[position]
        with(holder) {
            this.title.text = anchor.title ?: "-"
            //直播状态
            if (!anchorList.contains(AnchorSection.ANCHOR_SECTION_LIVING)
                && !anchorList.contains(AnchorSection.ANCHOR_SECTION_NOT_LIVING)
            ) {
                holder.status.visibility = View.VISIBLE
                if (anchor.status) {
                    this.status.text = context.getString(R.string.is_living)
                    this.status.setTextColor(Color.GREEN)
                } else {
                    this.status.text = context.getString(R.string.not_living)
                    this.status.setTextColor(Color.GRAY)
                }
            } else
                holder.status.visibility = View.GONE

            //主播名
            this.anchorName.text = anchor.nickname
            //平台名
            this.platform.text =
                PlatformDispatcher.getPlatformImpl(anchor.platform)?.platformName ?: "unknown"
            //item click
            this.itemView.setOnClickListener {
                itemClick(context, anchor, anchorList)
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

    class ViewHolderSection(itemView: View) : RecyclerView.ViewHolder(itemView)

}
