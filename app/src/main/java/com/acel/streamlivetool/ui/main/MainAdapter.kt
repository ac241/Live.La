package com.acel.streamlivetool.ui.main

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformPitcher
import kotlinx.android.synthetic.main.item_main_anchor.view.*


class MainAdapter(
    val mainActivity: MainActivity,
    private val anchorList: MutableList<Anchor>,
    private val anchorStatusMap: MutableMap<String, Boolean>
) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    private val platformNameMap: MutableMap<String, String> = mutableMapOf()

    private var mPosition: Int = -1

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_main_anchor, p0, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val anchor: Anchor = anchorList[p1]
        p0.let { viewholder ->
            //主播名
            viewholder.anchorName.text = anchor.nickname
            //平台名
            var platformName: String? =
                platformNameMap[anchor.getPlatform()]
            if (platformName == null) {
                val resInt = PlatformPitcher.getPlatformImpl(anchor.platform)?.platformShowNameRes
                if (resInt != null)
                    platformName = mainActivity.getString(resInt)
            }
            platformNameMap[anchor.platform] = platformName ?: "未知平台"
            viewholder.platform.text = platformName
            //直播间Id
            viewholder.roomId.text = anchor.showId
            //直播状态
            if (anchorStatusMap[anchor.anchorKey] != null) {
                if (anchorStatusMap[anchor.anchorKey]!!) {
                    viewholder.status.text = "直播中"
                    viewholder.status.setTextColor(Color.GREEN)
                } else {
                    viewholder.status.text = "未直播"
                    viewholder.status.setTextColor(Color.GRAY)
                }
            } else {
                viewholder.status.text = ""
            }
            //item click
            viewholder.itemView.setOnClickListener {
                mainActivity.presenter.itemClick(anchor)
            }

            //长按菜单
            viewholder.itemView.setOnLongClickListener {
                mPosition = viewholder.adapterPosition
                return@setOnLongClickListener false
            }

            //侧键点击
            viewholder.secondBtn.setOnClickListener {
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

    }

    fun getPosition(): Int {
        return mPosition
    }

}
