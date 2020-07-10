package com.acel.streamlivetool.ui.group_mode

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.acel.streamlivetool.MainAnchorHelper.anchorList
import com.acel.streamlivetool.MainAnchorHelper.deleteAnchor
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.cookie_mode.CookieModeActivity
import com.acel.streamlivetool.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_group_mode.*
import kotlinx.android.synthetic.main.layout_group_mode_grid_view.*
import kotlinx.android.synthetic.main.layout_group_mode_recycler_view.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.startActivity

class GroupModeActivity : BaseActivity(), GroupModeConstract.View {
    private lateinit var fragmentmanager: FragmentManager
    lateinit var presenter: GroupModePresenter
    private var listViewType = ListViewType.RecyclerView

    override fun getResLayoutId(): Int {
        return R.layout.activity_group_mode
    }

    enum class ListViewType {
        RecyclerView, GridView;
    }

    override fun init() {
        presenter = GroupModePresenter(this)
        anchorList.observe(this, Observer {
            Log.d("init", "observe data change")
            refreshAnchorList()
        })
        initToolbar()
        initPreference()

        when (listViewType) {
            ListViewType.RecyclerView -> initRecyclerView()
            ListViewType.GridView -> initGridView()
        }

        main_swipe_refresh.setOnRefreshListener {
            if (anchorList.value!!.size != 0)
                presenter.getAllAnchorsStatus()
            else
                hideSwipeRefreshBtn()
        }
    }

    private fun initPreference() {
        val type = defaultSharedPreferences.getString(
            resources.getString(R.string.pref_key_group_mode_list_type),
            "recycler_view"
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
            anchorList,
            presenter.anchorAttributeMap
        )
    }

    private fun initRecyclerView() {
        viewStub_group_mode_recycler_view.inflate()
        group_mode_recycler_view.layoutManager = LinearLayoutManager(this)
        val recyclerViewAdapter = GroupModeRecyclerViewAdapter(
            this,
            anchorList,
            presenter.anchorAttributeMap
        )
        group_mode_recycler_view.adapter = recyclerViewAdapter
        //关闭刷新item时CardView的闪烁提示
        group_mode_recycler_view.itemAnimator?.changeDuration = 0
    }

    private fun initToolbar() {
        setSupportActionBar(main_toolbar)
        supportActionBar?.setTitle(R.string.app_name)
    }

    private fun showAddAnchorFragment() {
        fragmentmanager = supportFragmentManager
        fragmentmanager.beginTransaction().commit()
        val addAnchorFragment = AddAnchorFragment()
        addAnchorFragment.show(fragmentmanager, "add_anchor_fragment")
    }


    @Synchronized
    override fun refreshAnchorStatus(anchor: Anchor) {
        when (listViewType) {
            ListViewType.RecyclerView ->
                group_mode_recycler_view.adapter?.notifyItemChanged(anchorList.value?.indexOf(anchor)!!)
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
        hideSwipeRefreshBtn()
    }

    override fun onDestroy() {
        super.onDestroy()
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
                deleteAnchor(anchorList.value!![position])
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_main_setting -> {
                startActivity<SettingsActivity>()
            }
            R.id.action_cookie_anchor -> {
                showAddAnchorFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("UNUSED_PARAMETER")
    fun fabClick(view: View) {
        startActivity<CookieModeActivity>()
    }
}
