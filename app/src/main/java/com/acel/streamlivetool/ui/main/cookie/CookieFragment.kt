/*
 * Copyright (c) 2020.
 * @author acel
 * 平台页
 */

package com.acel.streamlivetool.ui.main.cookie

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.databinding.FragmentCookieModeBinding
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.main.adapter.AnchorAdapter
import com.acel.streamlivetool.ui.main.adapter.AnchorGroupingListener
import com.acel.streamlivetool.ui.main.adapter.MODE_COOKIE
import com.acel.streamlivetool.ui.main.showListOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.util.PreferenceConstant
import com.acel.streamlivetool.util.ToastUtil.toast

private const val ARG_PARAM1 = "param1"

@SuppressLint("UseCompatLoadingForDrawables")
class CookieFragment : Fragment() {

    private lateinit var nowAnchorAdapter: AnchorAdapter

    internal val viewModel by viewModels<CookieViewModel>()

    private val iconDrawable by lazy {
        @Suppress("DEPRECATION") val drawable =
            PlatformDispatcher.getPlatformImpl(arguments?.getString(ARG_PARAM1)!!)?.iconRes?.let {
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
        nowAnchorAdapter = if (boolean) adapterShowImage else adapterNoImage
        setAdapter()
    }

    fun isShowImage(): Boolean {
        return nowAnchorAdapter == adapterShowImage
    }

    private var _binding: FragmentCookieModeBinding? = null
    private val binding
        get() = _binding
    var isLogining = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(CookieLifecycle(this))
        arguments?.let {
            viewModel.bindPlatform(it.getString(ARG_PARAM1)!!)
        }
        setHasOptionsMenu(true)

        viewModel.apply {
            liveDataUpdateState.observe(this@CookieFragment, {
                if (it == CookieViewModel.UpdateStatus.PREPARE || it == CookieViewModel.UpdateStatus.FINISH)
                    hideSwipeRefreshBtn()
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
        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        binding?.cookieSwipeRefresh?.setOnRefreshListener {
            viewModel.updateAnchorList()
        }
        binding?.include?.groupTitleWrapper?.findViewById<TextView>(R.id.status_living)?.apply {
            setCompoundDrawables(null, null, iconDrawable, null)
        }

    }

    internal fun hideSwipeRefreshBtn() {
        binding?.cookieSwipeRefresh?.isRefreshing = false
    }

    private fun initRecyclerView() {
        binding?.include?.recyclerView?.apply {
            layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            itemAnimator = DefaultItemAnimator().also {
                it.addDuration = 1000
                it.removeDuration = 1000
            }
        }
        nowAnchorAdapter = if (PreferenceConstant.showAnchorImage)
            adapterShowImage
        else
            adapterNoImage
        setAdapter()
        binding?.include?.recyclerView?.addOnScrollListener(AnchorGroupingListener())
    }

    private fun setAdapter() {
        binding?.include?.recyclerView?.adapter = nowAnchorAdapter
    }

    private fun showLoginTextView() {
        binding?.textViewLoginFirst?.visibility = View.VISIBLE
        binding?.textViewLoginFirst?.setOnClickListener {
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
        if (binding?.textViewLoginFirst?.visibility == View.VISIBLE)
            binding?.textViewLoginFirst?.visibility = View.GONE
    }

    private fun showListMsg(s: String) {
        binding?.textViewListMsg?.visibility = View.VISIBLE
        binding?.textViewListMsg?.text = s
    }

    private fun hideListMsg() {
        if (binding?.textViewListMsg?.visibility == View.VISIBLE)
            binding?.textViewListMsg?.visibility = View.GONE
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
            }
        return super.onContextItemSelected(item)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_list_overlay -> {
                if (isVisible)
                    (requireActivity() as MainActivity).showListOverlayWindowWithPermissionCheck(
                        viewModel.anchorList as MutableList<Anchor>
                    )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun scrollToTop() {
        binding?.include?.recyclerView?.smoothScrollToPosition(0)
    }

    companion object {
        @JvmStatic
        fun newInstance(platform: String) =
            CookieFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, platform)
                }
            }
    }

}