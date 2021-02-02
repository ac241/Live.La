package com.acel.streamlivetool.ui.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.anchor_additional.AdditionalActionManager
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.const_value.ConstValue.FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR
import com.acel.streamlivetool.const_value.ConstValue.ITEM_ID_FOLLOW_ANCHOR
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.main.adapter.AnchorGroupingListener.Companion.STATUS_LIVING
import com.acel.streamlivetool.ui.main.adapter.AnchorGroupingListener.Companion.STATUS_NOT_LIVING
import com.acel.streamlivetool.util.AnchorClickAction.itemClick
import com.acel.streamlivetool.util.AnchorClickAction.secondBtnClick
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.item_graphic_anchor.view.*
import kotlinx.android.synthetic.main.text_view_graphic_secondary_status.view.*
import kotlinx.android.synthetic.main.text_view_graphic_type_name.view.*


class AnchorAdapter(
    private val context: Context,
    private val anchorList: List<Anchor>,
    private val modeType: Int,
    private val showAnchorImage: Boolean
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mPosition: Int = -1
    private val additionalActionManager = AdditionalActionManager.instance
    private val onlineImageSpan =
        ImageSpan(context, R.drawable.ic_online_hot)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder: RecyclerView.ViewHolder
        when (viewType) {
            VIEW_TYPE_LIVING_GROUP_TITLE ->
                holder = ViewHolderGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_group_living, parent, false)
                        .also {
                            (it.layoutParams as StaggeredGridLayoutManager.LayoutParams)
                                .isFullSpan = true
                            it.tag = AnchorGroupingListener.STATUS_GROUP_TITLE_LIVING
                        }
                )
            VIEW_TYPE_NOT_LIVING_GROUP_TITLE ->
                holder = ViewHolderGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_group_not_living, parent, false)
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
        if (holder is ViewHolderGroup)
            return
        val anchor: Anchor = anchorList[position]
        holder as ViewHolderGraphic
        holder.itemView.tag = if (anchor.status) STATUS_LIVING else STATUS_NOT_LIVING
        //主播名
        holder.anchorName.text = anchor.nickname
        //平台名
        holder.platform.visibility = View.GONE
        //平台图标
        if (modeType == MODE_GROUP) {
            PlatformDispatcher.getPlatformImpl(anchor)?.iconRes?.let {
                holder.icon?.setImageResource(it)
            }
        }
//        if (modeType == MODE_GROUP) {
//            if (getItemViewType(position) == VIEW_TYPE_ANCHOR) {
//                holder.platform.visibility = View.GONE
//                PlatformDispatcher.getPlatformImpl(anchor)?.iconRes?.let {
//                    holder.icon?.setImageResource(it)
//                }
//            } else
//                holder.platform.text =
//                    PlatformDispatcher.getPlatformImpl(anchor)?.platformName ?: "unknown"
//        } else {
//            holder.platform.visibility = View.GONE
//        }
        //直播类型
        if (anchor.typeName != null) {
            holder.typeName.text = anchor.typeName
            holder.typeName.visibility = View.VISIBLE
        } else
            holder.typeName.visibility = View.GONE

        //title

        holder.title.text = anchor.title ?: "-"

        //直播时间
        holder.liveTime.text = anchor.liveTime ?: ""

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
        //如果是显图类型
        if (getItemViewType(position) == VIEW_TYPE_ANCHOR) {
            //图片
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
            //热度
            anchor.online?.apply {
                if (isNotEmpty()) {
                    val span = SpannableString("  $this")
                    span.setSpan(onlineImageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    holder.online?.text = span
                }
            }
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
            Log.d("onBindViewHolder", "${getLongClickPosition()}")
            return@setOnLongClickListener false
        }

        //第二按键点击
        holder.secondBtn.visibility = View.VISIBLE
        holder.secondBtn.setOnClickListener {
            secondBtnClick(context, anchor, anchorList)
        }

        //附加功能按钮
        val actions = additionalActionManager.match(anchor)
        if (
            defaultSharedPreferences.getBoolean(
                context.getString(R.string.pref_key_additional_action_btn),
                false
            ) && actions != null
        ) {
            holder.additionBtn.apply {
                visibility = View.VISIBLE
                if (actions.size == 1)
                    setImageResource(actions[0].iconResourceId)
                else
                    setImageResource(R.drawable.ic_additional_button)
                setOnClickListener {
                    MainExecutor.execute {
                        additionalActionManager.doActions(anchor, context)
                    }
                }
            }
        } else {
            holder.additionBtn.visibility = View.GONE
        }
    }

    fun getLongClickPosition(): Int = mPosition
    fun notifyAnchorsChange() = notifyDataSetChanged()

    inner class ViewHolderGraphic(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            val subMenu = menu?.addSubMenu("打开为")
            (itemView.context as AppCompatActivity).menuInflater.inflate(
                R.menu.anchor_item_menu_open_as, subMenu
            )
            menu?.setHeaderTitle("${anchorName.text}(${roomId.text})")
            if (liveTime.text.isNotEmpty())
                menu?.add(context.getString(R.string.live_time_formatter, liveTime.text))
            when (modeType) {
                MODE_GROUP -> {
                    (itemView.context as AppCompatActivity).menuInflater.inflate(
                        R.menu.anchor_item_menu,
                        menu
                    )
                    if (title.text == FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR) {
                        menu?.add(Menu.NONE, ITEM_ID_FOLLOW_ANCHOR, Menu.NONE, "关注该主播")
                    }
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
        val typeName: TextView = itemView.type_name
        val online: TextView? = itemView.grid_anchor_online ?: null
        val liveTime: TextView = itemView.grid_anchor_live_time
        val icon: ImageView? = itemView.platform_icon
    }

}
