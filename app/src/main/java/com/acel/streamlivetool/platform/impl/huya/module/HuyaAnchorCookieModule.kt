package com.acel.streamlivetool.platform.impl.huya.module

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.AbstractAnchorCookieImpl
import com.acel.streamlivetool.platform.base.CookieManager
import com.acel.streamlivetool.platform.bean.ApiResult
import com.acel.streamlivetool.platform.impl.huya.HuyaImpl
import com.acel.streamlivetool.platform.impl.huya.bean.Subscribe
import com.acel.streamlivetool.ui.custom.AlertDialogTool
import com.acel.streamlivetool.util.AnchorUtil
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.CookieUtil
import com.acel.streamlivetool.util.TimeUtil
import kotlinx.coroutines.*
import java.util.*

class HuyaAnchorCookieModule(private val platform: String, cookieManager: CookieManager) :
        AbstractAnchorCookieImpl(cookieManager) {

    override fun getAnchorsByCookieMode(): ApiResult<List<Anchor>> {
        cookieManager.getCookie().let { cookie ->
            if (cookie.isEmpty())
                return ApiResult(false, "非法的cookie", cookieValid = false)
            else {
                val subscribe = getSubscribe(cookie)
                if (subscribe?.status != 1000L)
                    return ApiResult(false, subscribe?.message.toString(), cookieValid = false)
                else
                    return run {
                        val list = mutableListOf<Anchor>()
                        subscribe.result.list.forEach {
                            list.add(
                                    Anchor(
                                            platform = platform,
                                            nickname = it.nick,
                                            showId = it.profileRoom.toString(),
                                            roomId = it.uid.toString(),
                                            status = it.isLive,
                                            title = it.intro,
                                            avatar = it.avatar180,
                                            keyFrame = it.screenshot,
                                            typeName = it.gameName,
                                            online = AnchorUtil.formatOnlineNumber(it.totalCount.toInt()),
                                            liveTime = TimeUtil.timestampToString(it.startTime)
                                    )
                            )
                        }
                        ApiResult(true, data = list)
                    }
            }
        }
    }

    private fun getSubscribe(cookie: String): Subscribe? {
        val cs = cookie.split(";")
        var uid = ""
        cs.forEach {
            if (it.contains("yyuid")) {
                val yyuid = it.split("=")
                uid = yyuid[1]
            }
        }
        return HuyaImpl.huyaService.getSubscribe(cookie, uid).execute().body()
    }

    override val supportFollow: Boolean = true

    override fun follow(context: Context, anchor: Anchor): ApiResult<String> {
        cookieManager.getCookie().let { cookie ->
            if (cookie.isEmpty())
                return ApiResult(false, "未登录", cookieValid = false)
            val uid = CookieUtil.getCookieField(cookie, "yyuid")
            uid?.let { u ->
                val response =
                        HuyaImpl.huyaService.follow(
                                cookie,
                                anchor.roomId,
                                u,
                                System.currentTimeMillis()
                        )
                                .execute().body()
                response?.apply {
                    return if (status == 1)
                        ApiResult(true, "关注成功")
                    else {
                        val result = showVerifyCodeWindow(context, response.data.replace("{\"url\":\"", "")
                                .replace("\"}", ""))
                        if (result) {
                            follow(context, anchor)
                        } else {
                            ApiResult(
                                    false, "需要校验验证码",
                                    code = response.status,
                                    data = response.data.replace("{\"url\":\"", "")
                                            .replace("\"}", "")
                            )
                        }
                    }
                }
            }
        }
        return ApiResult(false, "发生错误")
    }


    @SuppressLint("SetJavaScriptEnabled")
    fun showVerifyCodeWindow(context: Context, url: String): Boolean {
        Thread.currentThread().suspend()
        val obj = Object()
        var result = false
        val builder = AlertDialogTool.newAlertDialog(context)
        builder.apply {
            setTitle("需要校验验证码")
            setMessage("请在验证完成后点击确认")
            setView(R.layout.verify_window)
            setPositiveButton("确认") { d, _ ->
                d.dismiss()
                result = true
                synchronized(obj) {
                    obj.notify()
                }
            }
            setNegativeButton("取消") { d, _ ->
                d.dismiss()
                result = false
                synchronized(obj) {
                    obj.notify()
                }
            }
        }
        AppUtil.mainThread {
            val cm: android.webkit.CookieManager =
                    android.webkit.CookieManager.getInstance()
            cm.setCookie(url, cookieManager.getCookie())
            val alertDialog = builder.show()
            alertDialog.findViewById<WebView>(R.id.webView)?.apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setSupportMultipleWindows(true)
                    //缩放
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    //自适应
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                    userAgentString =
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    android.webkit.CookieManager.getInstance()
                            .setAcceptThirdPartyCookies(this, true)
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
                loadUrl(url)
            }
        }
        synchronized(obj) {
            obj.wait()
        }
        return result
    }

}