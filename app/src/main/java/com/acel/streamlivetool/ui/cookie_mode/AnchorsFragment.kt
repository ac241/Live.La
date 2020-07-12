package com.acel.streamlivetool.ui.cookie_mode

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.acel.streamlivetool.MainExecutor
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.AnchorsCookieMode
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.ui.login.LoginActivity
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.ToastUtil.toast
import kotlinx.android.synthetic.main.fragment_cookie_anchors.*
import kotlinx.android.synthetic.main.layout_login_first.*

class AnchorsFragment(val platform: IPlatform) : Fragment() {

    private var addCookie: Boolean = false
    private val anchors = mutableListOf<AnchorsCookieMode.Anchor>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cookie_anchors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cookie_anchor_recyclerView.layoutManager = LinearLayoutManager(context)
        cookie_anchor_recyclerView.adapter =
            CookieModeAdapter(
                activity as CookieModeActivity,
                anchors
            )
        cookie_anchor_swipe_refresh.setOnRefreshListener {
            getAnchors()
        }
        getAnchors()
    }

    private fun getAnchors() {
        MainExecutor.execute {
            val anchorsCookieMode = platform.getAnchorsWithCookieMode()
            if (!anchorsCookieMode.cookieOk) {
                if (viewStub_login_first != null)
                    runOnUiThread {
                        viewStub_login_first.inflate()
                        textView_login_first.setOnClickListener {
                            val intent = Intent(context, LoginActivity::class.java).also {
                                it.putExtra("platform", platform.platform)
                            }
                            startActivity(intent)
                            addCookie = true
                        }
                    }
            } else {
                with(anchorsCookieMode.anchors) {
                    if (this != null) {
                        anchors.clear()
                        anchors.addAll(this)
                        runOnUiThread {
                            cookie_anchor_recyclerView.adapter?.notifyDataSetChanged()
                        }
                    }
                }

                if (addCookie) {
                    runOnUiThread {
                        login_first_wrapper.visibility = View.GONE
                    }
                }
            }
            runOnUiThread {
                cookie_anchor_swipe_refresh.isRefreshing = false
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (addCookie) {
            getAnchors()
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (isVisible)
            when (item.itemId) {
                R.id.action_item_add_to_main_mode -> {
                    val adapter = cookie_anchor_recyclerView.adapter as CookieModeAdapter
                    val position = adapter.getPosition()
                    val result = AnchorRepository.getInstance(requireContext().applicationContext)
                        .insertAnchor(anchors[position])
                    toast(result.second)
                }
            }
        return super.onContextItemSelected(item)
    }

}