/*
 * Copyright (c) 2020.
 * @author acel
 * 平台页
 */

package com.acel.streamlivetool.ui.main.cookie

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.showListOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.const_value.PreferenceVariable
import com.acel.streamlivetool.databinding.FragmentCookieModeBinding
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.ui.main.HandleContextItemSelect
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.main.adapter.AnchorAdapter
import com.acel.streamlivetool.ui.main.adapter.AnchorItemDecoration
import com.acel.streamlivetool.ui.main.adapter.AnchorSpanSizeLookup
import com.acel.streamlivetool.ui.main.adapter.MODE_COOKIE
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.ToastUtil.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PLATFORM_KEY = "platform_key"

class CookieFragment : Fragment() {

    internal val viewModel by viewModels<CookieViewModel>()

    private val iconDrawable by lazy {
        PlatformDispatcher.getPlatformImpl(arguments?.getString(PLATFORM_KEY)!!)?.iconRes?.let {
            ResourcesCompat.getDrawable(resources, it, null)?.apply {
                setBounds(0, 0, 40, 40)
            }
        }
    }
    private val anchorAdapter by lazy {
        AnchorAdapter(requireContext(), viewModel.anchorList, MODE_COOKIE, true, iconDrawable!!)
    }

    private fun setShowImage(boolean: Boolean) {
        anchorAdapter.apply {
            if (boolean != showImage) {
                showImage = boolean
                notifyAnchorsChange()
            }
        }
    }

    private lateinit var _binding: FragmentCookieModeBinding
    private val binding
        get() = _binding
    var isLogining = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(CookieLifecycle(this))
        arguments?.let {
            viewModel.bindPlatform(it.getString(PLATFORM_KEY)!!)
        }
        setHasOptionsMenu(true)
    }

    private fun checkShowImage() {
        if (PreferenceVariable.showAnchorImage.value!!) {
            //如果显示图片
            if (AppUtil.isWifiConnected()) {
                //如果wifi连接
                setShowImage(true)
            } else {
                //如果wifi未连接
                if (PreferenceVariable.showAnchorImageWhenMobileData.value!!) {
                    //如果流量时显示图片
                    setShowImage(true)
                } else {
                    //如果流量时不显示图片
                    setShowImage(false)
                }
            }
        } else {
            //如果不显示图片
            setShowImage(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCookieModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        binding.cookieSwipeRefresh.apply {
            setOnRefreshListener {
                viewModel.updateAnchorList()
            }
            setProgressBackgroundColorSchemeResource(R.color.swipe_refresh)
            setColorSchemeResources(R.color.colorPrimary)
        }
        binding.includeType.groupTitleWrapper.findViewById<TextView>(R.id.status)?.apply {
            setCompoundDrawables(null, null, iconDrawable, null)
        }
        //observe live data
        viewModel.apply {
            liveDataUpdateState.observe(viewLifecycleOwner, {
                if (it == null)
                    return@observe
                when (it) {
                    CookieViewModel.UpdateStatus.IDLE, CookieViewModel.UpdateStatus.FINISH ->
                        updateFinish()
                    CookieViewModel.UpdateStatus.UPDATING ->
                        updating()
                }
            })
            liveDataDataChanged.observe(viewLifecycleOwner, {
                anchorAdapter.notifyAnchorsChange()
            })
            liveDataShowLoginText.observe(viewLifecycleOwner, {
                if (it) showLoginTextView()
                else hideLoginTextView()
            })
            liveDataUpdateAnchorMsg.observe(viewLifecycleOwner, {
                if (it.show) it.msg?.let { it1 -> showListMsg(it1) }
                else hideListMsg()
            })
        }
        /**
         * 显示图片observer
         */
        PreferenceVariable.showAnchorImage.observe(viewLifecycleOwner) {
            checkShowImage()
        }
        PreferenceVariable.showAnchorImageWhenMobileData.observe(viewLifecycleOwner) {
            checkShowImage()
        }
    }

    private var updatingTime: Long = 0L

    private fun updating() {
        synchronized(updatingTime) {
            updatingTime = System.currentTimeMillis()
            binding.cookieSwipeRefresh.isRefreshing = true
        }
    }

    private fun updateFinish() {
        synchronized(updatingTime) {
            //如果更新数据时间小于两秒，一定时间后再隐藏。
            if (System.currentTimeMillis() - updatingTime > 2000) {
                binding.cookieSwipeRefresh.isRefreshing = false
            } else {
                lifecycleScope.launch(Dispatchers.Default) {
                    delay(500)
                    withContext(Dispatchers.Main) {
                        binding.cookieSwipeRefresh.isRefreshing = false
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        iniRecyclerViewLayoutManager(newConfig.orientation)
    }

    private fun iniRecyclerViewLayoutManager(orientation: Int) {
        binding.includeType.recyclerView.layoutManager =
            GridLayoutManager(
                requireContext(),
                if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 4
            ).apply {
                spanSizeLookup = AnchorSpanSizeLookup(anchorAdapter, this)
            }
    }

    private fun initRecyclerView() {
        iniRecyclerViewLayoutManager(resources.configuration.orientation)
        binding.includeType.recyclerView.adapter = anchorAdapter
        anchorAdapter.showImage = PreferenceVariable.showAnchorImage.value!!
        iconDrawable?.let {
            binding.includeType.recyclerView.addItemDecoration(AnchorItemDecoration(it))
        }
    }


    private fun showLoginTextView() {
        binding.textViewLoginFirst.visibility = View.VISIBLE
        binding.textViewLoginFirst.setOnClickListener {
            val intent = Intent(context, LoginActivity::class.java).also {
                it.putExtra(
                    "platform",
                    viewModel.platform
                )
            }
            startActivity(intent)
            startLogin()
        }
    }

    private fun startLogin() {
        isLogining = true
    }

    internal fun loginFinish() {
        isLogining = false
    }

    private fun hideLoginTextView() {
        if (binding.textViewLoginFirst.visibility == View.VISIBLE)
            binding.textViewLoginFirst.visibility = View.GONE
    }

    private fun showListMsg(s: String) {
        binding.textViewListMsg.visibility = View.VISIBLE
        binding.textViewListMsg.text = s
    }

    private fun hideListMsg() {
        if (binding.textViewListMsg.visibility == View.VISIBLE)
            binding.textViewListMsg.visibility = View.GONE
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (isResumed)
            when (item.itemId) {
                R.id.action_item_add_to_main_mode -> {
                    val position =
                        anchorAdapter.getLongClickPosition()
                    val result = AnchorRepository.getInstance()
                        .insertAnchor(viewModel.anchorList[position])
                    toast(result.second)
                }
                else -> {
                    val position = anchorAdapter.getLongClickPosition()
                    val anchor = viewModel.anchorList[position]
                    HandleContextItemSelect.handle(
                        requireContext(),
                        item.itemId,
                        anchor,
                        viewModel.anchorList
                    )
                }
            }
        return super.onContextItemSelected(item)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_list_overlay -> {
                if (isVisible)
                    (requireActivity() as MainActivity)
                        .showListOverlayWindowWithPermissionCheck(viewModel.anchorList as MutableList<Anchor>)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun scrollToTop() {
        if (isAdded)
            binding.includeType.recyclerView.smoothScrollToPosition(0)
    }

    companion object {
        @JvmStatic
        fun newInstance(platform: String) =
            CookieFragment().apply {
                arguments = Bundle().apply {
                    putString(PLATFORM_KEY, platform)
                }
            }
    }

}