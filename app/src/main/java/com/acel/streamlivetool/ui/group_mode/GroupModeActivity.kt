package com.acel.streamlivetool.ui.group_mode

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
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity

class GroupModeActivity : BaseActivity(), GroupModeConstract.View {
    private lateinit var fragmentmanager: FragmentManager
    lateinit var presenter: GroupModePresenter
    private lateinit var adapter: GroupModeAdapter

    override fun getResLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun init() {
        anchorList.observe(this, Observer {
            refreshAnchorList()
        })
        initToolbar()
        presenter = GroupModePresenter(this)
        main_recycler_view.layoutManager = LinearLayoutManager(this)
        adapter = GroupModeAdapter(
            this,
            anchorList,
            presenter.anchorStatusMap,
            presenter.anchorTitleMap
        )
        main_recycler_view.adapter = adapter
        //关闭刷新item时CardView的闪烁提示
        main_recycler_view.itemAnimator?.changeDuration = 0
        main_swipe_refresh.setOnRefreshListener {
            if (anchorList.value!!.size != 0)
                presenter.getAllAnchorsStatus()
            else
                hideSwipeRefreshBtn()
        }

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
        main_recycler_view.adapter?.notifyItemChanged(anchorList.value?.indexOf(anchor)!!)
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
        main_recycler_view.adapter?.notifyDataSetChanged()
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
                val position = adapter.getPosition()
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

    fun fabClick(view: View) {
        startActivity<CookieModeActivity>()
    }
}
