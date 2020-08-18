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
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorPlaceHolder
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.platform.anchor_additional.AdditionalAction
import com.acel.streamlivetool.util.ActionClick.itemClick
import com.acel.streamlivetool.util.ActionClick.secondBtnClick
import com.acel.streamlivetool.util.AnchorListUtil.getLivingAnchors
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.ToastUtil
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.item_text_anchor.view.*


class TextAnchorAdapter(
    val context: Context,
    val anchorList: List<Anchor>,
    private val modeType: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AnchorAdapterWrapper {


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
            GraphicAnchorAdapter.VIEW_TYPE_LIVING_TITLE ->
                holder = ViewHolderStatusGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_status_title_living, parent, false)
                        .also {
                            it.tag =
                                AnchorListAddTitleListener.STATUS_GROUP_TITLE_LIVING
                        }
                )
            GraphicAnchorAdapter.VIEW_TYPE_NOT_LIVING_TITLE ->
                holder = ViewHolderStatusGroup(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_status_title_not_living, parent, false)
                        .also {
                            it.tag =
                                AnchorListAddTitleListener.STATUS_GROUP_TITLE_NOT_LIVING
                        }
                )
            else ->
                holder = ViewHolderText(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_text_anchor, parent, false)
                    , modeType
                )
        }

        return holder
    }

    override fun getItemViewType(position: Int): Int {
        return when (anchorList[position]) {
            AnchorPlaceHolder.anchorIsLiving ->
                GraphicAnchorAdapter.VIEW_TYPE_LIVING_TITLE
            AnchorPlaceHolder.anchorNotLiving ->
                GraphicAnchorAdapter.VIEW_TYPE_NOT_LIVING_TITLE
            else ->
                GraphicAnchorAdapter.VIEW_TYPE_NORMAL
        }
    }


    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderStatusGroup)
            return

        holder as ViewHolderText
        val anchor: Anchor = anchorList[position]
        holder.itemView.tag =
            if (anchor.status) AnchorListAddTitleListener.STATUS_LIVING else AnchorListAddTitleListener.STATUS_NOT_LIVING

        with(holder) {
            //title
            holder.title.text =
                anchor.title ?: "-"
            //直播状态
            if (!anchorList.contains(AnchorPlaceHolder.anchorIsLiving)
                || !anchorList.contains(AnchorPlaceHolder.anchorNotLiving)
            ) {
                holder.status.visibility = View.VISIBLE
                if (anchor.status) {
                    holder.status.text = context.getString(R.string.is_living)
                    holder.status.setTextColor(Color.GREEN)
                } else {
                    holder.status.text = context.getString(R.string.not_living)
                    holder.status.setTextColor(Color.GRAY)
                }
                holder.title.text =
                    anchor.title
            } else
                holder.status.visibility = View.GONE
            //二级状态
            anchor.secondaryStatus.apply {
                if (this != null && isNotEmpty()) {
                    holder.secondaryStatus.visibility = View.VISIBLE
                    holder.secondaryStatus.text = this
                } else {
                    holder.secondaryStatus.visibility = View.GONE
                }
            }

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
                itemClick(context, anchor, anchorList)
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
                    ToastUtil.toast(actionName)
                    return@setOnLongClickListener true
                }
            } else {
                holder.additionBtn.visibility = View.GONE
            }
        }
    }


    override fun getItemCount(): Int = anchorList.size
    override fun getLongClickPosition(): Int = mPosition
    override fun notifyAnchorsChange() = notifyDataSetChanged()

    class ViewHolderText(itemView: View, private val modeType: Int) :
        RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.setHeaderTitle("${anchorName.text}(${roomId.text})")
            when (modeType) {
                MODE_GROUP ->
                    (itemView.context as AppCompatActivity).menuInflater.inflate(
                        R.menu.anchor_item_menu,
                        menu
                    )
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

        val anchorName: TextView = itemView.anchor_name
        val platform: TextView = itemView.main_anchor_platform
        val roomId: TextView = itemView.main_anchor_roomId
        val status: TextView = itemView.main_anchor_status
        val secondaryStatus: TextView = itemView.main_anchor_secondary_status
        val secondBtn: ImageView = itemView.main_second_btn
        val title: TextView = itemView.anchor_title
        val additionBtn: ImageView = itemView.btn_addition_action
    }

}
