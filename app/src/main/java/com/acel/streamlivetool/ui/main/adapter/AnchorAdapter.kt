package com.acel.streamlivetool.ui.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.anchor_additional.AdditionalActionManager
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.const_value.ConstValue.FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR
import com.acel.streamlivetool.const_value.ConstValue.ITEM_ID_FOLLOW_ANCHOR
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.platform.PlatformDispatcher.platformImpl
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.util.AnchorClickAction.secondBtnClick
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.PreferenceVariable.showAdditionalActionButton
import kotlinx.android.synthetic.main.item_anchor.view.*
import kotlinx.android.synthetic.main.text_view_graphic_secondary_status.view.*
import kotlinx.android.synthetic.main.text_view_type_name.view.*


class AnchorAdapter(
    private val context: Context,
    private val anchorList: List<Anchor>,
    private val modeType: Int,
    var showImage: Boolean,
    private val iconDrawable: Drawable
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mPosition: Int = -1
    private val additionalActionManager = AdditionalActionManager.instance
    private val onlineImageSpan = ImageSpan(context, R.drawable.ic_online_hot)

    private var livingSectionPosition = -1
    private var notLivingSectionPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder: RecyclerView.ViewHolder
        when (viewType) {
            VIEW_TYPE_SECTION_LIVING ->
                holder = ViewHolderGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_section_living, parent, false)
                        .also {
//                            (it.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan =
//                                true
                            iconDrawable.setBounds(0, 0, 40, 40)
                            it.findViewById<TextView>(R.id.status)?.apply {
                                setCompoundDrawables(null, null, iconDrawable, null)
                            }
                        }
                )
            VIEW_TYPE_SECTION_NOT_LIVING ->
                holder = ViewHolderGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_section_not_living, parent, false)
                        .also {
//                            (it.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan =
//                                true
                            iconDrawable.setBounds(0, 0, 40, 40)
                            it.findViewById<TextView>(R.id.status)?.apply {
                                setCompoundDrawables(null, null, iconDrawable, null)
                            }
                        }
                )
            VIEW_TYPE_ANCHOR_SIMPLIFY ->
                holder = ViewHolderGraphic(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_anchor_simplify, parent, false)
                )
            else ->
                //是否显示图片
                holder = if (showImage) ViewHolderGraphic(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_anchor, parent, false)
                ) else ViewHolderGraphic(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_anchor_simplify, parent, false)
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
                AnchorSection.ANCHOR_SECTION_LIVING ->
                    VIEW_TYPE_SECTION_LIVING
                AnchorSection.ANCHOR_SECTION_NOT_LIVING ->
                    VIEW_TYPE_SECTION_NOT_LIVING
                else -> {
                    if (anchorList[position].status && showImage)
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

    private val spannableStringBuffer = SpannableStringBuilder()

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderGroup)
            return

        val anchor: Anchor = anchorList[position]
        holder as ViewHolderGraphic

        //主播名
        holder.anchorName.text = anchor.nickname
        //平台名
        holder.platform.visibility = View.GONE

        //平台图标
        if (modeType == MODE_GROUP) {
            anchor.platformImpl()?.iconRes?.let {
                holder.icon?.setImageResource(it)
            }
        }

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
                ImageLoader.load(context, this, holder.avatar)
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
                    spannableStringBuffer.clear()
                    spannableStringBuffer.append(" $this")
//                    val span = SpannableString("  $this")
                    spannableStringBuffer.setSpan(
                        onlineImageSpan,
                        0,
                        1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    holder.online?.text = spannableStringBuffer
                }
            }
        }

        //直播状态
//        if (!anchorList.contains(AnchorStatusGroup.LIVING_GROUP)
//            && !anchorList.contains(AnchorStatusGroup.NOT_LIVING_GROUP)
//        ) {
//            holder.status.visibility = View.VISIBLE
//            if (anchor.status) {
//                holder.status.text = context.getString(R.string.is_living)
//                holder.status.setTextColor(Color.parseColor("#4CAF50"))
//            } else {
//                holder.status.text = context.getString(R.string.not_living)
//                holder.status.setTextColor(Color.WHITE)
//            }
//        } else
//            holder.status.visibility = View.GONE
        //二级状态
        with(anchor.secondaryStatus) {
            if (this != null && isNotEmpty()) {
                holder.secondaryStatus.visibility = View.VISIBLE
                holder.secondaryStatus.text = this
            } else {
                holder.secondaryStatus.visibility = View.GONE
            }
        }

        //item click
        holder.itemView.setOnClickListener {
            context as MainActivity
            context.itemClick(it, context, anchor, anchorList)
//            itemClick(context, anchor, anchorList)
        }

        //长按
        holder.itemView.setOnLongClickListener {
            mPosition = position
            return@setOnLongClickListener false
        }

        //第二按键点击
        holder.secondBtn.visibility = View.VISIBLE
        holder.secondBtn.setOnClickListener {
            secondBtnClick(context, anchor, anchorList)
        }

        //附加功能按钮
        if (showAdditionalActionButton) {
            val match = additionalActionManager.match(anchor)
            if (match) {
                val actions = additionalActionManager.getActions(anchor)
                actions?.let {
                    holder.additionBtn.apply {
                        visibility = View.VISIBLE
                        if (it.size == 1)
                            setImageResource(it[0].iconResourceId)
                        else
                            setImageResource(R.drawable.ic_additional_button)
                        setOnClickListener {
                            MainExecutor.execute {
                                additionalActionManager.doActions(anchor, context)
                            }
                        }
                    }
                }
            } else
                holder.additionBtn.visibility = View.GONE
        } else {
            holder.additionBtn.visibility = View.GONE
        }
    }

    fun getLongClickPosition(): Int = mPosition

    fun notifyAnchorsChange() {
        livingSectionPosition = anchorList.indexOf(AnchorSection.ANCHOR_SECTION_LIVING)
        notLivingSectionPosition = anchorList.indexOf(AnchorSection.ANCHOR_SECTION_NOT_LIVING)
        notifyDataSetChanged()
    }

    fun getNotLivingSectionPosition(): Int = notLivingSectionPosition
    fun getLivingSectionPosition(): Int = livingSectionPosition

    /**
     * @return int size , null follow default
     */
    fun isFullSpan(position: Int): Boolean {
        return when (getItemViewType(position)) {
            VIEW_TYPE_SECTION_LIVING, VIEW_TYPE_SECTION_NOT_LIVING, VIEW_TYPE_ANCHOR_SIMPLIFY ->
                true
            else -> false
        }
    }

    inner class ViewHolderGraphic(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.setHeaderTitle("${anchorName.text}(${roomId.text})")
            if (liveTime.text.isNotEmpty())
                menu?.add(context.getString(R.string.live_time_formatter, liveTime.text))
            val subMenu = menu?.addSubMenu("打开为")
            (itemView.context as AppCompatActivity).menuInflater.inflate(
                R.menu.anchor_item_menu_open_as, subMenu
            )
            when (modeType) {
                MODE_GROUP -> {
                    menu?.add("编辑")?.apply {
                        setOnMenuItemClickListener {
                            val position = getLongClickPosition()
                            val anchor = anchorList[position]
                            (context as MainActivity).showEditAnchorFragment(anchor)
                            true
                        }
                    }
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

        //        val status: TextView = itemView.grid_anchor_status
        val secondBtn: ImageView = itemView.grid_anchor_second_btn
        val title: TextView = itemView.grid_anchor_title
        val additionBtn: ImageView = itemView.grid_anchor_addition_action
        val secondaryStatus: TextView = itemView.secondary_status
        val roomId: TextView = itemView.grid_anchor_roomId
        val typeName: TextView = itemView.type_name
        val online: TextView? = itemView.grid_anchor_online ?: null
        val liveTime: TextView = itemView.grid_anchor_live_time
        val icon: ImageView? = itemView.platform_icon
    }
}
