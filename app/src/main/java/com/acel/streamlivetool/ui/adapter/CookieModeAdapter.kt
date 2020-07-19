package com.acel.streamlivetool.ui.adapter

import android.graphics.Color
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.AnchorsCookieMode
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.ActionClick.itemClick
import com.acel.streamlivetool.ui.ActionClick.secondBtnClick
import com.acel.streamlivetool.ui.cookie_mode.CookieModeActivity
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.item_recycler_anchor.view.*

class CookieModeAdapter(
    private val cookieAnchorActivity: CookieModeActivity,
    private val anchors: MutableList<AnchorsCookieMode.Anchor>
) : RecyclerView.Adapter<CookieModeAdapter.ViewHolder>() {

    private val platformNameMap: MutableMap<String, String> = mutableMapOf()
    private var mPosition: Int = -1
    private val fullVersion =
        defaultSharedPreferences.getBoolean(
            cookieAnchorActivity.getString(R.string.full_version),
            false
        )

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_recycler_anchor, p0, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val anchor = anchors[position]
        with(viewHolder) {
            //标题
            this.title.text = anchor.title
            //主播名
            this.anchorName.text = anchor.nickname
            //平台名
            var platformName: String? =
                platformNameMap[anchor.platform]
            if (platformName == null) {
                val resInt =
                    PlatformDispatcher.getPlatformImpl(anchor.platform)?.platformShowNameRes
                if (resInt != null)
                    platformName = cookieAnchorActivity.getString(resInt)
            }
            platformNameMap[anchor.platform] = platformName ?: "未知平台"
            this.platform.text = platformName
            //直播间Id
            this.roomId.text = anchor.showId
            //直播状态
            if (anchor.status) {
                this.status.text = "直播中"
                this.status.setTextColor(Color.GREEN)
            } else {
                this.status.text = "未直播"
                this.status.setTextColor(Color.GRAY)
            }
            //item click
            this.itemView.setOnClickListener {
                itemClick(cookieAnchorActivity, anchor)
            }

            // 长按菜单
            viewHolder.itemView.setOnLongClickListener {
                mPosition = viewHolder.bindingAdapterPosition
                return@setOnLongClickListener false
            }

            //侧键点击
            if (fullVersion) {
                viewHolder.secondBtn.visibility = View.VISIBLE
                viewHolder.secondBtn.setOnClickListener {
                    secondBtnClick(cookieAnchorActivity, anchor)
                }
            }
        }
    }

    override fun getItemCount(): Int = anchors.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        val anchorName: TextView = itemView.anchor_name
        val platform: TextView = itemView.main_anchor_platform
        val roomId: TextView = itemView.main_anchor_roomId
        val status: TextView = itemView.main_anchor_status
        val secondBtn: ImageView = itemView.main_second_btn
        val title: TextView = itemView.anchor_title
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            cookieAnchorActivity.menuInflater.inflate(R.menu.anchor_item_menu_cookie_mode, menu)
        }

        init {
            itemView.setOnCreateContextMenuListener(this)
        }
    }
}
