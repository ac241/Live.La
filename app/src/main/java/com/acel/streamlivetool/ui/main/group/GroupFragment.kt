/*
 * Copyright (c) 2020.
 * @author acel
 * 主页
 */

package com.acel.streamlivetool.ui.main.group

import android.animation.Animator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.const_value.ConstValue
import com.acel.streamlivetool.databinding.FragmentGroupModeBinding
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.main.adapter.AnchorAdapter
import com.acel.streamlivetool.ui.main.adapter.AnchorGroupingListener
import com.acel.streamlivetool.ui.main.adapter.MODE_GROUP
import com.acel.streamlivetool.ui.main.showListOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.util.PreferenceConstant
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import kotlinx.android.synthetic.main.activity_main.*

class GroupFragment : Fragment() {

    val viewModel by viewModels<GroupViewModel>()
    private lateinit var nowAnchorAdapter: AnchorAdapter
    private val adapterShowAnchorImage by lazy {
        AnchorAdapter(
            requireContext(),
            viewModel.sortedAnchorList.value!!,
            MODE_GROUP, true
        )
    }
    private val adapterNotShowAnchorImage by lazy {
        AnchorAdapter(
            requireContext(),
            viewModel.sortedAnchorList.value!!,
            MODE_GROUP, false
        )
    }

    private var _binding: FragmentGroupModeBinding? = null
    private val binding
        get() = _binding

    private var updateProcessAnimate: ViewPropertyAnimator? = null
    var processViewAlpha: Float = 0.5f

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
        /**
         * observe liveData
         */
        viewModel.apply {
            sortedAnchorList.observe(this@GroupFragment, Observer {
                refreshAnchorAttribute()
            })
            liveDataUpdateStatus.observe(this@GroupFragment, Observer {
                if (it == GroupViewModel.UpdateStATUS.PREPARE || it == GroupViewModel.UpdateStATUS.FINISH)
                    hideSwipeRefreshBtn()
            })
            updateErrorMsg.observe(this@GroupFragment, Observer {
                it?.let {
                    if (it.isNotEmpty()) {
                        val snackBar = Snackbar.make(requireActivity().main_container, it, 5000)
                        snackBar.setSpanClickable()
                        snackBar.show()
                    }
                }
            })
            updateSuccess.observe(this@GroupFragment, Observer {
//                val toast = Toast.makeText(requireContext(), "主页 更新成功。", Toast.LENGTH_SHORT)
//                toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 150)
//                toast.show()
                completeUpdateDetails("主页 更新成功。")
            })
        }
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
        binding?.groupSwipeRefresh?.setOnRefreshListener {
            viewModel.sortedAnchorList.value?.let {
                if (it.isNotEmpty())
                    viewModel.updateAllAnchor()
                else
                    hideSwipeRefreshBtn()
            }
        }
        processViewAlpha = binding?.includeProcessToast?.textViewUpdateAnchorsDetails?.alpha ?: 0.5f

        val drawable =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                resources.getDrawable(R.drawable.ic_home_page, null)
            } else {
                resources.getDrawable(R.drawable.ic_home_page)
            }
        drawable?.setBounds(0, 0, 40, 40)
        binding?.include?.groupTitleWrapper?.findViewById<TextView>(R.id.status_living)?.apply {
            setCompoundDrawables(null, null, drawable, null)
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
        binding?.include?.recyclerView?.addOnScrollListener(AnchorGroupingListener())
    }

    private fun setGraphicAdapter() {
        binding?.include?.recyclerView?.adapter = nowAnchorAdapter
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
            ConstValue.ITEM_ID_FOLLOW_ANCHOR -> {
                val position = nowAnchorAdapter.getLongClickPosition()
                val anchor = viewModel.sortedAnchorList.value!![position]
                viewModel.followAnchor(anchor)
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

//    @Suppress("DEPRECATION")
    private fun showUpdateDetails(text: String) {
        updateProcessAnimate?.cancel()
        binding?.includeProcessToast?.textViewUpdateAnchorsDetails?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                this.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            else
                this.text = Html.fromHtml(text)
            visibility = View.VISIBLE
        }
    }

    private fun completeUpdateDetails(text: String) {
        showUpdateDetails(text)
        binding?.includeProcessToast?.textViewUpdateAnchorsDetails?.apply {
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
        binding?.include?.recyclerView?.smoothScrollToPosition(0)
    }

    companion object {
        @JvmStatic
        fun newInstance() = GroupFragment()
    }

}


