package com.acel.streamlivetool.ui.main.adapter

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
import com.acel.streamlivetool.util.ActionClick.itemClick
import kotlinx.android.synthetic.main.item_overlay_list.view.*


class ListOverlayAdapter(val context: Context, val anchorList: List<Anchor>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AnchorAdapterWrapper {

    private var mPosition: Int = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder: RecyclerView.ViewHolder
        when (viewType) {
            VIEW_TYPE_LIVING_GROUP_TITLE ->
                holder = ViewHolderStatusGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_overlay_status_living, parent, false)
                        .also {
                            it.tag =
                                AnchorListAddTitleListener.STATUS_GROUP_TITLE_LIVING
                        }
                )
            VIEW_TYPE_NOT_LIVING_GROUP_TITLE ->
                holder = ViewHolderStatusGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_overlay_status_not_living, parent, false)
                        .also {
                            it.tag =
                                AnchorListAddTitleListener.STATUS_GROUP_TITLE_NOT_LIVING
                        }
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
                VIEW_TYPE_LIVING_GROUP_TITLE
            AnchorPlaceHolder.anchorNotLiving ->
                VIEW_TYPE_NOT_LIVING_GROUP_TITLE
            else ->
                VIEW_TYPE_ANCHOR
        }
    }


    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderStatusGroup)
            return
        holder as ViewHolder
        val anchor: Anchor = anchorList[position]
        holder.itemView.tag =
            if (anchor.status) AnchorListAddTitleListener.STATUS_LIVING else AnchorListAddTitleListener.STATUS_NOT_LIVING
        with(holder) {
            this.title.text =
                anchor.title ?: "-"
            //直播状态
            if (!anchorList.contains(AnchorPlaceHolder.anchorIsLiving)
                && !anchorList.contains(AnchorPlaceHolder.anchorNotLiving)
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

    override fun getLongClickPosition(): Int {
        return mPosition
    }

    override fun notifyAnchorsChange() {
        notifyDataSetChanged()
    }
}
