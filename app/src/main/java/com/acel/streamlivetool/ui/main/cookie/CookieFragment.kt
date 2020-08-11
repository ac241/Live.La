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
import com.acel.streamlivetool.databinding.FragmentCookieModeBinding
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.main.MainActivity.Companion.ListItemType
import com.acel.streamlivetool.ui.main.adapter.*
import com.acel.streamlivetool.ui.main.showListOverlayWindowWithPermissionCheck
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences

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
    private var _binding: FragmentCookieModeBinding? = null
    val binding
        get() = _binding
    var isLogining = false

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
        _binding = FragmentCookieModeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPreference()
        initRecyclerView()

        binding?.cookieSwipeRefresh?.setOnRefreshListener {
            viewModel.getAnchors()
        }
    }

    internal fun hideSwipeRefreshBtn() {
        binding?.cookieSwipeRefresh?.isRefreshing = false
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
        binding?.include?.recyclerView?.addOnScrollListener(AnchorListAddTitleListener())
    }

    internal fun setGraphicAdapter() {
        binding?.include?.recyclerView?.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding?.include?.recyclerView?.adapter = graphicAnchorAdapter
        nowAnchorAnchorAdapter = graphicAnchorAdapter
    }

    fun setTextAdapter() {
        binding?.include?.recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding?.include?.recyclerView?.adapter = textAnchorAdapter
        nowAnchorAnchorAdapter = textAnchorAdapter
    }


    fun showLoginTextView() {
        runOnUiThread {
            binding?.textViewLoginFirst?.visibility = View.VISIBLE
            binding?.textViewLoginFirst?.setOnClickListener {
                val intent = Intent(context, LoginActivity::class.java).also {
                    it.putExtra("platform",
                        platform?.let { it1 -> PlatformDispatcher.getPlatformImpl(it1)?.platform })
                }
                startActivity(intent)
                startLogin()
            }
        }
    }

    private fun startLogin() {
        isLogining = true
    }

    internal fun loginFinish() {
        isLogining = false
    }

    fun hideLoginTextView() {
        if (binding?.textViewLoginFirst?.visibility == View.VISIBLE)
            runOnUiThread {
                binding?.textViewLoginFirst?.visibility = View.GONE
            }
    }

    fun showListMsg(s: String) {
        runOnUiThread {
            binding?.textViewListMsg?.visibility = View.VISIBLE
            binding?.textViewListMsg?.text = s
        }
    }

    fun hideListMsg() {
        if (binding?.textViewListMsg?.visibility == View.VISIBLE)
            runOnUiThread {
                binding?.textViewListMsg?.visibility = View.GONE
            }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (isVisible)
            when (item.itemId) {
                R.id.action_item_add_to_main_mode -> {
                    val position =
                        nowAnchorAnchorAdapter.getLongClickPosition()
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