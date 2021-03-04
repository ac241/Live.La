/*
 * Copyright (c) 2020.
 * @author acel
 * 主页
 */

package com.acel.streamlivetool.ui.main.group

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.showListOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.const_value.ConstValue
import com.acel.streamlivetool.databinding.FragmentGroupModeBinding
import com.acel.streamlivetool.ui.main.HandleContextItemSelect
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.main.adapter.AnchorAdapter
import com.acel.streamlivetool.ui.main.adapter.AnchorGroupingListener
import com.acel.streamlivetool.ui.main.adapter.MODE_GROUP
import com.acel.streamlivetool.util.PreferenceConstant
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupFragment : Fragment() {

    val viewModel by viewModels<GroupViewModel>()
    private lateinit var nowAnchorAdapter: AnchorAdapter
    private val adapterShowImage by lazy {
        AnchorAdapter(
            requireContext(),
            viewModel.sortedAnchorList.value!!,
            MODE_GROUP, true
        )
    }
    private val adapterNoImage by lazy {
        AnchorAdapter(
            requireContext(),
            viewModel.sortedAnchorList.value!!,
            MODE_GROUP, false
        )
    }

    private lateinit var _binding: FragmentGroupModeBinding
    private val binding
        get() = _binding

    private var updateProcessAnimate: ViewPropertyAnimator? = null
    var processViewAlpha: Float = 0.5f
    private val snackBar by lazy { Snackbar.make(requireActivity().main_container, "", 5000) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        lifecycle.addObserver(GroupLifecycle(this))
    }

    /**
     * 反射设置snackbar文字可点击
     */
    private fun Snackbar.setSpanClickable() {
        val snackbarContentLayout =
            ((view as Snackbar.SnackbarLayout).getChildAt(0) as SnackbarContentLayout)
        try {
            val controller = snackbarContentLayout::class.java.getDeclaredField("messageView")
            controller.isAccessible = true
            val messageView = controller.get(snackbarContentLayout) as TextView
            messageView.movementMethod = LinkMovementMethod.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        binding.groupSwipeRefresh.setOnRefreshListener {
            viewModel.sortedAnchorList.value?.let {
                if (it.isNotEmpty())
                    viewModel.updateAllAnchor()
            }
        }
        processViewAlpha = binding.includeProcessToast.textViewUpdateAnchorsDetails.alpha

        val drawable =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                resources.getDrawable(R.drawable.ic_home_page, null)
            } else {
                resources.getDrawable(R.drawable.ic_home_page)
            }
        drawable?.setBounds(0, 0, 40, 40)
        binding.include.groupTitleWrapper.findViewById<TextView>(R.id.status_living)?.apply {
            setCompoundDrawables(null, null, drawable, null)
        }
        /**
         * observe liveData
         */
        viewModel.apply {
            sortedAnchorList.observe(viewLifecycleOwner, {
                refreshAnchorAttribute()
            })
            liveDataUpdateStatus.observe(viewLifecycleOwner, {
                if (it == null)
                    return@observe
                when (it) {
                    GroupViewModel.UpdateStatus.PREPARE, GroupViewModel.UpdateStatus.FINISH ->
                        updateFinish()
                    GroupViewModel.UpdateStatus.UPDATING ->
                        updating()
                }
            })
            updateErrorMsg.observe(viewLifecycleOwner, {
                it?.let {
                    if (it.isNotEmpty()) {
                        snackBar.setText(it)
                        snackBar.setSpanClickable()
                        snackBar.show()
                    }
                }
            })
            updateSuccess.observe(viewLifecycleOwner, {
                snackBar.dismiss()
//                completeUpdateDetails("主页 更新成功。")
            })
            showCheckedFollowDialog.observe(viewLifecycleOwner, {
                it?.let {
                    viewModel.showFollowDialog(requireActivity(), it)
                }
            })
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        iniRecyclerViewLayoutManager(newConfig.orientation)
    }

    private fun iniRecyclerViewLayoutManager(orientation: Int) {
        binding.include.recyclerView.layoutManager =
            StaggeredGridLayoutManager(
                if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3,
                StaggeredGridLayoutManager.VERTICAL
            )
    }

    private fun initRecyclerView() {
        iniRecyclerViewLayoutManager(resources.configuration.orientation)
        nowAnchorAdapter = if (PreferenceConstant.showAnchorImage)
            adapterShowImage
        else
            adapterNoImage
        setAdapter()
        binding.include.recyclerView.addOnScrollListener(AnchorGroupingListener())
    }

    private fun setAdapter() {
        binding.include.recyclerView.adapter = nowAnchorAdapter
    }

    fun setShowImage(boolean: Boolean) {
        if (boolean) {
            if (!isShowImage()) {
                nowAnchorAdapter = adapterShowImage
                setAdapter()
            }
        } else {
            if (isShowImage()) {
                nowAnchorAdapter = adapterNoImage
                setAdapter()
            }
        }
    }

    private fun isShowImage(): Boolean =
        nowAnchorAdapter == adapterShowImage


    @Synchronized
    fun refreshAnchorAttribute() {
        nowAnchorAdapter.notifyAnchorsChange()
//        updateFinish()
    }

    private var updatingTime: Long = 0L

    private fun updating() {
        synchronized(updatingTime) {
            updatingTime = System.currentTimeMillis()
            binding.groupSwipeRefresh.isRefreshing = true
        }
    }

    private fun updateFinish() {
        synchronized(updatingTime) {
            //如果更新数据时间小于两秒，一定时间后再隐藏。
            if (System.currentTimeMillis() - updatingTime > 2000) {
                binding.groupSwipeRefresh.isRefreshing = false
            } else {
                lifecycleScope.launch(Dispatchers.Default) {
                    delay(500)
                    withContext(Dispatchers.Main) {
                        binding.groupSwipeRefresh.isRefreshing = false
                    }
                }
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (isResumed)
            when (item.itemId) {
                R.id.action_item_delete -> {
                    val position = nowAnchorAdapter.getLongClickPosition()
                    viewModel.deleteAnchor(viewModel.sortedAnchorList.value!![position])
                }
                ConstValue.ITEM_ID_FOLLOW_ANCHOR -> {
                    val position = nowAnchorAdapter.getLongClickPosition()
                    val anchor = viewModel.sortedAnchorList.value!![position]
                    viewModel.followAnchor(requireContext(),anchor) {}
                }
                else -> {
                    val position = nowAnchorAdapter.getLongClickPosition()
                    val anchor = viewModel.sortedAnchorList.value!![position]
                    HandleContextItemSelect.handle(
                        requireContext(),
                        item.itemId,
                        anchor,
                        viewModel.sortedAnchorList.value!!
                    )
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

    @Suppress("DEPRECATION")
    private fun showUpdateDetails(text: String) {
        updateProcessAnimate?.cancel()
        binding.includeProcessToast.textViewUpdateAnchorsDetails.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                this.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            else
                this.text = Html.fromHtml(text)
            visibility = View.VISIBLE
        }
    }

    @Suppress("SameParameterValue")
    private fun completeUpdateDetails(text: String) {
        showUpdateDetails(text)
        binding.includeProcessToast.textViewUpdateAnchorsDetails.apply {
            updateProcessAnimate = animate().alpha(0f).setDuration(1500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(p0: Animator?) {
                        visibility = View.GONE
                        alpha = processViewAlpha
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                        alpha = processViewAlpha
                    }

                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) {}
                }).setStartDelay(1500)
        }
    }

    fun scrollToTop() {
        binding.include.recyclerView.smoothScrollToPosition(0)
    }

    fun checkFollowed(anchor: Anchor) {
        viewModel.addToCheckedFollowed(anchor)
    }

    companion object {
        @JvmStatic
        fun newInstance() = GroupFragment()
    }

}


