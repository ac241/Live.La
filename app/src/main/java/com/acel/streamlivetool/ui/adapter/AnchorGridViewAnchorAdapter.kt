package com.acel.streamlivetool.ui.adapter

import android.content.Context
import android.graphics.Color
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
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.bean.AnchorsCookieMode
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.platform.anchor_additional.AdditionalAction
import com.acel.streamlivetool.ui.ActionClick.itemClick
import com.acel.streamlivetool.ui.ActionClick.secondBtnClick
import com.acel.streamlivetool.ui.cookie_mode.CookieModeActivity
import com.acel.streamlivetool.ui.group_mode.GroupModeActivity
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.item_grid_anchor.view.*


class AnchorGridViewAnchorAdapter() : BaseAdapter(),
    AnchorAdapterWrapper {
    private lateinit var context: Context
    private lateinit var anchorList: List<Anchor>
    private var isScrolling = false
    override fun setScrolling(boolean: Boolean) {
        isScrolling = boolean
    }

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


    private var anchorAttributeMap: MutableLiveData<MutableMap<String, AnchorAttribute>>? = null

    private val platformNameMap: MutableMap<String, String> = mutableMapOf()
    private var mPosition: Int = -1
    private val fullVersion by lazy {
        defaultSharedPreferences.getBoolean(
            context.getString(R.string.full_version),
            false
        )
    }

    private val additionalAction = AdditionalAction.instance

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

        val anchorName: TextView = itemView.grid_anchor_name
        val platform: TextView = itemView.grid_anchor_platform
        val image: ImageView = itemView.grid_anchor_image
        val avatar: ImageView = itemView.grid_anchor_avatar

        //        val roomId: TextView = itemView.main_anchor_roomId
        val status: TextView = itemView.grid_anchor_status
        val secondBtn: ImageView = itemView.grid_anchor_second_btn
        val title: TextView = itemView.grid_anchor_title
        val additionBtn: ImageView = itemView.grid_anchor_addition_action
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val viewHolder: ViewHolder
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_grid_anchor, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else
            viewHolder = convertView?.tag as ViewHolder

        convertView?.tag = viewHolder
        val anchor: Anchor = anchorList[position]

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

        //设置状态，图片，头像，点击事件
        when (modeType) {
            ModeType.GroupMode ->
                setValueGroupMode(anchor, viewHolder)
            ModeType.CookieMode ->
                setValueCookieMode(anchor, viewHolder)
        }

        //直播间Id
//        viewHolder.roomId.text = anchor.showId

        //item click
        viewHolder.itemView.setOnClickListener {
            itemClick(context, anchor)
        }

        //长按
        viewHolder.itemView.setOnLongClickListener {
            mPosition = position
            return@setOnLongClickListener false
        }

        //副键点击
        if (fullVersion) {
            viewHolder.secondBtn.visibility = View.VISIBLE
            viewHolder.secondBtn.setOnClickListener {
                secondBtnClick(context, anchor)
            }
        }

        //附加功能按钮
        if (defaultSharedPreferences.getBoolean(
                context.getString(R.string.pref_key_additional_action_btn),
                false
            ) && additionalAction.check(anchor)
        ) {
            viewHolder.additionBtn.visibility = View.VISIBLE
            viewHolder.additionBtn.setOnClickListener {
                MainExecutor.execute {
                    additionalAction.doAdditionalAction(anchor, context)
                }
            }
        } else {
            viewHolder.additionBtn.visibility = View.GONE
        }

        return view!!
    }

    private fun setValueCookieMode(anchor: Anchor, viewHolder: ViewHolder) {
        //直播状态
        anchor as AnchorsCookieMode.Anchor
        if (anchor.status) {
            viewHolder.status.text = "直播中"
            viewHolder.status.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            viewHolder.status.text = "未直播"
            viewHolder.status.setTextColor(Color.WHITE)
        }
        //title
        viewHolder.title.text = anchor.title
        //头像
        with(anchor.avatar) {
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
        with(anchor.keyFrame) {
            if (this != null) {
                ImageLoader.load(
                    context,
                    this,
                    viewHolder.image
                )
            } else
                viewHolder.image.setImageResource(R.drawable.ic_load_img_fail)
        }

    }

    private fun setValueGroupMode(anchor: Anchor, viewHolder: ViewHolder) {
        with(anchorAttributeMap?.value?.get(anchor.anchorKey())) {
            viewHolder.title.text = this?.title ?: "-"
            //头像
            with(this?.avatar) {
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
            with(this?.image) {
                if (this != null) {
                    ImageLoader.load(
                        context,
                        this,
                        viewHolder.image
                    )
                } else
                    viewHolder.image.setImageResource(R.drawable.ic_load_img_fail)
            }
            //直播状态
            if (this?.isLive != null) {
                if (this.isLive) {
                    viewHolder.status.text = "直播中"
                    viewHolder.status.setTextColor(Color.parseColor("#4CAF50"))
                } else {
                    viewHolder.status.text = "未直播"
                    viewHolder.status.setTextColor(Color.WHITE)
                }
            } else {
                viewHolder.status.text = ""
            }
        }
    }

    override fun getItem(position: Int): Anchor? =
        anchorList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = anchorList.size
    override fun getLongClickPosition(): Int = mPosition
    override fun notifyAnchorsChange() = notifyDataSetChanged()
}
