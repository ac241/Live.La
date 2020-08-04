package com.acel.streamlivetool.ui.main.group

import android.os.Bundle
import android.util.Log
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
import com.acel.streamlivetool.databinding.FragmentGroupModeBinding
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.main.MainActivity.Companion.ListItemType
import com.acel.streamlivetool.ui.main.adapter.*
import com.acel.streamlivetool.ui.main.showListOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.util.defaultSharedPreferences

class GroupFragment : Fragment() {
    internal var listItemType = when (defaultSharedPreferences.getString(
        MyApplication.application.resources.getString(R.string.pref_key_group_mode_list_type),
        MyApplication.application.resources.getString(R.string.string_grid_view)
    )) {
        MyApplication.application.resources.getString(R.string.string_recycler_view) -> ListItemType.Text
        MyApplication.application.resources.getString(R.string.string_grid_view) -> ListItemType.Graphic
        else -> ListItemType.Text
    }
    val viewModel by viewModels<GroupViewModel> { GroupViewModel.ViewModeFactory(this) }
    internal lateinit var nowAnchorAnchorAdapter: AnchorAdapterWrapper
    private val graphicAnchorAdapter by lazy {
        GraphicAnchorAdapter(
            requireContext(),
            viewModel.sortedAnchorList.value!!,
            MODE_GROUP
        )
    }
    private val textAnchorAdapter by lazy {
        TextAnchorAdapter(
            requireContext(),
            viewModel.sortedAnchorList.value!!,
            MODE_GROUP
        )
    }
    private var _binding: FragmentGroupModeBinding? = null
    private val binding
        get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("onCreateView", "onCreateView")
        _binding = FragmentGroupModeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        lifecycle.addObserver(GroupLifecycle(this))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        binding?.groupSwipeRefresh?.setOnRefreshListener {
            viewModel.sortedAnchorList.value?.let {
                if (it.isNotEmpty())
                    viewModel.getAllAnchorsAttribute()
                else
                    hideSwipeRefreshBtn()
            }
        }
    }

    private fun initRecyclerView() {
        when (listItemType) {
            ListItemType.Text -> {
                setTextAdapter()
            }

            ListItemType.Graphic -> {
                setGraphicAdapter()
            }
        }
        binding?.include?.recyclerView?.addOnScrollListener(AnchorListAddTitleListener())
        //关闭刷新item时CardView的闪烁提示
        binding?.include?.recyclerView?.itemAnimator?.changeDuration = 0
    }

    fun setGraphicAdapter() {
        val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding?.include?.recyclerView?.layoutManager = manager
        binding?.include?.recyclerView?.adapter = graphicAnchorAdapter
        nowAnchorAnchorAdapter = graphicAnchorAdapter
    }

    fun setTextAdapter() {
        binding?.include?.recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding?.include?.recyclerView?.adapter = textAnchorAdapter
        nowAnchorAnchorAdapter = textAnchorAdapter
    }

    @Synchronized
    fun refreshAnchorAttribute() {
        nowAnchorAnchorAdapter.notifyAnchorsChange()
        hideSwipeRefreshBtn()
    }

    @Synchronized
    internal fun hideSwipeRefreshBtn() {
        binding?.groupSwipeRefresh?.isRefreshing = false
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
                if (isVisible)
                    (requireActivity() as MainActivity).showListOverlayWindowWithPermissionCheck(
                        viewModel.sortedAnchorList.value!!
                    )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmStatic
        fun newInstance() = GroupFragment()
    }

}