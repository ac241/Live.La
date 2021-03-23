/*
 * Copyright (c) 2020.
 * @author acel
 */

@file:Suppress("UNCHECKED_CAST")

package com.acel.streamlivetool.anchor_extension.action

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.res.ResourcesCompat
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.custom.AlertDialogTool
import com.acel.streamlivetool.util.AppUtil.mainThread
import kotlinx.android.synthetic.main.alert_browser_page.*

class GetLOLMatchExtension :
    AnchorExtensionInterface {
    companion object {
        val instance by lazy {
            GetLOLMatchExtension()
        }
    }

    override val iconResourceId: Int = R.drawable.ic_lpl_schedule
    override val iconDrawable: Drawable =
        ResourcesCompat.getDrawable(MyApplication.application.resources, iconResourceId, null)
            ?: error("image error")

    override val actionName: String
        get() = "英雄联盟本周赛程"

    val anchorList = listOf(
        Anchor("douyu", "英雄联盟赛事", "288016", "288016"),
        Anchor("douyu", "英雄联盟赛事", "664810", "664810"),
        Anchor("douyu", "英雄联盟赛事", "522424", "522424"),
        Anchor("huya", "英雄联盟赛事", "660000", "1346609715"),
        Anchor("bilibili", "哔哩哔哩英雄联盟赛事", "7734200", "7734200"),
        Anchor("egameqq", "LPL夏季赛主舞台", "58049", "367958257"),
        Anchor("douyu", "解说米勒", "5067522", "5067522")
    )

    override fun match(anchor: Anchor) = anchorList.contains(anchor)

    override fun doAction(context: Context, anchor: Anchor) {
        showWanplusPage(context)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showWanplusPage(context: Context) {
        val builder = AlertDialogTool.newAlertDialog(context)
        builder.setView(R.layout.alert_browser_page)
        mainThread {
            val dialog = builder.show()
            dialog.alert_webView.apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        loadUrl("javascript:\$(\".app-download\").remove()")
                        loadUrl("javascript:\$(\".filter\").click()")
                        loadUrl("javascript:\$(\".slide-list li:contains('LPL')\")[0].click()\n")
                        loadUrl("javascript:\$(\".tip-list button\").click()")
                    }
                }
                loadUrl("https://m.wanplus.com/schedule")
            }
        }
    }
}