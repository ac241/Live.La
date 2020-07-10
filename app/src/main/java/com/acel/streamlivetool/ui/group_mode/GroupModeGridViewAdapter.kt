package com.acel.streamlivetool.ui.group_mode

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.ActionClick.itemClick
import kotlinx.android.synthetic.main.item_grid_anchor.view.*


class GroupModeGridViewAdapter(
    val context: Context,
    private val anchorList: MutableLiveData<MutableList<Anchor>>,
    private val anchorAttributeMap: MutableMap<String, AnchorAttribute>
) : BaseAdapter() {

    private val platformNameMap: MutableMap<String, String> = mutableMapOf()
    private var mPosition: Int = -1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            (context as AppCompatActivity).menuInflater.inflate(R.menu.anchor_item_menu, menu)
        }

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        val anchorName: TextView = itemView.grid_anchor_name
        val platform: TextView = itemView.grid_anchor_platform
        val image: ImageView = itemView.grid_anchor_image
        val avatar: ImageView = itemView.grid_anchor_avatar

        //        val roomId: TextView = itemView.main_anchor_roomId
        val status: TextView = itemView.grid_anchor_status

        //        val secondBtn: ImageView = itemView.main_second_btn
        val title: TextView = itemView.grid_anchor_title
    }

    fun getPosition(): Int {
        return mPosition
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.d("getView", "$convertView")
        var view = convertView
        val viewHolder: ViewHolder
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_grid_anchor, null)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else
            viewHolder = convertView?.tag as ViewHolder

        convertView?.tag = viewHolder
        val anchor: Anchor = anchorList.value!![position]
        //
        viewHolder.title.text = anchorAttributeMap[anchor.anchorKey]?.title ?: "-"
        //主播名
        viewHolder.anchorName.text = anchor.nickname
        //平台名
        var platformName: String? =
            platformNameMap[anchor.platform]
        if (platformName == null) {
            val resInt =
                PlatformDispatcher.getPlatformImpl(anchor.platform)?.platformShowNameRes
            if (resInt != null)
                platformName = context.getString(resInt)
        }
        platformNameMap[anchor.platform] = platformName ?: "未知平台"
        viewHolder.platform.text = platformName

        //头像
        with(anchorAttributeMap[anchor.anchorKey]?.avatar) {
            if (this != null) {
                ImageLoader.load(
                    context,
                    this,
                    viewHolder.avatar
                )
            } else {
                viewHolder.avatar.setImageResource(R.drawable.ic_load_img_fail)
            }
        }

        //图片
        with(anchorAttributeMap[anchor.anchorKey]?.image) {
            if (this != null) {
                ImageLoader.load(
                    context,
                    this,
                    viewHolder.image
                )
            } else
                viewHolder.image.setImageResource(R.drawable.ic_load_img_fail)
        }


        //直播间Id
//        viewHolder.roomId.text = anchor.showId
//        直播状态
        if (anchorAttributeMap[anchor.anchorKey]?.isLive != null) {
            if (anchorAttributeMap[anchor.anchorKey]?.isLive!!) {
                viewHolder.status.text = "直播中"
                viewHolder.status.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                viewHolder.status.text = "未直播"
                viewHolder.status.setTextColor(Color.WHITE)
            }
        } else {
            viewHolder.status.text = ""
        }

        //item click
        viewHolder.itemView.setOnClickListener {
            itemClick(context, anchor)
        }

        //长按
        viewHolder.itemView.setOnLongClickListener {
            mPosition = position
            return@setOnLongClickListener false
        }

//        //侧键点击
//        viewHolder.secondBtn.setOnClickListener {
//            secondBtnClick(context, anchor)
//        }

        return view!!
    }

    override fun getItem(position: Int): Anchor? = anchorList.value?.get(position)

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = anchorList.value!!.size

}
