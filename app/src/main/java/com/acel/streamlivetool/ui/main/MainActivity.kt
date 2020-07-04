package com.acel.streamlivetool.ui.main

import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.cookie_anchor.CookieAnchorActivity
import com.acel.streamlivetool.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity

class MainActivity : BaseActivity(), MainConstract.View {
    private lateinit var fragmentmanager: FragmentManager
    lateinit var presenter: MainPresenter
    private lateinit var adapter: MainAdapter

    override fun getResLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun init() {
        initToolbar()
        presenter = MainPresenter(this)
        main_recycler_view.layoutManager = LinearLayoutManager(this)
        adapter = MainAdapter(
            this,
            presenter.anchorList,
            presenter.anchorStatusMap,
            presenter.anchorTitleMap
        )
        main_recycler_view.adapter = adapter
        //关闭刷新item时CardViewd的闪烁提示
        main_recycler_view.itemAnimator?.changeDuration = 0
        main_swipe_refresh.setOnRefreshListener { presenter.getAllAnchorsStatus() }
        main_btn_add_anchor.setOnClickListener {
            showAddAnchorFragment()
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
        main_recycler_view.adapter?.notifyItemChanged(presenter.anchorList.indexOf(anchor))
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
            R.id.action_main_item_delete -> {
                val position = adapter.getPosition()
                presenter.deleteAnchor(presenter.anchorList[position])
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
                startActivity<CookieAnchorActivity>()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
