package com.acel.streamlivetool.platform.huya

import android.util.Log
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.net.WebSocketClient
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.ui.player.DanmuClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.nio.ByteBuffer

class HuyaDanmuManager : IPlatform.DanmuManager() {

    override fun generateReceiver(
        cookie: String,
        anchor: Anchor,
        danmuClient: DanmuClient
    ): DanmuReceiver? {
        return HuyaDanmuReceiver()
    }

    class HuyaDanmuReceiver : DanmuReceiver {
        var websocket: WebSocket? = null

        override fun start() {
            val request = Request.Builder().url("wss://cdnws.api.huya.com").build()
            websocket = WebSocketClient.newWebSocket(request, object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    super.onMessage(webSocket, bytes)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                }

                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    val buffer = ByteBuffer.allocate(10)
//                    websocket?.send(ByteString.of(arrOutput, 0, arrOutput.size))
                    // TODO: 2021/2/20 wup TARS jce
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                }
            })
        }

        override fun stop() {

        }
    }

    companion object {
        var heartBeatPack = byteArrayOf(
            0x00.toByte(),
            0x03.toByte(),
            0x1D.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x69.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x69.toByte(),
            0x10.toByte(),
            0x03.toByte(),
            0x2C.toByte(),
            0x3C.toByte(),
            0x4C.toByte(),
            0x56.toByte(),
            0x08.toByte(),
            0x6F.toByte(),
            0x6E.toByte(),
            0x6C.toByte(),
            0x69.toByte(),
            0x6E.toByte(),
            0x65.toByte(),
            0x75.toByte(),
            0x69.toByte(),
            0x66.toByte(),
            0x0F.toByte(),
            0x4F.toByte(),
            0x6E.toByte(),
            0x55.toByte(),
            0x73.toByte(),
            0x65.toByte(),
            0x72.toByte(),
            0x48.toByte(),
            0x65.toByte(),
            0x61.toByte(),
            0x72.toByte(),
            0x74.toByte(),
            0x42.toByte(),
            0x65.toByte(),
            0x61.toByte(),
            0x74.toByte(),
            0x7D.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x3C.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x01.toByte(),
            0x06.toByte(),
            0x04.toByte(),
            0x74.toByte(),
            0x52.toByte(),
            0x65.toByte(),
            0x71.toByte(),
            0x1D.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x2F.toByte(),
            0x0A.toByte(),
            0x0A.toByte(),
            0x0C.toByte(),
            0x16.toByte(),
            0x00.toByte(),
            0x26.toByte(),
            0x00.toByte(),
            0x36.toByte(),
            0x07.toByte(),
            0x61.toByte(),
            0x64.toByte(),
            0x72.toByte(),
            0x5F.toByte(),
            0x77.toByte(),
            0x61.toByte(),
            0x70.toByte(),
            0x46.toByte(),
            0x00.toByte(),
            0x0B.toByte(),
            0x12.toByte(),
            0x64.toByte(),
            0xD3.toByte(),
            0xA5.toByte(),
            0xD0.toByte(),
            0x22.toByte(),
            0x64.toByte(),
            0xD3.toByte(),
            0xA5.toByte(),
            0xD0.toByte(),
            0x3C.toByte(),
            0x42.toByte(),
            0x64.toByte(),
            0xD3.toByte(),
            0xA5.toByte(),
            0xD0.toByte(),
            0x5C.toByte(),
            0x60.toByte(),
            0x01.toByte(),
            0x7C.toByte(),
            0x82.toByte(),
            0x00.toByte(),
            0x3F.toByte(),
            0x03.toByte(),
            0xAB.toByte(),
            0x9C.toByte(),
            0xAC.toByte(),
            0x0B.toByte(),
            0x8C.toByte(),
            0x98.toByte(),
            0x0C.toByte(),
            0xA8.toByte(),
            0x0C.toByte()
        )
    }
}