package com.acel.streamlivetool.ui.group_mode

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.ActionClick.itemClick
import com.acel.streamlivetool.ui.ActionClick.secondBtnClick
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.item_list_overlay.view.*
import kotlinx.android.synthetic.main.item_recycler_anchor.view.*
import kotlinx.android.synthetic.main.item_recycler_anchor.view.anchor_name
import kotlinx.android.synthetic.main.item_recycler_anchor.view.anchor_title


class ListOverlayAdapter(
    val groupModeActivity: GroupModeActivity,
    private val presenter: GroupModePresenter
) : RecyclerView.Adapter<ListOverlayAdapter.ViewHolder>() {

    private val platformNameMap: MutableMap<String, String> = mutableMapOf()
    private var mPosition: Int = -1

    private val fullVersion =
        defaultSharedPreferences.getBoolean(
            groupModeActivity.getString(R.string.full_version),
            false
        )

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_list_overlay, p0, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
        val anchor: Anchor = presenter.sortedAnchorList[p1]
        holder.let { viewHolder ->
            presenter.anchorAttributeMap.value?.get(anchor.anchorKey()).let {
                viewHolder.title.text =
                    it?.title ?: "-"
                //直播状态
                if (it?.isLive != null) {
                    if (it.isLive) {
                        viewHolder.status.text = "直播中"
                        viewHolder.status.setTextColor(Color.GREEN)
                    } else {
                        viewHolder.status.text = "未直播"
                        viewHolder.status.setTextColor(Color.GRAY)
                    }
                } else {
                    viewHolder.status.text = ""
                }
            }
            //主播名
            viewHolder.anchorName.text = anchor.nickname
            //平台名
            var platformName: String? =
                platformNameMap[anchor.platform]
            if (platformName == null) {
                val resInt =
                    PlatformDispatcher.getPlatformImpl(anchor.platform)?.platformShowNameRes
                if (resInt != null)
                    platformName = groupModeActivity.getString(resInt)
            }
            viewHolder.platform.text = platformName ?: "未知平台"
            //item click
            viewHolder.itemView.setOnClickListener {
                itemClick(groupModeActivity, anchor)
            }
        }
    }

    override fun getItemCount(): Int = presenter.sortedAnchorList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val anchorName: TextView = itemView.anchor_name
        val platform: TextView = itemView.anchor_platform
        val status: TextView = itemView.anchor_status
        val title: TextView = itemView.anchor_title

    }

    fun getPosition(): Int {
        return mPosition
    }

}
