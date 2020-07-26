package com.acel.streamlivetool.ui.group_mode

import android.Manifest
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.base.MyApplication.Companion.finishAllActivity
import com.acel.streamlivetool.base.MyApplication.Companion.isActivityFirst
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.adapter.*
import com.acel.streamlivetool.ui.cookie_mode.CookieModeActivity
import com.acel.streamlivetool.ui.overlay.ListOverlayWindowManager
import com.acel.streamlivetool.ui.overlay.PlayerOverlayWindowManager
import com.acel.streamlivetool.ui.public_interface.PlayOverlayFunction
import com.acel.streamlivetool.ui.settings.SettingsActivity
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.fragment_group_mode.*
import kotlinx.android.synthetic.main.layout_anchor_recycler_view.*
import permissions.dispatcher.*


@RuntimePermissions
class GroupModeActivity : BaseActivity(), GroupModeConstract.View, PlayOverlayFunction {

    private lateinit var fragmentmanager: FragmentManager
    lateinit var presenter: GroupModePresenter
    private var layoutManagerType = ListItemType.Text
    private val addAnchorFragment = AddAnchorFragment()
    private lateinit var nowAnchorAnchorAdapter: AnchorAdapterWrapper


    override fun getResLayoutId(): Int {
        return R.layout.fragment_group_mode
    }

    companion object {
        enum class ListItemType {
            Text, Graphic;
        }
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
        initRecyclerView()
        main_swipe_refresh.setOnRefreshListener {
            presenter.sortedAnchorList.value?.let {
                if (it.isNotEmpty())
                    presenter.getAllAnchorsAttribute()
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

        layoutManagerType = when (type) {
            resources.getString(R.string.string_recycler_view) -> ListItemType.Text
            resources.getString(R.string.string_grid_view) -> ListItemType.Graphic
            else -> ListItemType.Text
        }
    }


    private fun initRecyclerView() {
        when (layoutManagerType) {
            ListItemType.Text -> {
                recycler_view.layoutManager = LinearLayoutManager(this)
                val adapter = TextAnchorAdapter(
                    this,
                    presenter.sortedAnchorList.value!!,
                    MODE_GROUP
                )
                recycler_view.adapter = adapter
                nowAnchorAnchorAdapter = adapter
            }

            ListItemType.Graphic -> {
                val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                recycler_view.layoutManager = manager
                val adapter = GraphicAnchorAdapter(
                    this,
                    presenter.sortedAnchorList.value!!,
                    MODE_GROUP
                )
                recycler_view.adapter = adapter
                nowAnchorAnchorAdapter = adapter
//                recycler_view.addItemDecoration(GroupTitleDecoration())
            }
        }
        recycler_view.addOnScrollListener(AnchorListAddTitleListener())

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
                presenter.anchorRepository.deleteAnchor(presenter.sortedAnchorList.value!![position])
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
            presenter.sortedAnchorList.value!!
        )
    }

    @NeedsPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    fun showPlayerOverlayWindow(anchor: Anchor) {
        PlayerOverlayWindowManager.instance.play(anchor)
    }

    @Suppress("UNUSED_PARAMETER")
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
