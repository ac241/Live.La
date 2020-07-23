package com.acel.streamlivetool.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorPlaceHolder
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.platform.anchor_additional.AdditionalAction
import com.acel.streamlivetool.ui.ActionClick.itemClick
import com.acel.streamlivetool.ui.ActionClick.secondBtnClick
import com.acel.streamlivetool.ui.cookie_mode.CookieModeActivity
import com.acel.streamlivetool.ui.group_mode.GroupModeActivity
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.defaultSharedPreferences


class TextAnchorAdapter(val context: Context, val anchorList: List<Anchor>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AnchorAdapterWrapper {

    private val modeType by lazy {
        when (context) {
            is CookieModeActivity ->
                ModeType.CookieMode
            is GroupModeActivity ->
                ModeType.GroupMode
            else ->
                ModeType.GroupMode
        }
    }

    private val platformNameMap: MutableMap<String, String> = mutableMapOf()
    private var mPosition: Int = -1
    private val additionalAction = AdditionalAction.instance

    private val fullVersion by lazy {
        defaultSharedPreferences.getBoolean(
            context.getString(R.string.full_version),
            false
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder: RecyclerView.ViewHolder
        when (viewType) {
            GraphicAnchorAdapter.VIEW_TYPE_LIVING ->
                holder = ViewHolderStatusGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_status_group, parent, false)
                )
            GraphicAnchorAdapter.VIEW_TYPE_SLEEPING ->
                holder = ViewHolderStatusGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_status_group, parent, false)
                )
            else ->
                holder = ViewHolderText(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_recycler_anchor, parent, false)
                    , modeType
                )
        }

        return holder
    }

    override fun getItemViewType(position: Int): Int {
        return when (anchorList[position]) {
            AnchorPlaceHolder.anchorIsLiving ->
                GraphicAnchorAdapter.VIEW_TYPE_LIVING
            AnchorPlaceHolder.anchorNotLiving ->
                GraphicAnchorAdapter.VIEW_TYPE_SLEEPING
            else ->
                GraphicAnchorAdapter.VIEW_TYPE_NORMAL
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderStatusGroup) {
            when (anchorList[position]) {
                AnchorPlaceHolder.anchorIsLiving ->
                    holder.statusText.text = context.getString(R.string.is_living)
                AnchorPlaceHolder.anchorNotLiving ->
                    holder.statusText.text = context.getString(R.string.not_living)
            }
            return
        }
        holder as ViewHolderText

        val anchor: Anchor = anchorList[position]
        with(holder) {
            //title
            holder.title.text =
                anchor.title ?: "-"
            //直播状态
            if (!anchorList.contains(AnchorPlaceHolder.anchorIsLiving)
                && !anchorList.contains(AnchorPlaceHolder.anchorIsLiving)
            ) {
                holder.status.visibility = View.VISIBLE
                if (anchor.status) {
                    holder.status.text = "直播中"
                    holder.status.setTextColor(Color.GREEN)
                } else {
                    holder.status.text = "未直播"
                    holder.status.setTextColor(Color.GRAY)
                }
                holder.title.text =
                    anchor.title
            } else
                holder.status.visibility = View.GONE

            //主播名
            this.anchorName.text = anchor.nickname
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
            this.platform.text = platformName
            //直播间Id
            this.roomId.text = anchor.showId

            //item click
            this.itemView.setOnClickListener {
                itemClick(context, anchor)
            }

            //长按菜单
            this.itemView.setOnLongClickListener {
                mPosition = this.bindingAdapterPosition
                return@setOnLongClickListener false
            }

            //侧键点击
            if (fullVersion) {
                this.secondBtn.visibility = View.VISIBLE
                this.secondBtn.setOnClickListener {
                    secondBtnClick(context, anchor)
                }
            }
            //附加功能按钮
            if (defaultSharedPreferences.getBoolean(
                    context.getString(R.string.pref_key_additional_action_btn),
                    false
                ) && additionalAction.check(anchor)
            ) {
                this.additionBtn.visibility = View.VISIBLE
                this.additionBtn.setOnClickListener {
                    MainExecutor.execute {
                        additionalAction.doAdditionalAction(anchor, context)
                    }
                }
            } else {
                this.additionBtn.visibility = View.GONE
            }
        }
    }


    override fun getItemCount(): Int = anchorList.size


    override fun getLongClickPosition(): Int = mPosition
    override fun notifyAnchorsChange() = notifyDataSetChanged()
    override fun setScrolling(boolean: Boolean) {

    }
}
