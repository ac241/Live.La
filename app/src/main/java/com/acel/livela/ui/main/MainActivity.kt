package com.acel.livela.ui.main

import android.support.v4.app.FragmentManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.acel.livela.R
import com.acel.livela.ui.settings.SettingsActivity
import com.acel.livela.base.BaseActivity
import com.acel.livela.bean.Anchor
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.startActivity


class MainActivity : BaseActivity(), MainConstract.View, AnkoLogger {
    lateinit var fragmentmanager: FragmentManager
    lateinit var recyclerView: RecyclerView
    lateinit var presenter: MainPresenter
    lateinit var adapter: MainAdapter

    override fun getResLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun init() {
        initToolbar()
        recyclerView = main_recycler_view
        presenter = MainPresenter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MainAdapter(this, presenter.anchorList, presenter.anchorStatusMap)
        recyclerView.adapter = adapter
        //关闭刷新item时CardViewd的闪烁提示
        recyclerView.itemAnimator?.changeDuration = 0
        main_swipe_refresh.setOnRefreshListener { presenter.getAllAnchorsStatus() }
        main_btn_add_anchor.setOnClickListener {
            showAddAnchorFragment()
        }
    }

    private fun initToolbar() {
        setSupportActionBar(main_toolbar)
//        supportActionBar?.setTitle(R.string.app_name)
//        supportActionBar
    }

    private fun showAddAnchorFragment() {
        fragmentmanager = supportFragmentManager
        fragmentmanager.beginTransaction().commit()
        val addAnchorFragment = AddAnchorFragment()
        addAnchorFragment.show(fragmentmanager, "add_anchor_fragment")
    }


    @Synchronized
    override fun refreshAnchorStatus(anchor: Anchor) {
        recyclerView.adapter?.notifyItemChanged(presenter.anchorList.indexOf(anchor))
        hideSwipeRefreshBtn()
    }


    override fun addAnchorSuccess(anchor: Anchor) {
        val addAnchorFragment = fragmentmanager.findFragmentByTag("add_anchor_fragment") as AddAnchorFragment
        addAnchorFragment.onGetAnchorInfoSuccess(anchor)
    }

    override fun addAnchorFail(reason: String) {
        val addAnchorFragment = fragmentmanager.findFragmentByTag("add_anchor_fragment") as AddAnchorFragment
        addAnchorFragment.onGetAnchorInfoFailed(reason)
    }

    @Synchronized
    override fun refreshAnchorList() {
        Log.d("ACEL_LOG", "refreshAnchorList")
        recyclerView.adapter?.notifyDataSetChanged()
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_main_setting -> {
                startActivity<SettingsActivity>()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
