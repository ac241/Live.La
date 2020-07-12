package com.acel.streamlivetool.ui.group_mode

import android.graphics.Color
import android.view.ContextMenu
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
import com.acel.streamlivetool.util.AppUtil
import kotlinx.android.synthetic.main.item_main_anchor.view.*


class GroupModeRecyclerViewAdapter(
    val groupModeActivity: GroupModeActivity,
    private val presenter: GroupModePresenter
) : RecyclerView.Adapter<GroupModeRecyclerViewAdapter.ViewHolder>() {

    private val platformNameMap: MutableMap<String, String> = mutableMapOf()
    private var mPosition: Int = -1

    private val fullVersion =
        AppUtil.defaultSharedPreferences.getBoolean(
            groupModeActivity.getString(R.string.full_version),
            false
        )

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_main_anchor, p0, false)
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
            platformNameMap[anchor.platform] = platformName ?: "未知平台"
            viewHolder.platform.text = platformName
            //直播间Id
            viewHolder.roomId.text = anchor.showId

            //item click
            viewHolder.itemView.setOnClickListener {
                itemClick(groupModeActivity, anchor)
            }

            //长按菜单
            viewHolder.itemView.setOnLongClickListener {
                mPosition = viewHolder.bindingAdapterPosition
                return@setOnLongClickListener false
            }

            //侧键点击
            if (fullVersion) {
                viewHolder.secondBtn.visibility = View.VISIBLE
                viewHolder.secondBtn.setOnClickListener {
                    secondBtnClick(groupModeActivity, anchor)
                }
            }
        }
    }

    override fun getItemCount(): Int = presenter.sortedAnchorList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            groupModeActivity.menuInflater.inflate(R.menu.anchor_item_menu, menu)
        }

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        val anchorName: TextView = itemView.main_anchor_name
        val platform: TextView = itemView.main_anchor_platform
        val roomId: TextView = itemView.main_anchor_roomId
        val status: TextView = itemView.main_anchor_status
        val secondBtn: ImageView = itemView.main_second_btn
        val title: TextView = itemView.main_anchor_title

    }

    fun getPosition(): Int {
        return mPosition
    }

}
