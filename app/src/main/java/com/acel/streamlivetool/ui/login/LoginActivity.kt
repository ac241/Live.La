/*
 * Copyright (c) 2020.
 * @author acel
 * 登录
 */

package com.acel.streamlivetool.ui.login

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.acel.streamlivetool.R
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.ToastUtil.toast
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    val cookieManager: CookieManager = CookieManager.getInstance()

    @Suppress("DEPRECATION")
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(android.R.color.background_light)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        val platform = intent.getStringExtra("platform") ?: return
        val platformImpl = PlatformDispatcher.getPlatformImpl(platform) ?: return
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val cookieStr = cookieManager.getCookie(url)
                if (cookieStr != null) {
                    Log.d("onPageFinished", cookieStr.toString())
                    if (platformImpl.checkLoginOk(cookieStr)) {
                        platformImpl.saveCookie(cookieStr)
                        toast("添加成功")
                        finish()
                    }
                }
            }
        }
        webView.webChromeClient = object : WebChromeClient() {

        }
        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                if (platformImpl.usePcAgent()) {
                    setSupportMultipleWindows(true)
                    //缩放
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    //自适应
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                        layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                    userAgentString =
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"
                }
            }
        }
        webView.loadUrl(platformImpl.getLoginUrl())
    }

    @Suppress("DEPRECATION")
    override fun onDestroy() {
        super.onDestroy()
        webView.clearCache(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null)
        } else
            cookieManager.removeAllCookie()
    }
}