package com.acel.streamlivetool.ui.main

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
import kotlinx.android.synthetic.main.item_main_anchor.view.*


class MainAdapter(
    val mainActivity: MainActivity,
    private val anchorList: MutableList<Anchor>,
    private val anchorStatusMap: MutableMap<String, Boolean>,
    private val anchorTitlieMap: MutableMap<String, String>
) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    private val platformNameMap: MutableMap<String, String> = mutableMapOf()

    private var mPosition: Int = -1

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_main_anchor, p0, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val anchor: Anchor = anchorList[p1]
        p0.let { viewHolder ->
            //
            viewHolder.title.text = anchorTitlieMap[anchor.anchorKey] ?: "-"
            //主播名
            viewHolder.anchorName.text = anchor.nickname
            //平台名
            var platformName: String? =
                platformNameMap[anchor.platform]
            if (platformName == null) {
                val resInt =
                    PlatformDispatcher.getPlatformImpl(anchor.platform)?.platformShowNameRes
                if (resInt != null)
                    platformName = mainActivity.getString(resInt)
            }
            platformNameMap[anchor.platform] = platformName ?: "未知平台"
            viewHolder.platform.text = platformName
            //直播间Id
            viewHolder.roomId.text = anchor.showId
            //直播状态
            if (anchorStatusMap[anchor.anchorKey] != null) {
                if (anchorStatusMap[anchor.anchorKey]!!) {
                    viewHolder.status.text = "直播中"
                    viewHolder.status.setTextColor(Color.GREEN)
                } else {
                    viewHolder.status.text = "未直播"
                    viewHolder.status.setTextColor(Color.GRAY)
                }
            } else {
                viewHolder.status.text = ""
            }
            //item click
            viewHolder.itemView.setOnClickListener {
                mainActivity.presenter.itemClick(anchor)
            }

            //长按菜单
            viewHolder.itemView.setOnLongClickListener {
                mPosition = viewHolder.bindingAdapterPosition
                return@setOnLongClickListener false
            }

            //侧键点击
            viewHolder.secondBtn.setOnClickListener {
                mainActivity.presenter.secondBtnClick(anchor)
            }
        }
    }

    override fun getItemCount(): Int = anchorList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            mainActivity.menuInflater.inflate(R.menu.main_item_menu, menu)
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
