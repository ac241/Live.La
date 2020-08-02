package com.acel.streamlivetool.ui.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorPlaceHolder
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.platform.anchor_additional.AdditionalAction
import com.acel.streamlivetool.ui.main.adapter.AnchorListAddTitleListener.Companion.STATUS_LIVING
import com.acel.streamlivetool.ui.main.adapter.AnchorListAddTitleListener.Companion.STATUS_NOT_LIVING
import com.acel.streamlivetool.util.ActionClick.itemClick
import com.acel.streamlivetool.util.ActionClick.secondBtnClick
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.defaultSharedPreferences


class GraphicAnchorAdapter(
    private val context: Context,
    private val anchorList: List<Anchor>,
    private val modeType: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AnchorAdapterWrapper {
    companion object {
        const val VIEW_TYPE_NORMAL = 443
        const val VIEW_TYPE_LIVING_TITLE = 444
        const val VIEW_TYPE_NOT_LIVING_TITLE = 445
    }

    private var isScrolling = false
    override fun setScrolling(boolean: Boolean) {
        isScrolling = boolean
    }

    private val platformNameMap: MutableMap<String, String> = mutableMapOf()
    private var mPosition: Int = -1
    private val fullVersion by lazy {
        defaultSharedPreferences.getBoolean(
            context.getString(R.string.full_version),
            false
        )
    }

    private val additionalAction = AdditionalAction.instance

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder: RecyclerView.ViewHolder
        when (viewType) {
            VIEW_TYPE_LIVING_TITLE ->
                holder = ViewHolderStatusGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_status_title_living, parent, false)
                        .also {
                            (it.layoutParams as StaggeredGridLayoutManager.LayoutParams)
                                .isFullSpan = true
                            it.tag = AnchorListAddTitleListener.STATUS_GROUP_TITLE_LIVING
                        }
                )
            VIEW_TYPE_NOT_LIVING_TITLE ->
                holder = ViewHolderStatusGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_status_title_not_living, parent, false)
                        .also {
                            (it.layoutParams as StaggeredGridLayoutManager.LayoutParams)
                                .isFullSpan = true
                            it.tag =
                                AnchorListAddTitleListener.STATUS_GROUP_TITLE_NOT_LIVING
                        }
                )
            else ->
                holder = ViewHolderGraphic(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_graphic_anchor, parent, false)
                    , modeType
                )
        }
        return holder
    }

    override fun getItemCount(): Int {
        return anchorList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (anchorList[position]) {
            AnchorPlaceHolder.anchorIsLiving ->
                VIEW_TYPE_LIVING_TITLE
            AnchorPlaceHolder.anchorNotLiving ->
                VIEW_TYPE_NOT_LIVING_TITLE
            else ->
                VIEW_TYPE_NORMAL
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
        var platformName: String? =
            platformNameMap[anchor.platform]
        if (platformName == null) {
            val resInt =
                PlatformDispatcher.getPlatformImpl(anchor.platform)?.platformShowNameRes
            if (resInt != null)
                platformName = context.getString(resInt)
        }
        platformNameMap[anchor.platform] = platformName ?: "未知平台"
        holder.platform.text = platformName

        //title
        holder.title.text = anchor.title ?: "-"

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
        //直播状态
        if (!anchorList.contains(AnchorPlaceHolder.anchorIsLiving)
            && !anchorList.contains(AnchorPlaceHolder.anchorNotLiving)
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

        //直播间Id
//        viewHolder.roomId.text = anchor.showId

        //item click
        holder.itemView.setOnClickListener {
            itemClick(context, anchor)
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
                secondBtnClick(context, anchor)
            }
        }

        //附加功能按钮
        if (defaultSharedPreferences.getBoolean(
                context.getString(R.string.pref_key_additional_action_btn),
                false
            ) && additionalAction.check(anchor)
        ) {
            holder.additionBtn.visibility = View.VISIBLE
            holder.additionBtn.setOnClickListener {
                MainExecutor.execute {
                    additionalAction.doAdditionalAction(anchor, context)
                }
            }
        } else {
            holder.additionBtn.visibility = View.GONE
        }
    }


    override fun getLongClickPosition(): Int = mPosition
    override fun notifyAnchorsChange() = notifyDataSetChanged()

}
