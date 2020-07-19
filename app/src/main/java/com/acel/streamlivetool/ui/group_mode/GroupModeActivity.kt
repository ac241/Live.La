package com.acel.streamlivetool.ui.group_mode

import android.Manifest
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.base.MyApplication.Companion.finishAllActivity
import com.acel.streamlivetool.base.MyApplication.Companion.isActivityFirst
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.adapter.AnchorGridViewAnchorAdapter
import com.acel.streamlivetool.ui.adapter.AnchorRecyclerViewAnchorAdapter
import com.acel.streamlivetool.ui.cookie_mode.CookieModeActivity
import com.acel.streamlivetool.ui.adapter.AnchorAdapterWrapper
import com.acel.streamlivetool.ui.overlay.ListOverlayWindowManager
import com.acel.streamlivetool.ui.overlay.PlayerOverlayWindowManager
import com.acel.streamlivetool.ui.public_interface.PlayOverlayFunction
import com.acel.streamlivetool.ui.settings.SettingsActivity
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.activity_group_mode.*
import kotlinx.android.synthetic.main.anchor_list_view.*
import kotlinx.android.synthetic.main.layout_group_mode_grid_view.*
import kotlinx.android.synthetic.main.layout_group_mode_recycler_view.*
import permissions.dispatcher.*


@RuntimePermissions
class GroupModeActivity : BaseActivity(), GroupModeConstract.View, PlayOverlayFunction {

    private lateinit var fragmentmanager: FragmentManager
    lateinit var presenter: GroupModePresenter
    private var listViewType = ListViewType.RecyclerView
    private val addAnchorFragment = AddAnchorFragment()
    private lateinit var nowAnchorAnchorAdapter: AnchorAdapterWrapper


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

    override fun createdDo() {
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
            resources.getString(R.string.string_grid_view)
        )

        listViewType = when (type) {
            resources.getString(R.string.string_recycler_view) -> ListViewType.RecyclerView
            resources.getString(R.string.string_grid_view) -> ListViewType.GridView
            else -> ListViewType.RecyclerView
        }
    }

    private fun initGridView() {
        viewStub_grid_view.inflate()
        val adapter = AnchorGridViewAnchorAdapter(
            this,
            presenter.sortedAnchorList,
            presenter.anchorAttributeMap
        )
        grid_view.adapter = adapter
        nowAnchorAnchorAdapter = adapter
        //解决滑动冲突
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
                        main_swipe_refresh.isEnabled = firstVisibleItemView.top >= 0
                    } else
                        main_swipe_refresh.isEnabled = true
                } else {
                    main_swipe_refresh.isEnabled = false
                }
//                // 判断滚动到底部
//                if (view.lastVisiblePosition == view.count - 1) {
//                }
            }
        })
    }

    private fun initRecyclerView() {
        viewStub_recycler_view.inflate()
        recycler_view.layoutManager = LinearLayoutManager(this)
        val recyclerViewAdapter =
            AnchorRecyclerViewAnchorAdapter(
                this,
                presenter.sortedAnchorList,
                presenter.anchorAttributeMap
            )
        recycler_view.adapter = recyclerViewAdapter
        nowAnchorAnchorAdapter = recyclerViewAdapter
        //关闭刷新item时CardView的闪烁提示
        recycler_view.itemAnimator?.changeDuration = 0
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
    override fun refreshAnchorAttribute() {
        nowAnchorAnchorAdapter.notifyAnchorsChange()
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
        nowAnchorAnchorAdapter.notifyAnchorsChange()
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
                val position = nowAnchorAnchorAdapter.getLongClickPosition()
                presenter.anchorRepository.deleteAnchor(presenter.sortedAnchorList[position])
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
                showListOverlayWindowWithPermissionCheck()
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

    @NeedsPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun showListOverlayWindow() {
        ListOverlayWindowManager.instance.toggleShow(
            this,
            presenter.sortedAnchorList,
            presenter.anchorAttributeMap
        )
    }

    @NeedsPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun showPlayerOverlayWindow(anchor: Anchor) {
        PlayerOverlayWindowManager.instance.play(anchor)
    }

    @OnShowRationale(Manifest.permission.SYSTEM_ALERT_WINDOW)
    internal fun showRationaleForSystemAlertWindow(request: PermissionRequest?) {
    }

    @OnPermissionDenied(Manifest.permission.SYSTEM_ALERT_WINDOW)
    internal fun showDeniedForSystemAlertWindow() {
        toast("无权限")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResult(requestCode)
    }

    override fun playStream(anchor: Anchor) {
        showPlayerOverlayWindowWithPermissionCheck(anchor)
    }
}
