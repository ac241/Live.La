/*
 * Copyright (c) 2020.
 * @author acel
 * 平台页
 */

package com.acel.streamlivetool.ui.main.cookie

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.showListOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.databinding.FragmentCookieModeBinding
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.ui.main.HandleContextItemSelect
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.main.adapter.AnchorAdapter
import com.acel.streamlivetool.ui.main.adapter.AnchorGroupingListener
import com.acel.streamlivetool.ui.main.adapter.MODE_COOKIE
import com.acel.streamlivetool.util.PreferenceConstant
import com.acel.streamlivetool.util.ToastUtil.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PLATFORM_KEY = "platform_key"

@SuppressLint("UseCompatLoadingForDrawables")
class CookieFragment : Fragment() {

    private lateinit var nowAnchorAdapter: AnchorAdapter

    internal val viewModel by viewModels<CookieViewModel>()

    private val iconDrawable by lazy {
        @Suppress("DEPRECATION") val drawable =
            PlatformDispatcher.getPlatformImpl(arguments?.getString(PLATFORM_KEY)!!)?.iconRes?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    resources.getDrawable(it, null)
                } else {
                    resources.getDrawable(it)
                }
            }
        drawable?.setBounds(0, 0, 40, 40)
        drawable
    }
    private val adapterShowImage by lazy {
        AnchorAdapter(
            requireContext(),
            viewModel.anchorList,
            MODE_COOKIE, true
        )
    }
    private val adapterNoImage by lazy {
        AnchorAdapter(
            requireContext(),
            viewModel.anchorList,
            MODE_COOKIE, false
        )
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

    private fun isShowImage(): Boolean = nowAnchorAdapter == adapterShowImage

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

        viewModel.apply {
            liveDataUpdateState.observe(this@CookieFragment, {
                if (it == null)
                    return@observe
                when (it) {
                    CookieViewModel.UpdateStatus.IDLE, CookieViewModel.UpdateStatus.FINISH ->
                        updateFinish()
                    CookieViewModel.UpdateStatus.UPDATING ->
                        updating()
                }
            })
            liveDataDataChanged.observe(this@CookieFragment, {
                nowAnchorAdapter.notifyAnchorsChange()
            })
            liveDataShowLoginText.observe(this@CookieFragment, {
                if (it) showLoginTextView()
                else hideLoginTextView()
            })
            liveDataUpdateAnchorMsg.observe(this@CookieFragment, {
                if (it.show) it.msg?.let { it1 -> showListMsg(it1) }
                else hideListMsg()
            })
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
        binding.cookieSwipeRefresh.setOnRefreshListener {
            viewModel.updateAnchorList()
        }
        binding.include.groupTitleWrapper.findViewById<TextView>(R.id.status_living)?.apply {
            setCompoundDrawables(null, null, iconDrawable, null)
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
                        nowAnchorAdapter.getLongClickPosition()
                    val result = AnchorRepository.getInstance()
                        .insertAnchor(viewModel.anchorList[position])
                    toast(result.second)
                }
                else -> {
                    val position = nowAnchorAdapter.getLongClickPosition()
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
        binding.include.recyclerView.smoothScrollToPosition(0)
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