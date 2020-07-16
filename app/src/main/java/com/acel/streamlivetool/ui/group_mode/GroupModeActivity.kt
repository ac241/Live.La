package com.acel.streamlivetool.ui.group_mode

import android.Manifest
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.base.MyApplication.Companion.finishAllActivity
import com.acel.streamlivetool.base.MyApplication.Companion.isActivityFirst
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.cookie_mode.CookieModeActivity
import com.acel.streamlivetool.ui.settings.SettingsActivity
import com.acel.streamlivetool.ui.view.ListOverlayWindow
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.activity_group_mode.*
import kotlinx.android.synthetic.main.layout_group_mode_grid_view.*
import kotlinx.android.synthetic.main.layout_group_mode_recycler_view.*
import permissions.dispatcher.*

@RuntimePermissions
class GroupModeActivity : BaseActivity(), GroupModeConstract.View {
    private lateinit var fragmentmanager: FragmentManager
    lateinit var presenter: GroupModePresenter
    private var listViewType = ListViewType.RecyclerView
    private val addAnchorFragment = AddAnchorFragment()
    var listOverlayLayout: View? = null
    var recyclerViewListOverlay: RecyclerView? = null
    override fun getResLayoutId(): Int {
        return R.layout.activity_group_mode
    }

    enum class ListViewType {
        RecyclerView, GridView;
    }

    override fun onBackPressed() {
        if (isActivityFirst(this)) {
            finishAllActivity()
            super.onBackPressed()
        } else
            startCookieModeActivity()
    }

    override fun createDo() {
        presenter = GroupModePresenter(this)
        initToolbar()
        initPreference()
        when (listViewType) {
            ListViewType.RecyclerView -> initRecyclerView()
            ListViewType.GridView -> initGridView()
        }

        main_swipe_refresh.setOnRefreshListener {
            presenter.anchorRepository.anchorList.value?.let {
                if (it.isNotEmpty())
                    presenter.getAllAnchorsStatus()
                else
                    hideSwipeRefreshBtn()
            }
        }
    }

    private fun initPreference() {
        val type = defaultSharedPreferences.getString(
            resources.getString(R.string.pref_key_group_mode_list_type),
            resources.getString(R.string.grid_view)
        )

        listViewType = when (type) {
            "recycler_view" -> ListViewType.RecyclerView
            "grid_view" -> ListViewType.GridView
            else -> ListViewType.RecyclerView
        }
    }

    private fun initGridView() {
        viewStub_group_mode_grid_view.inflate()
        group_mode_gridView.adapter = GroupModeGridViewAdapter(
            this,
            presenter
        )
        //解决滑动冲突
        group_mode_gridView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
            override fun onScroll(
                view: AbsListView,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                if (firstVisibleItem == 0) {
                    if (group_mode_gridView.childCount != 0) {
                        val firstVisibleItemView: View = group_mode_gridView.getChildAt(0)
                        main_swipe_refresh.isEnabled = firstVisibleItemView.top >= 0
                    } else
                        main_swipe_refresh.isEnabled = true
                } else {
                    main_swipe_refresh.isEnabled = false
                }
//
//                // 判断滚动到底部
//                if (view.lastVisiblePosition == view.count - 1) {
//                }
            }
        })

    }

    private fun initRecyclerView() {
        viewStub_group_mode_recycler_view.inflate()
        group_mode_recycler_view.layoutManager = LinearLayoutManager(this)
        val recyclerViewAdapter = GroupModeRecyclerViewAdapter(
            this,
            presenter
        )
        group_mode_recycler_view.adapter = recyclerViewAdapter
        //关闭刷新item时CardView的闪烁提示
        group_mode_recycler_view.itemAnimator?.changeDuration = 0
    }

    private fun initToolbar() {
//        setSupportActionBar(main_toolbar)
//        supportActionBar?.setTitle(R.string.app_name)
    }

    private fun showAddAnchorFragment() {
        fragmentmanager = supportFragmentManager
        fragmentmanager.beginTransaction().commit()
        addAnchorFragment.show(fragmentmanager, "add_anchor_fragment")
    }


    @Synchronized
    override fun refreshAnchorStatus() {
        when (listViewType) {
            ListViewType.RecyclerView ->
                group_mode_recycler_view.adapter?.notifyDataSetChanged()
            ListViewType.GridView ->
                (group_mode_gridView.adapter as GroupModeGridViewAdapter).notifyDataSetChanged()
        }
        hideSwipeRefreshBtn()
    }

    override fun addAnchorSuccess(anchor: Anchor) {
        val addAnchorFragment =
            fragmentmanager.findFragmentByTag("add_anchor_fragment") as AddAnchorFragment
        addAnchorFragment.onGetAnchorInfoSuccess(anchor)
    }

    override fun addAnchorFailed(reason: String) {
        val addAnchorFragment =
            fragmentmanager.findFragmentByTag("add_anchor_fragment") as AddAnchorFragment
        addAnchorFragment.onGetAnchorInfoFailed(reason)
    }

    @Synchronized
    override fun refreshAnchorList() {
        when (listViewType) {
            ListViewType.RecyclerView -> group_mode_recycler_view.adapter?.notifyDataSetChanged()
            ListViewType.GridView -> (group_mode_gridView.adapter as GroupModeGridViewAdapter).notifyDataSetChanged()
        }
        recyclerViewListOverlay?.adapter?.notifyDataSetChanged()
        hideSwipeRefreshBtn()
    }

    override fun destroyDo() {
        presenter.onDestroy()
    }

    private fun hideSwipeRefreshBtn() {
        main_swipe_refresh.isRefreshing = false
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_item_delete -> {
                val position = when (listViewType) {
                    ListViewType.RecyclerView -> (group_mode_recycler_view.adapter as GroupModeRecyclerViewAdapter).getPosition()
                    ListViewType.GridView -> (group_mode_gridView.adapter as GroupModeGridViewAdapter).getPosition()
                }
                presenter.anchorRepository.deleteAnchor(presenter.anchorRepository.anchorList.value!![position])
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.group_mode_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_main_setting -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.action_cookie_anchor -> {
                showAddAnchorFragment()
            }
            R.id.action_list_overlay -> {
                if (listOverlayLayout == null)
                    showOverlayWindowWithPermissionCheck()
                else
                    removeListOverlayWindow()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("UNUSED_PARAMETER")
    fun fabClick(view: View) {
        startCookieModeActivity()
    }

    private fun startCookieModeActivity() {
        startActivity(Intent(this, CookieModeActivity::class.java))
    }

    /**
     * 创建List悬浮窗
     */
    private fun createListOverlayWindow() {
        val listOverlayWindow = ListOverlayWindow.instance.create(this)
        listOverlayWindow.setMovable(windowManager)
        listOverlayLayout = listOverlayWindow.getLayout()
        if (listOverlayLayout != null) {
            recyclerViewListOverlay =
                listOverlayLayout?.findViewById(R.id.recycler_view_list_overlay)
            recyclerViewListOverlay?.layoutManager = LinearLayoutManager(this)
            recyclerViewListOverlay?.adapter = ListOverlayAdapter(this, presenter)
            val btnClose = listOverlayLayout?.findViewById<ImageView>(R.id.btn_list_overlay_close)
            btnClose?.setOnClickListener {
                removeListOverlayWindow()
            }
        }
    }

    /**
     * 移除List悬浮窗
     */
    private fun removeListOverlayWindow() {
        windowManager.removeView(listOverlayLayout)
        listOverlayLayout = null
        recyclerViewListOverlay = null
    }

    @NeedsPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun showOverlayWindow() {
        createListOverlayWindow()
    }

    @OnShowRationale(Manifest.permission.SYSTEM_ALERT_WINDOW)
    internal fun showRationaleForSystemAlertWindow(request: PermissionRequest?) {
    }

    @OnPermissionDenied(Manifest.permission.SYSTEM_ALERT_WINDOW)
    internal fun showDeniedForSystemAlertWindow() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResult(requestCode)
    }
}
