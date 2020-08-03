package com.acel.streamlivetool.ui.main.cookie

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
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.main.adapter.*
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.main.MainActivity.Companion.ListItemType
import com.acel.streamlivetool.ui.main.showListOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.fragment_cookie_mode.*
import kotlinx.android.synthetic.main.layout_anchor_recycler_view.*

private const val ARG_PARAM1 = "param1"

class CookieFragment : Fragment() {

    internal lateinit var nowAnchorAnchorAdapter: AnchorAdapterWrapper
    var platform: String? = null
    internal var layoutManagerType =
        ListItemType.Text
    internal val viewModel by viewModels<CookieViewModel> {
        CookieViewModel.ViewModeFactory(
            this
        )
    }
    private val textAnchorAdapter by lazy {
        TextAnchorAdapter(
            requireContext(),
            viewModel.anchorList,
            MODE_COOKIE
        )
    }

    private val graphicAnchorAdapter by lazy {
        GraphicAnchorAdapter(
            requireContext(),
            viewModel.anchorList,
            MODE_COOKIE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(CookieLifecycle(this))
        arguments?.let {
            platform = it.getString(ARG_PARAM1)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cookie_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPreference()
        initRecyclerView()

        cookie_swipe_refresh.setOnRefreshListener {
            viewModel.getAnchors()
        }
    }

    internal fun hideSwipeRefreshBtn() {
        cookie_swipe_refresh.isRefreshing = false
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
                setTextAdapter()
            }
            ListItemType.Graphic -> {
                setGraphicAdapter()
            }
        }
        recycler_view.addOnScrollListener(AnchorListAddTitleListener())
    }

    internal fun setGraphicAdapter() {
        recycler_view.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recycler_view.adapter = graphicAnchorAdapter
        nowAnchorAnchorAdapter = graphicAnchorAdapter
    }

    fun setTextAdapter() {
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.adapter = textAnchorAdapter
        nowAnchorAnchorAdapter = textAnchorAdapter
    }


    fun showLoginTextView() {
        runOnUiThread {
            textView_login_first.visibility = View.VISIBLE
            textView_login_first.setOnClickListener {
                val intent = Intent(context, LoginActivity::class.java).also {
                    it.putExtra("platform",
                        platform?.let { it1 -> PlatformDispatcher.getPlatformImpl(it1)?.platform })
                }
                startActivity(intent)
            }
        }
    }

    fun hideLoginTextView() {
        if (textView_login_first.visibility == View.VISIBLE)
            runOnUiThread {
                textView_login_first.visibility = View.GONE
            }
    }

    fun showListMsg(s: String) {
        runOnUiThread {
            textView_list_msg.visibility = View.VISIBLE
            textView_list_msg.text = s
        }
    }
    fun hideListMsg() {
        if (textView_list_msg.visibility == View.VISIBLE)
            runOnUiThread {
                textView_list_msg.visibility = View.GONE
            }
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