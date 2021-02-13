package com.acel.streamlivetool.net

import com.acel.streamlivetool.net.RetrofitUtils.Companion.okHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

object WebSocketClient {
    fun newWebSocket(request: Request,webSocketListener: WebSocketListener): WebSocket {
        return okHttpClient.newWebSocket(request, webSocketListener)
    }
}