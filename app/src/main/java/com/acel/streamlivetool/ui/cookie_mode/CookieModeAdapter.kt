package com.acel.streamlivetool.ui.cookie_mode

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
import kotlinx.android.synthetic.main.item_main_anchor.view.*

class CookieModeAdapter(
    private val cookieAnchorActivity: CookieModeActivity,
    private val anchors: MutableList<AnchorsCookieMode.Anchor>
) : RecyclerView.Adapter<CookieModeAdapter.ViewHolder>() {

    private val platformNameMap: MutableMap<String, String> = mutableMapOf()

    private var mPosition: Int = -1

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_main_anchor, p0, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val anchor = anchors[position]
        viewHolder.let {
            //标题
            it.title.text = anchor.title
            //主播名
            it.anchorName.text = anchor.nickname
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
            it.platform.text = platformName
            //直播间Id
            it.roomId.text = anchor.showId
            //直播状态
            if (anchor.status) {
                it.status.text = "直播中"
                it.status.setTextColor(Color.GREEN)
            } else {
                it.status.text = "未直播"
                it.status.setTextColor(Color.GRAY)
            }
            //item click
            it.itemView.setOnClickListener {
                itemClick(cookieAnchorActivity, anchor)
            }

            // 长按菜单
            viewHolder.itemView.setOnLongClickListener {
                mPosition = viewHolder.bindingAdapterPosition
                return@setOnLongClickListener false
            }

            //侧键点击
            it.secondBtn.setOnClickListener {
                secondBtnClick(cookieAnchorActivity, anchor)
            }
        }
    }

    override fun getItemCount(): Int = anchors.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        val anchorName: TextView = itemView.main_anchor_name
        val platform: TextView = itemView.main_anchor_platform
        val roomId: TextView = itemView.main_anchor_roomId
        val status: TextView = itemView.main_anchor_status
        val secondBtn: ImageView = itemView.main_second_btn
        val title: TextView = itemView.main_anchor_title
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

    fun getPosition(): Int {
        return mPosition
    }

}
