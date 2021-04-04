package com.acel.streamlivetool.platform.impl.huya.danmu

import android.util.Log
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.Danmu
import com.acel.streamlivetool.net.WebSocketClient
import com.acel.streamlivetool.platform.base.CookieManager
import com.acel.streamlivetool.platform.base.ReusableDanmuClient
import com.acel.streamlivetool.platform.impl.huya.bean.WebSocketData
import com.acel.streamlivetool.ui.main.player.DanmuManager
import com.acel.streamlivetool.util.defaultSharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class HuyaDanmuClient(
        cookieManager: CookieManager,
        danmuManager: DanmuManager,
        anchor: Anchor
) :
        ReusableDanmuClient(cookieManager, danmuManager, anchor) {

    private var webSocket: WebSocket? = null
    private val gson = Gson()
    private var heartbeatJob: Job? = null

    override fun start(anchor: Anchor, danmuManager: DanmuManager) {
        super.start(anchor, danmuManager)
        val cookie = cookieManager?.getCookie()
        cookie ?: throw IllegalArgumentException("cookie manager null?")
        val appId = defaultSharedPreferences.getString(
                MyApplication.application.getString(R.string.key_huya_danmu_app_id),
                ""
        )
        val secret = defaultSharedPreferences.getString(
                MyApplication.application.getString(R.string.key_huya_danmu_secret),
                ""
        )
        if (appId.isNullOrEmpty() || secret.isNullOrEmpty()) {
            danmuManager.errorCallback(
                    "需要先在[设置-虎牙弹幕数据]填写数据",
                    this,
                    anchor,
                    DanmuManager.ErrorType.SPECIAL
            )
            return
        }

        val wsLink = HuyaWSHelper.getWSUrl(anchor.showId, appId, secret)
        val request = Request.Builder().url(wsLink).build()
        webSocket = WebSocketClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)

                heartbeatJob = GlobalScope.launch(Dispatchers.IO) {
                    runCatching {
                        while (true) {
                            webSocket.send(HuyaWSHelper.heartBeat)
                            delay(15000)
                        }
                    }.onFailure {
                        Log.d(
                                "acel_log@heartBeat",
                                "${anchor.platform} ${anchor.nickname}的弹幕心跳包被取消"
                        )
                        it.printStackTrace()
                    }
                }
                danmuManager.startCallback()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                t.printStackTrace()
                if (isRunning) {
                    danmuManager.errorCallback("error", this@HuyaDanmuClient, anchor)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                danmuManager.stopCallBack(reason)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                handleMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                Log.d(
                        "HuyaDanmuReceiver",
                        "5${bytes.toByteArray().decodeToString()}"
                )
            }
        })
    }

    override fun stop() {
        super.stop()
        webSocket?.close(1000, null)
        webSocket = null
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun handleMessage(text: String) {
        try {
            val data = gson.fromJson(text, WebSocketData::class.java)
            data.data.apply {
                danmuManager?.newDanmuCallback(
                        Danmu(
                                content,
                                roomId.toString(),
                                sendNick,
                                null
                        )
                )
            }
        } catch (e: Exception) {
        }
    }

}