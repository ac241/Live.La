package com.acel.streamlivetool.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.ui.adapter.*
import com.acel.streamlivetool.ui.main.MainActivity.Companion.ListItemType
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.activity_group_mode.*
import kotlinx.android.synthetic.main.fab_layout.*
import kotlinx.android.synthetic.main.layout_anchor_recycler_view.*

class GroupFragment : Fragment() {
    private var listItemType = when (defaultSharedPreferences.getString(
        MyApplication.application.resources.getString(R.string.pref_key_group_mode_list_type),
        MyApplication.application.resources.getString(R.string.string_grid_view)
    )) {
        MyApplication.application.resources.getString(R.string.string_recycler_view) -> ListItemType.Text
        MyApplication.application.resources.getString(R.string.string_grid_view) -> ListItemType.Graphic
        else -> ListItemType.Text
    }
    private val viewModel by viewModels<GroupViewModel> { GroupViewModel.ViewModeFactory(this) }
    private lateinit var nowAnchorAnchorAdapter: AnchorAdapterWrapper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_group_mode, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        main_swipe_refresh.setOnRefreshListener {
            viewModel.sortedAnchorList.value?.let {
                if (it.isNotEmpty())
                    viewModel.getAllAnchorsAttribute()
                else
                    hideSwipeRefreshBtn()
            }
        }
        btn_fab.setOnClickListener {
            fabClick()
        }
    }

    private fun initRecyclerView() {
        when (listItemType) {
            ListItemType.Text -> {
                recycler_view.layoutManager = LinearLayoutManager(requireContext())
                val adapter = TextAnchorAdapter(
                    requireContext(),
                    viewModel.sortedAnchorList.value!!,
                    MODE_GROUP
                )
                recycler_view.adapter = adapter
                nowAnchorAnchorAdapter = adapter
            }

            ListItemType.Graphic -> {
                val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                recycler_view.layoutManager = manager
                val adapter = GraphicAnchorAdapter(
                    requireContext(),
                    viewModel.sortedAnchorList.value!!,
                    MODE_GROUP
                )
                recycler_view.adapter = adapter
                nowAnchorAnchorAdapter = adapter
//                recycler_view.addItemDecoration(GroupTitleDecoration())
            }
        }
        recycler_view.addOnScrollListener(AnchorListAddTitleListener())

        //关闭刷新item时CardView的闪烁提示
        recycler_view.itemAnimator?.changeDuration = 0
    }


    @Synchronized
    fun refreshAnchorAttribute() {
        nowAnchorAnchorAdapter.notifyAnchorsChange()
        hideSwipeRefreshBtn()
    }


    private fun hideSwipeRefreshBtn() {
        main_swipe_refresh.isRefreshing = false
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_item_delete -> {
                val position = nowAnchorAnchorAdapter.getLongClickPosition()
                viewModel.deleteAnchor(viewModel.sortedAnchorList.value!![position])
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_list_overlay -> {
                (requireActivity() as MainActivity).showListOverlayWindowWithPermissionCheck(
                    viewModel.sortedAnchorList.value!!
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fabClick() {
        (requireActivity() as MainActivity).toggleFragment()
    }

    companion object {
        @JvmStatic
        fun newInstance() = GroupFragment()
    }
}