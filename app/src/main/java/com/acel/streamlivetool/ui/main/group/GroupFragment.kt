package com.acel.streamlivetool.ui.main.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.databinding.FragmentGroupModeBinding
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.main.adapter.AnchorAdapterWrapper
import com.acel.streamlivetool.ui.main.adapter.AnchorListAddTitleListener
import com.acel.streamlivetool.ui.main.adapter.GraphicAnchorAdapter
import com.acel.streamlivetool.ui.main.adapter.MODE_GROUP
import com.acel.streamlivetool.ui.main.showListOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.util.PreferenceConstant

class GroupFragment : Fragment() {

    val viewModel by viewModels<GroupViewModel> { GroupViewModel.ViewModeFactory(this) }
    private lateinit var nowAnchorAdapter: AnchorAdapterWrapper
    private val adapterShowAnchorImage by lazy {
        GraphicAnchorAdapter(
            requireContext(),
            viewModel.sortedAnchorList.value!!,
            MODE_GROUP, true
        )
    }
    private val adapterNotShowAnchorImage by lazy {
        GraphicAnchorAdapter(
            requireContext(),
            viewModel.sortedAnchorList.value!!,
            MODE_GROUP, false
        )
    }

    private var _binding: FragmentGroupModeBinding? = null
    private val binding
        get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                    viewModel.updateAllAnchor()
                else
                    hideSwipeRefreshBtn()
            }
        }
    }

    private fun initRecyclerView() {
        binding?.include?.recyclerView?.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        nowAnchorAdapter = if (PreferenceConstant.showAnchorImage)
            adapterShowAnchorImage
        else
            adapterNotShowAnchorImage
        setGraphicAdapter()
        binding?.include?.recyclerView?.addOnScrollListener(AnchorListAddTitleListener())
    }

    private fun setGraphicAdapter() {
        binding?.include?.recyclerView?.adapter = nowAnchorAdapter as GraphicAnchorAdapter
    }

    fun setShowImage(boolean: Boolean) {
        nowAnchorAdapter = if (boolean) adapterShowAnchorImage else adapterNotShowAnchorImage
        setGraphicAdapter()
    }

    fun isShowImage(): Boolean {
        return nowAnchorAdapter == adapterShowAnchorImage
    }

    @Synchronized
    fun refreshAnchorAttribute() {
        nowAnchorAdapter.notifyAnchorsChange()
        hideSwipeRefreshBtn()
    }

    @Synchronized
    internal fun hideSwipeRefreshBtn() {
        binding?.groupSwipeRefresh?.isRefreshing = false
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_item_delete -> {
                val position = nowAnchorAdapter.getLongClickPosition()
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