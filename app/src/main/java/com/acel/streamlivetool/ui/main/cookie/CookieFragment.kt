/*
 * Copyright (c) 2020.
 * @author acel
 * 平台页
 */

package com.acel.streamlivetool.ui.main.cookie

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.databinding.FragmentCookieModeBinding
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.main.adapter.AnchorGroupingListener
import com.acel.streamlivetool.ui.main.adapter.AnchorAdapter
import com.acel.streamlivetool.ui.main.adapter.MODE_COOKIE
import com.acel.streamlivetool.ui.main.showListOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.util.PreferenceConstant
import com.acel.streamlivetool.util.ToastUtil.toast

private const val ARG_PARAM1 = "param1"

class CookieFragment : Fragment() {

    private lateinit var nowAnchorAdapter: AnchorAdapter

    internal val viewModel by viewModels<CookieViewModel>()

    private val adapterShowAnchorImage by lazy {
        AnchorAdapter(
            requireContext(),
            viewModel.anchorList,
            MODE_COOKIE, true
        )
    }
    private val adapterNotShowAnchorImage by lazy {
        AnchorAdapter(
            requireContext(),
            viewModel.anchorList,
            MODE_COOKIE, false
        )
    }

    fun setShowImage(boolean: Boolean) {
        nowAnchorAdapter = if (boolean) adapterShowAnchorImage else adapterNotShowAnchorImage
        setAdapter()
    }

    fun isShowImage(): Boolean {
        return nowAnchorAdapter == adapterShowAnchorImage
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
            liveDataUpdateState.observe(this@CookieFragment, Observer {
                if (it == CookieViewModel.UpdateStatus.PREPARE || it == CookieViewModel.UpdateStatus.FINISH)
                    hideSwipeRefreshBtn()
            })
            liveDataDataChanged.observe(this@CookieFragment, Observer {
                nowAnchorAdapter.notifyAnchorsChange()
            })
            liveDataShowLoginText.observe(this@CookieFragment, Observer {
                if (it) showLoginTextView()
                else hideLoginTextView()
            })
            liveDataUpdateAnchorMsg.observe(this@CookieFragment, Observer {
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

    }

    internal fun hideSwipeRefreshBtn() {
        binding?.cookieSwipeRefresh?.isRefreshing = false
    }


    private fun initRecyclerView() {
        binding?.include?.recyclerView?.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        nowAnchorAdapter = if (PreferenceConstant.showAnchorImage)
            adapterShowAnchorImage
        else
            adapterNotShowAnchorImage
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

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
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