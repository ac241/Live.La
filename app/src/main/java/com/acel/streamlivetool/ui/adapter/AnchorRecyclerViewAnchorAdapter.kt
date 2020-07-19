package com.acel.streamlivetool.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.MainExecutor
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.bean.AnchorsCookieMode
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.platform.anchor_additional.AdditionalAction
import com.acel.streamlivetool.ui.ActionClick.itemClick
import com.acel.streamlivetool.ui.ActionClick.secondBtnClick
import com.acel.streamlivetool.ui.cookie_mode.CookieModeActivity
import com.acel.streamlivetool.ui.group_mode.GroupModeActivity
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.item_recycler_anchor.view.*


class AnchorRecyclerViewAnchorAdapter() :
    RecyclerView.Adapter<AnchorRecyclerViewAnchorAdapter.ViewHolder>(),
    AnchorAdapterWrapper {

    constructor(context: Context, anchorList: List<Anchor>) : this() {
        if (context !is CookieModeActivity)
            throw Exception("only cookie mode")
        this.context = context
        this.anchorList = anchorList
    }

    constructor(
        context: Context, anchorList: List<Anchor>,
        anchorAttributeMap: MutableLiveData<MutableMap<String, AnchorAttribute>>
    ) : this() {
        if (context !is GroupModeActivity)
            throw Exception("only group mode")
        this.context = context
        this.anchorList = anchorList
        this.anchorAttributeMap = anchorAttributeMap
    }

    private lateinit var context: Context
    private lateinit var anchorList: List<Anchor>
    private var anchorAttributeMap: MutableLiveData<MutableMap<String, AnchorAttribute>>? = null

    enum class ModeType { CookieMode, GroupMode }

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


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_recycler_anchor, p0, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
        val anchor: Anchor = anchorList[p1]
        with(holder) {
            //设置状态，图片，头像，点击事件
            when (modeType) {
                ModeType.GroupMode ->
                    setValueGroupMode(anchor, this)
                ModeType.CookieMode ->
                    setValueCookieMode(anchor, this)
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

    private fun setValueGroupMode(anchor: Anchor, viewHolder: ViewHolder) {
        with(anchorAttributeMap?.value?.get(anchor.anchorKey())) {
            viewHolder.title.text =
                this?.title ?: "-"
            //直播状态
            if (this?.isLive != null) {
                if (this.isLive) {
                    viewHolder.status.text = "直播中"
                    viewHolder.status.setTextColor(Color.GREEN)
                } else {
                    viewHolder.status.text = "未直播"
                    viewHolder.status.setTextColor(Color.GRAY)
                }
            } else {
                viewHolder.status.text = ""
            }
        }
    }

    private fun setValueCookieMode(anchor: Anchor, viewHolder: ViewHolder) {
        //直播状态
        anchor as AnchorsCookieMode.Anchor
        if (anchor.status) {
            viewHolder.status.text = "直播中"
            viewHolder.status.setTextColor(Color.GREEN)
        } else {
            viewHolder.status.text = "未直播"
            viewHolder.status.setTextColor(Color.GRAY)
        }
        viewHolder.title.text =
            anchor.title
    }

    override fun getItemCount(): Int = anchorList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            when (modeType) {
                ModeType.GroupMode ->
                    (context as AppCompatActivity).menuInflater.inflate(
                        R.menu.anchor_item_menu,
                        menu
                    )
                ModeType.CookieMode ->
                    (context as AppCompatActivity).menuInflater.inflate(
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
        val secondBtn: ImageView = itemView.main_second_btn
        val title: TextView = itemView.anchor_title
        val additionBtn: ImageView = itemView.btn_addition_action
    }

    override fun getLongClickPosition(): Int = mPosition
    override fun notifyAnchorsChange() = notifyDataSetChanged()
}
