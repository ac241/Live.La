package com.acel.streamlivetool.ui.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.anchor_extension.AnchorExtensionManager
import com.acel.streamlivetool.anchor_extension.action.AnchorExtensionInterface
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.value.ConstValue.FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR
import com.acel.streamlivetool.value.ConstValue.ITEM_ID_FOLLOW_ANCHOR
import com.acel.streamlivetool.value.PreferenceVariable.showAdditionalActionButton
import com.acel.streamlivetool.net.ImageLoader
import com.acel.streamlivetool.platform.PlatformDispatcher.getIconDrawable
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.util.AnchorClickAction.secondBtnClick
import com.acel.streamlivetool.util.MainExecutor
import kotlinx.android.synthetic.main.item_anchor.view.*
import kotlinx.android.synthetic.main.item_section_living.view.*
import kotlinx.android.synthetic.main.text_view_graphic_secondary_status.view.*
import kotlinx.android.synthetic.main.text_view_type_name.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AnchorAdapter(
        private val context: Context,
        private val anchorList: List<Anchor>,
        private val modeType: Int,
        var showImage: Boolean,
        private val iconDrawable: Drawable
) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private var mPosition: Int = -1
    private val additionalActionManager = AnchorExtensionManager.instance
    private val onlineImageSpan = ImageSpan(context, R.drawable.ic_online_hot)

    private var livingSectionPosition = -1
    private var notLivingSectionPosition = -1

    private val filterList = mutableListOf<Anchor>()
    private var filterKeyword = ""
    private var useFilter = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ANCHOR ->
                ViewHolderGraphic(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_anchor, parent, false)
                )
            VIEW_TYPE_SECTION_LIVING ->
                ViewHolderSection(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_section_living, parent, false)
                )
            VIEW_TYPE_SECTION_NOT_LIVING ->
                ViewHolderSection(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_section_not_living, parent, false)
                                .apply {
                                    iconDrawable.setBounds(0, 0, 40, 40)
                                    findViewById<TextView>(R.id.section_title)?.apply {
                                        setCompoundDrawables(null, null, iconDrawable, null)
                                    }
                                }
                )
            VIEW_TYPE_ANCHOR_SIMPLIFY ->
                ViewHolderGraphic(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_anchor_simplify, parent, false)
                )
            else ->
                ViewHolderGraphic(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_anchor, parent, false)
                )
        }
    }

    override fun getItemCount(): Int {
        return displayList().size
    }

    override fun getItemViewType(position: Int): Int {
        try {
            return when (displayList()[position]) {
                AnchorSection.ANCHOR_SECTION_LIVING ->
                    VIEW_TYPE_SECTION_LIVING
                AnchorSection.ANCHOR_SECTION_NOT_LIVING ->
                    VIEW_TYPE_SECTION_NOT_LIVING
                else -> {
                    if (displayList()[position].status && showImage)
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

    private val spannableStringBuilder = SpannableStringBuilder()
    private val drawableLoadFail =
            ResourcesCompat.getDrawable(context.resources, R.drawable.ic_load_img_fail, null)

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolderSection) {
            holder.filterEdit.apply {
                if (position == 0) {
                    visibility = View.VISIBLE
                    if (getItemViewType(position) == VIEW_TYPE_SECTION_NOT_LIVING)
                        holder.sectionTitle.setCompoundDrawables(null, null, null, null)
                } else {
                    visibility = View.GONE
                    if (getItemViewType(position) == VIEW_TYPE_SECTION_NOT_LIVING)
                        holder.sectionTitle.setCompoundDrawables(null, null, iconDrawable, null)
                }
                setText(filterKeyword)
                if (justEditFilterKeyword) {
                    justEditFilterKeyword = false
                    requestFocus()
                }
            }
            return
        }

        val anchor: Anchor = displayList()[position]
        holder as ViewHolderGraphic

        //主播名
        holder.anchorName.text = anchor.nickname
        //平台名
        holder.platform.visibility = View.GONE

        //平台图标
        if (modeType == MODE_GROUP) {
            holder.icon?.setImageDrawable(anchor.getIconDrawable())
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
                holder.avatar.setImageDrawable(drawableLoadFail)
            }
        }

        //如果是显图类型
        if (getItemViewType(position) == VIEW_TYPE_ANCHOR) {
            //图片
            with(anchor.keyFrame) {
                if (this != null) {
                    ImageLoader.load(context, this, holder.image)
                } else
                    holder.image.setImageDrawable(drawableLoadFail)
            }
            //热度
            anchor.online?.apply {
                if (isNotEmpty()) {
                    spannableStringBuilder.clear()
                    //加一个空格等待替换
                    spannableStringBuilder.append(" $this")
                    spannableStringBuilder.setSpan(
                            onlineImageSpan,
                            0,
                            1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    holder.online?.text = spannableStringBuilder
                }
            }
        }

        //二级状态
        with(anchor.secondaryStatus) {
            if (this != null && isNotEmpty()) {
                holder.secondaryStatus.visibility = View.VISIBLE
                holder.secondaryStatus.text = this
            } else {
                holder.secondaryStatus.visibility = View.GONE
            }
        }

        //附加功能按钮
        if (showAdditionalActionButton.value!! && anchor.anchorExtensions != null) {
            anchor.anchorExtensions?.let {
                holder.additionBtn.apply {
                    visibility = View.VISIBLE
                    if (it.size == 1)
                        setImageDrawable(it[0].iconDrawable)
                    else
                        setImageDrawable(AnchorExtensionInterface.iconDrawableDefault)
                }
            }
        } else {
            holder.additionBtn.visibility = View.GONE
        }
    }

    fun displayList(): List<Anchor> {
        return if (useFilter) filterList else anchorList
    }

    fun getLongClickPosition(): Int = mPosition

    fun notifyAnchorsChange(resetFilter: Boolean = true) {
        if (resetFilter) {
            resetFilter()
        }
        livingSectionPosition = displayList().indexOf(AnchorSection.ANCHOR_SECTION_LIVING)
        notLivingSectionPosition = displayList().indexOf(AnchorSection.ANCHOR_SECTION_NOT_LIVING)
        notifyDataSetChanged()
    }

    private fun resetFilter() {
        useFilter = false
        filterKeyword = ""
    }

    fun getNotLivingSectionPosition(): Int = notLivingSectionPosition

    @Suppress("unused")
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
            View.OnCreateContextMenuListener, View.OnClickListener {
        override fun onCreateContextMenu(
                menu: ContextMenu?,
                v: View?,
                menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            mPosition = absoluteAdapterPosition
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
                            val anchor = displayList()[position]
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

        val anchorName: TextView = itemView.grid_anchor_name
        val platform: TextView = itemView.grid_anchor_platform
        val image: ImageView = itemView.grid_anchor_image
        val avatar: ImageView = itemView.grid_anchor_avatar

        //        val status: TextView = itemView.grid_anchor_status
        private val secondBtn: ImageView = itemView.grid_anchor_second_btn
        val title: TextView = itemView.grid_anchor_title
        val additionBtn: ImageView = itemView.grid_anchor_addition_action
        val secondaryStatus: TextView = itemView.secondary_status
        val roomId: TextView = itemView.grid_anchor_roomId
        val typeName: TextView = itemView.type_name
        val online: TextView? = itemView.grid_anchor_online ?: null
        val liveTime: TextView = itemView.grid_anchor_live_time
        val icon: ImageView? = itemView.platform_icon

        init {
            itemView.apply {
                setOnCreateContextMenuListener(this@ViewHolderGraphic)
                setOnClickListener(this@ViewHolderGraphic)
            }

            secondBtn.setOnClickListener {
                val position = absoluteAdapterPosition
                secondBtnClick(context, displayList()[position], displayList())
            }
            additionBtn.setOnClickListener {
                val position = absoluteAdapterPosition
                MainExecutor.execute {
                    additionalActionManager.doActions(displayList()[position], context)
                }
            }
        }

        override fun onClick(v: View?) {
            val position = absoluteAdapterPosition
            context as MainActivity
            context.itemClick(itemView, context, displayList()[position], displayList())
        }
    }

    /**
     * 分组的ViewHolder
     */
    private var justEditFilterKeyword = false

    inner class ViewHolderSection(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sectionTitle: TextView = itemView.section_title
        val filterEdit: EditText = itemView.filter_keyword

        init {
            filterEdit.addTextChangedListener {
                if (filterEdit.hasFocus()) {
                    filterKeyword = it.toString()
                    justEditFilterKeyword = true
                    startFilterJob()
                }
            }
        }
    }

    private var filterJob: Job? = null
    private fun startFilterJob() {
        filterJob?.cancel()
        filterJob = GlobalScope.launch {
            delay(500)
            this@AnchorAdapter.filter.filter(filterKeyword)
        }
    }


    inner class AnchorFilter : Filter() {
        private val tempResult = FilterResults()
        override fun performFiltering(constraint: CharSequence): FilterResults {
            filterList.clear()
            anchorList.forEach {
                if (it == AnchorSection.ANCHOR_SECTION_LIVING ||
                        it == AnchorSection.ANCHOR_SECTION_NOT_LIVING ||
                        it.nickname.contains(constraint, true)
                ) {
                    filterList.add(it)
                }
            }
            return tempResult
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            useFilter = constraint.isNotEmpty()
            notifyAnchorsChange(false)
        }
    }

    private val anchorFilter = AnchorFilter()
    override fun getFilter(): Filter = anchorFilter
}