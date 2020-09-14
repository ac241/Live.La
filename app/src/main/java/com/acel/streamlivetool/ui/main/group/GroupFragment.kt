package com.acel.streamlivetool.ui.main.group

import android.animation.Animator
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.databinding.FragmentGroupModeBinding
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.main.adapter.AnchorAdapterWrapper
import com.acel.streamlivetool.ui.main.adapter.AnchorGroupingListener
import com.acel.streamlivetool.ui.main.adapter.GraphicAnchorAdapter
import com.acel.streamlivetool.ui.main.adapter.MODE_GROUP
import com.acel.streamlivetool.ui.main.showListOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.util.PreferenceConstant
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class GroupFragment : Fragment() {

    val viewModel by viewModels<GroupViewModel> { GroupViewModel.ViewModeFactory() }
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

    private var updateProcessAnimate: ViewPropertyAnimator? = null
    var processViewAlpha: Float = 0.5f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupModeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        Log.d("ffraonResume", "resume")
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
            liveDataUpdateDetails.observe(this@GroupFragment, Observer {
                if (it == GroupViewModel.UpdateState.PREPARE || it == GroupViewModel.UpdateState.FINISH)
                    hideSwipeRefreshBtn()
            })
            liveDataUpdateAnchorResult.observe(this@GroupFragment, Observer { result ->
                result.result?.let {
                    if (!result.complete)
                        showUpdateDetails(it)
                    else
                        completeUpdateDetails(it)
                }
            })
            snackBarMsg.observe(this@GroupFragment, Observer {
                it?.let {
                    if (it.isNotEmpty()) {
                        Snackbar.make(requireActivity().main_container, it, 5000).show()
                    }
                }
            })
            liveDataCookieInvalid.observe(this@GroupFragment, Observer {
                if (it != null) {
                    alertCookieInvalid(it)
                }
            })
        }
    }


    private fun alertCookieInvalid(platform: String) {
        val platformImpl = PlatformDispatcher.getPlatformImpl(platform)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("${platformImpl?.platformName} 的Cookie无效")
        builder.setMessage("是否登录？")
        builder.setPositiveButton("是") { _, _ ->
            val intent = Intent(MyApplication.application, LoginActivity::class.java).also {
                it.putExtra(
                    "platform",
                    platformImpl?.platform
                )
            }
            MyApplication.application.startActivity(intent)
        }
        builder.show()
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
        processViewAlpha = binding?.includeProcessToast?.textViewUpdateAnchorsDetails?.alpha ?: 0.5f
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

    @Suppress("DEPRECATION")
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

    companion object {
        @JvmStatic
        fun newInstance() = GroupFragment()
    }

}