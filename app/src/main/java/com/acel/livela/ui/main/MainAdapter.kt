package com.acel.livela.ui.main

import android.app.Activity
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.acel.livela.R
import com.acel.livela.bean.Anchor
import com.acel.livela.platform.PlatformPitcher
import kotlinx.android.synthetic.main.main_anchor_item.view.*
import org.jetbrains.anko.textColor


class MainAdapter(
    val mainActivity: MainActivity,
    val anchorList: MutableList<Anchor>,
    val anchorStatusMap: MutableMap<String, Boolean>
) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    val platformNameMap: MutableMap<String, String> = mutableMapOf()

    var mPosition: Int = -1

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.main_anchor_item, p0, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val anchor: Anchor = anchorList[p1]
        p0.let { it ->
            //主播名
            it.anchorName.text = anchor.nickname
            //平台名
            var platformName: String? =
                platformNameMap.get(anchor.getPlatform())
            if (platformName == null) {
                val resInt = PlatformPitcher.getPlatformImpl(anchor.platform)?.platformShowNameRes
                if (resInt != null)
                    platformName = mainActivity.getString(resInt)
            }
            platformNameMap.put(anchor.platform, if (platformName != null) platformName else "未知平台")
            it.platform.text = platformName
            //直播间Id
            it.roomId.text = anchor.showId
            //直播状态
            if (anchorStatusMap.get(anchor.anchorKey) != null)
                if (anchorStatusMap.get(anchor.anchorKey)!!) {
                    it.status.text = "直播中"
                    it.status.setTextColor(Color.GREEN)
                } else {
                    it.status.text = "未直播"
                    it.status.setTextColor(Color.GRAY)
                }

            it.itemView.setOnClickListener {
                //打开应用
//                PlatformPitcher.getPlatformImpl(anchor.platform)?.startApp(mainActivity, anchor)
                //打开播放器
                mainActivity.presenter.startPlay(anchor)
            }
            it.itemView.setOnLongClickListener { view ->
                mPosition = it.adapterPosition
                return@setOnLongClickListener false
            }
        }
    }

    override fun getItemCount(): Int = anchorList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            mainActivity.getMenuInflater().inflate(R.menu.main_item_menu, menu)
        }

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        val anchorName = itemView.main_anchor_name
        val platform = itemView.main_anchor_platform
        val roomId = itemView.main_anchor_roomId
        val status = itemView.main_anchor_status

    }

    fun getPosition(): Int {
        return mPosition
    }

}
