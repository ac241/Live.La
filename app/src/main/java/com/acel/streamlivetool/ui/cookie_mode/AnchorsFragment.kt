package com.acel.streamlivetool.ui.cookie_mode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.AnchorsCookieMode
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.ui.adapter.AnchorRecyclerViewAnchorAdapter
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.ui.adapter.AnchorAdapterWrapper
import com.acel.streamlivetool.ui.adapter.AnchorGridViewAnchorAdapter
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.activity_cookie_mode.*
import kotlinx.android.synthetic.main.anchor_list_view.*
import kotlinx.android.synthetic.main.fragment_cookie_anchors.*
import kotlinx.android.synthetic.main.layout_group_mode_grid_view.*
import kotlinx.android.synthetic.main.layout_group_mode_recycler_view.*
import kotlinx.android.synthetic.main.layout_login_first.*

class AnchorsFragment(val platform: IPlatform) : Fragment() {

    private var addCookie: Boolean = false
    private val anchors = mutableListOf<AnchorsCookieMode.Anchor>()
    private lateinit var nowAnchorAnchorAdapter: AnchorAdapterWrapper
    private var listViewType = ListViewType.RecyclerView
    private val viewPager by lazy { (requireActivity() as CookieModeActivity).viewPager }

    enum class ListViewType {
        RecyclerView, GridView;
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cookie_anchors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPreference()
        when (listViewType) {
            ListViewType.RecyclerView -> initRecyclerView()
            ListViewType.GridView -> initGridView()
        }

        cookie_anchor_swipe_refresh.setOnRefreshListener {
            getAnchors()
        }
        getAnchors()
    }

    private fun initPreference() {
        val type = defaultSharedPreferences.getString(
            resources.getString(R.string.pref_key_cookie_mode_list_type),
            resources.getString(R.string.string_grid_view)
        )

        listViewType = when (type) {
            resources.getString(R.string.string_recycler_view) -> ListViewType.RecyclerView
            resources.getString(R.string.string_grid_view) -> ListViewType.GridView
            else -> ListViewType.RecyclerView
        }
    }

    private fun initRecyclerView() {
        viewStub_recycler_view.inflate()
        recycler_view.layoutManager = LinearLayoutManager(context)
        val adapter = AnchorRecyclerViewAnchorAdapter(
            activity as Context,
            anchors
        )
        recycler_view.adapter = adapter
        nowAnchorAnchorAdapter = adapter
    }

    private fun initGridView() {
        viewStub_grid_view.inflate()
        val adapter = AnchorGridViewAnchorAdapter(
            activity as Context,
            anchors
        )
        grid_view.adapter = adapter
        nowAnchorAnchorAdapter = adapter
        //解决与swipeRefreshLayout滑动冲突
        grid_view.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
            override fun onScroll(
                view: AbsListView,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                if (firstVisibleItem == 0) {
                    if (grid_view.childCount != 0) {
                        val firstVisibleItemView: View = grid_view.getChildAt(0)
                        cookie_anchor_swipe_refresh.isEnabled = firstVisibleItemView.top >= 0
                    } else
                        cookie_anchor_swipe_refresh.isEnabled = true
                } else {
                    cookie_anchor_swipe_refresh.isEnabled = false
                }
//                // 判断滚动到底部
//                if (view.lastVisiblePosition == view.count - 1) {
//                }
            }
        })
    }

    private fun getAnchors() {
        MainExecutor.execute {
            val anchorsCookieMode = platform.getAnchorsWithCookieMode()
            if (!anchorsCookieMode.cookieOk) {
                if (viewStub_login_first != null)
                    showLoginSub()
            } else {
                with(anchorsCookieMode.anchors) {
                    if (this != null) {
                        anchors.clear()
                        anchors.addAll(this)
                        runOnUiThread {
                            nowAnchorAnchorAdapter.notifyAnchorsChange()
                        }
                    }
                }

                if (addCookie) {
                    runOnUiThread {
                        login_first_wrapper.visibility = View.GONE
                    }
                }
            }
            runOnUiThread {
                cookie_anchor_swipe_refresh.isRefreshing = false
            }
        }
    }

    private fun showLoginSub() {
        runOnUiThread {
            viewStub_login_first.inflate()
            textView_login_first.setOnClickListener {
                val intent = Intent(context, LoginActivity::class.java).also {
                    it.putExtra("platform", platform.platform)
                }
                startActivity(intent)
                addCookie = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (addCookie) {
            getAnchors()
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (isVisible)
            when (item.itemId) {
                R.id.action_item_add_to_main_mode -> {
                    val position =
                        (recycler_view.adapter as AnchorAdapterWrapper).getLongClickPosition()
                    val result = AnchorRepository.getInstance(requireContext().applicationContext)
                        .insertAnchor(anchors[position])
                    toast(result.second)
                }
            }
        return super.onContextItemSelected(item)
    }

}