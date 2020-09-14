package com.acel.streamlivetool.ui.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.main.adapter.anchor_additional.AdditionalActionManager
import com.acel.streamlivetool.ui.main.adapter.AnchorGroupingListener.Companion.STATUS_LIVING
import com.acel.streamlivetool.ui.main.adapter.AnchorGroupingListener.Companion.STATUS_NOT_LIVING
import com.acel.streamlivetool.util.ActionClick.itemClick
import com.acel.streamlivetool.util.ActionClick.secondBtnClick
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.PreferenceConstant.fullVersion
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.item_graphic_anchor.view.*
import kotlinx.android.synthetic.main.text_view_graphic_secondary_status.view.*
import kotlinx.android.synthetic.main.text_view_graphic_type_name.view.*


class GraphicAnchorAdapter(
    private val context: Context,
    private val anchorList: List<Anchor>,
    private val modeType: Int,
    private val showAnchorImage: Boolean
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AnchorAdapterWrapper {
    private var mPosition: Int = -1
    private val additionalAction = AdditionalActionManager.instance

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder: RecyclerView.ViewHolder
        when (viewType) {
            VIEW_TYPE_LIVING_GROUP_TITLE ->
                holder = ViewHolderStatusGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_status_title_living, parent, false)
                        .also {
                            (it.layoutParams as StaggeredGridLayoutManager.LayoutParams)
                                .isFullSpan = true
                            it.tag = AnchorGroupingListener.STATUS_GROUP_TITLE_LIVING
                        }
                )
            VIEW_TYPE_NOT_LIVING_GROUP_TITLE ->
                holder = ViewHolderStatusGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_status_title_not_living, parent, false)
                        .also {
                            (it.layoutParams as StaggeredGridLayoutManager.LayoutParams)
                                .isFullSpan = true
                            it.tag =
                                AnchorGroupingListener.STATUS_GROUP_TITLE_NOT_LIVING
                        }
                )
            VIEW_TYPE_ANCHOR_SIMPLIFY ->
                holder = ViewHolderGraphic(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_graphic_anchor_simplify, parent, false)
                        .also {
                            (it.layoutParams as StaggeredGridLayoutManager.LayoutParams)
                                .isFullSpan = true
                        }
                )
            else ->
                //是否显示图片
                holder = if (showAnchorImage) ViewHolderGraphic(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_graphic_anchor, parent, false)
                ) else ViewHolderGraphic(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_graphic_anchor_simplify, parent, false)
                        .also {
                            (it.layoutParams as StaggeredGridLayoutManager.LayoutParams)
                                .isFullSpan = true
                        }
                )
        }
        return holder
    }

    override fun getItemCount(): Int {
        return anchorList.size
    }

    override fun getItemViewType(position: Int): Int {
        try {
            return when (anchorList[position]) {
                AnchorStatusGroup.LIVING_GROUP ->
                    VIEW_TYPE_LIVING_GROUP_TITLE
                AnchorStatusGroup.NOT_LIVING_GROUP ->
                    VIEW_TYPE_NOT_LIVING_GROUP_TITLE
                else -> {
                    if (anchorList[position].status)
                        VIEW_TYPE_ANCHOR
                    else
                        VIEW_TYPE_ANCHOR_SIMPLIFY
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return VIEW_TYPE_ANCHOR
        }
    }


    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderStatusGroup)
            return
        val anchor: Anchor = anchorList[position]
        holder as ViewHolderGraphic
        holder.itemView.tag = if (anchor.status) STATUS_LIVING else STATUS_NOT_LIVING
        //主播名
        holder.anchorName.text = anchor.nickname
        //平台名
        if (modeType == MODE_GROUP) {
            holder.platform.text =
                PlatformDispatcher.getPlatformImpl(anchor.platform)?.platformName ?: "unknown"
        } else {
            holder.platform.visibility = View.GONE
        }
        //直播类型
        if (anchor.typeName != null) {
            holder.typeName.text = anchor.typeName
            holder.typeName.visibility = View.VISIBLE
        } else
            holder.typeName.visibility = View.GONE

        //title
        holder.title.text = anchor.title ?: "-"

        //roomid
        holder.roomId.text = anchor.showId

        //头像
        with(anchor.avatar) {
            if (this != null) {
                ImageLoader.load(
                    context,
                    this,
                    holder.avatar
                )
            } else {
                holder.avatar.setImageResource(R.drawable.ic_load_img_fail)
            }
        }

        //图片    如果直播中则加载
        if (getItemViewType(position) == VIEW_TYPE_ANCHOR)
            with(anchor.keyFrame) {
                if (this != null) {
                    ImageLoader.load(
                        context,
                        this,
                        holder.image
                    )
                } else
                    holder.image.setImageResource(R.drawable.ic_load_img_fail)
            }
        //直播状态
        if (!anchorList.contains(AnchorStatusGroup.LIVING_GROUP)
            && !anchorList.contains(AnchorStatusGroup.NOT_LIVING_GROUP)
        ) {
            holder.status.visibility = View.VISIBLE
            if (anchor.status) {
                holder.status.text = context.getString(R.string.is_living)
                holder.status.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                holder.status.text = context.getString(R.string.not_living)
                holder.status.setTextColor(Color.WHITE)
            }
        } else
            holder.status.visibility = View.GONE
        //二级状态
        with(anchor.secondaryStatus) {
            if (this != null && isNotEmpty()) {
                holder.secondaryStatus.visibility = View.VISIBLE
                holder.secondaryStatus.text = this
            } else {
                holder.secondaryStatus.visibility = View.GONE
            }
        }

        //直播间Id
//        viewHolder.roomId.text = anchor.showId

        //item click
        holder.itemView.setOnClickListener {
            itemClick(context, anchor, anchorList)
        }

        //长按
        holder.itemView.setOnLongClickListener {
            mPosition = position
            return@setOnLongClickListener false
        }

        //副键点击
        if (fullVersion) {
            holder.secondBtn.visibility = View.VISIBLE
            holder.secondBtn.setOnClickListener {
                secondBtnClick(context, anchor, anchorList)
            }
        }

        //附加功能按钮
        if (defaultSharedPreferences.getBoolean(
                context.getString(R.string.pref_key_additional_action_btn),
                false
            ) && additionalAction.match(anchor)
        ) {
            val actionName = additionalAction.getActionName(anchor)
            holder.additionBtn.contentDescription = actionName
            holder.additionBtn.visibility = View.VISIBLE
            holder.additionBtn.setOnClickListener {
                MainExecutor.execute {
                    additionalAction.doAdditionalAction(anchor, context)
                }
            }
            holder.additionBtn.setOnLongClickListener {
                toast(actionName)
                return@setOnLongClickListener true
            }
        } else {
            holder.additionBtn.visibility = View.GONE
        }
    }

    override fun getLongClickPosition(): Int = mPosition
    override fun notifyAnchorsChange() = notifyDataSetChanged()

    inner class ViewHolderGraphic(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.setHeaderTitle("${anchorName.text}(${roomId.text})")
            when (modeType) {
                MODE_GROUP -> {
                    (itemView.context as AppCompatActivity).menuInflater.inflate(
                        R.menu.anchor_item_menu,
                        menu
                    )
                }
                MODE_COOKIE ->
                    (itemView.context as AppCompatActivity).menuInflater.inflate(
                        R.menu.anchor_item_menu_cookie_mode,
                        menu
                    )
            }
        }

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        val anchorName: TextView = itemView.grid_anchor_name
        val platform: TextView = itemView.grid_anchor_platform
        val image: ImageView = itemView.grid_anchor_image
        val avatar: ImageView = itemView.grid_anchor_avatar
        val status: TextView = itemView.grid_anchor_status
        val secondBtn: ImageView = itemView.grid_anchor_second_btn
        val title: TextView = itemView.grid_anchor_title
        val additionBtn: ImageView = itemView.grid_anchor_addition_action
        val secondaryStatus: TextView = itemView.grid_anchor_secondary_status
        val roomId: TextView = itemView.grid_anchor_roomId
        val typeName: TextView = itemView.grid_anchor_type_name
    }

}
