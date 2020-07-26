package com.acel.streamlivetool.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.ui.adapter.*
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.fragment_cookie_anchors.*
import kotlinx.android.synthetic.main.layout_anchor_recycler_view.*
import kotlinx.android.synthetic.main.layout_login_first.*

class CookieAnchorsFragment(val platform: IPlatform) : Fragment() {
    internal lateinit var nowAnchorAnchorAdapter: AnchorAdapterWrapper
    private var layoutManagerType = ListItemType.Text
    private val viewModel by viewModels<CookieAnchorsViewModel> {
        CookieAnchorsViewModel.ViewModeFactory(this)
    }

    enum class ListItemType {
        Text, Graphic;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        initRecyclerView()

        cookie_anchor_swipe_refresh.setOnRefreshListener {
            viewModel.getAnchors()
        }
    }

    private fun initPreference() {
        val type = defaultSharedPreferences.getString(
            resources.getString(R.string.pref_key_cookie_mode_list_type),
            resources.getString(R.string.string_grid_view)
        )

        layoutManagerType = when (type) {
            resources.getString(R.string.string_recycler_view) -> ListItemType.Text
            resources.getString(R.string.string_grid_view) -> ListItemType.Graphic
            else -> ListItemType.Graphic
        }
    }

    private fun initRecyclerView() {
        when (layoutManagerType) {
            ListItemType.Text -> {
                recycler_view.layoutManager = LinearLayoutManager(requireContext())
                val adapter = TextAnchorAdapter(
                    requireContext(),
                    viewModel.anchorList,
                    MODE_COOKIE
                )
                recycler_view.adapter = adapter
                nowAnchorAnchorAdapter = adapter
            }

            ListItemType.Graphic -> {
                recycler_view.layoutManager =
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                val adapter = GraphicAnchorAdapter(
                    requireContext(),
                    viewModel.anchorList,
                    MODE_COOKIE
                )
                recycler_view.adapter = adapter
                nowAnchorAnchorAdapter = adapter
            }
        }
        recycler_view.addOnScrollListener(AnchorListAddTitleListener())
    }


    fun showLoginSub() {
        runOnUiThread {
            viewStub_login_first.inflate()
            textView_login_first.setOnClickListener {
                val intent = Intent(context, LoginActivity::class.java).also {
                    it.putExtra("platform", platform.platform)
                }
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAnchors()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (isVisible)
            when (item.itemId) {
                R.id.action_item_add_to_main_mode -> {
                    val position =
                        nowAnchorAnchorAdapter.getLongClickPosition()
                    val result = AnchorRepository.getInstance(requireContext().applicationContext)
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
                        viewModel.anchorList
                    )
            }
        }
        return super.onOptionsItemSelected(item)
    }

}