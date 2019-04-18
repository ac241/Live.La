package com.acel.livela.ui.main

import android.support.v4.app.FragmentManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import com.acel.livela.R
import com.acel.livela.base.BaseActivity
import com.acel.livela.bean.Anchor
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger


class MainActivity : BaseActivity(), MainConstract.View, AnkoLogger {
    lateinit var fragmentmanager: FragmentManager
    lateinit var recyclerView: RecyclerView
    lateinit var presenter: MainPresenter
    lateinit var adapter: MainAdapter

    override fun getResLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun init() {
        recyclerView = main_recycler_view
        presenter = MainPresenter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MainAdapter(this, presenter.anchorList, presenter.anchorStatusMap)
        recyclerView.adapter = adapter
        //关闭刷新item时CardViewd的闪烁提示
        recyclerView.itemAnimator?.changeDuration = 0
        main_swipe_refresh.setOnRefreshListener { presenter.getAnchorsStatus() }
        main_btn_add_anchor.setOnClickListener {
            showAddAnchorFragment()
        }
    }

    private fun showAddAnchorFragment() {
        fragmentmanager = supportFragmentManager
        fragmentmanager.beginTransaction().commit()
        val addAnchorFragment = AddAnchorFragment()
        addAnchorFragment.show(fragmentmanager, "add_anchor_fragment")
    }


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

    override fun refreshAnchorList() {
        recyclerView.adapter?.notifyDataSetChanged()
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
            R.id.main_item_delete -> {
                val position = adapter.getPosition()
                presenter.deleteAnchor(presenter.anchorList[position])
            }
        }

        return super.onContextItemSelected(item)
    }
}
