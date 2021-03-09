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
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.custom.AlertDialogTool
import com.acel.streamlivetool.util.ToastUtil.toast
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity() {
    val cookieManager: CookieManager = CookieManager.getInstance()
    var platformImpl: IPlatform? = null

    @Suppress("DEPRECATION")
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ViewCompat.getWindowInsetsController(window.decorView)?.isAppearanceLightStatusBars =
            !isNightMode()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.background_light, null)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }


        val platform = intent.getStringExtra("platform") ?: return
        platformImpl = PlatformDispatcher.getPlatformImpl(platform)
        if (platformImpl == null) {
            toast("意外的平台，终止继续")
        }
        platformImpl?.let { impl ->
            if (impl.loginTips.isNotEmpty())
                tips.apply {
                    text = impl.loginTips
                    visibility = View.VISIBLE
                }
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    val cookieStr = cookieManager.getCookie(url)
                    if (cookieStr != null) {
                        Log.d("onPageFinished", cookieStr.toString())
                        if (impl.checkLoginOk(cookieStr)) {
                            impl.saveCookie(cookieStr)
                            toast("添加成功")
                            finish()
                        } else {
                            webView.loadUrl("javascript:$('#nav-login').click()")
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
                    if (impl.loginWithPcAgent()) {
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
            webView.loadUrl(impl.getLoginUrl())
        }
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

    @Suppress("UNUSED_PARAMETER")
    fun setCookieManual(view: View) {
        val alertDialog = AlertDialogTool.newAlertDialog(this).setTitle("手动设置Cookie")
            .setView(R.layout.alert_set_cookie_manual)
            .show()
        alertDialog.findViewById<Button>(R.id.commit)?.setOnClickListener {
            val cookie = alertDialog.findViewById<EditText>(R.id.edit_cookie)?.text.toString()
            if (cookie.isNotEmpty()) {
                platformImpl?.saveCookie(cookie)
                toast("保存成功")
                alertDialog.dismiss()
                finish()
            } else
                toast("请输入cookie")
        }

    }


}