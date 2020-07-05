package com.acel.streamlivetool.ui.cookie_anchor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.acel.streamlivetool.MainExecutor
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.AnchorsCookieMode
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.ui.login.LoginActivity
import kotlinx.android.synthetic.main.fragment_cookie_anchors.*
import kotlinx.android.synthetic.main.layout_login_first.*
import org.jetbrains.anko.support.v4.runOnUiThread

class AnchorsFragment(val platform: IPlatform) : Fragment() {

    private var addCookie: Boolean = false
    private val anchors = mutableListOf<AnchorsCookieMode.Anchor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
            CookieAnchorAdapter(
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
                        Log.d("getAnchors", "${anchors.size}")
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
}