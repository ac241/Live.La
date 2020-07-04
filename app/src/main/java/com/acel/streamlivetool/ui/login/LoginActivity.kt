package com.acel.streamlivetool.ui.login

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.acel.streamlivetool.R
import com.acel.streamlivetool.platform.PlatformDispatcher
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity() {
    val cookieManager: CookieManager = CookieManager.getInstance()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val platform = intent.getStringExtra("platform") ?: return
        val platformImpl = PlatformDispatcher.getPlatformImpl(platform) ?: return
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val cookieStr = cookieManager.getCookie(url)
                Log.d("onPageFinished", cookieStr.toString())
                if (platformImpl.checkLoginOk(cookieStr)) {
                    platformImpl.saveCookie(cookieStr)
                    toast("添加成功")
                    finish()
                }
            }
        }
        webView.webChromeClient = object : WebChromeClient() {

        }
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.setSupportZoom(true)
        // 设置出现缩放工具
        webView.settings.builtInZoomControls = true
        webView.setInitialScale(25)
        //扩大比例的缩放
        webView.settings.useWideViewPort = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        }
        webView.settings.loadWithOverviewMode = true
        if (platformImpl.usePcAgent())
            webView.settings.userAgentString =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"

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