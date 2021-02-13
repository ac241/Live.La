package com.acel.streamlivetool.platform.bilibili

import android.util.Log
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.Danmu
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.net.WebSocketClient
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.ui.player.DanmuClient
import com.acel.streamlivetool.util.CookieUtil
import com.acel.streamlivetool.util.ZlibUtil
import kotlinx.coroutines.*
import okhttp3.*
import okio.ByteString
import java.nio.ByteBuffer
import java.util.regex.Matcher

class BilibiliDanmuManager :
    IPlatform.DanmuManager() {

    override val danmuAssertCookie: Boolean
        get() = true

    override fun generateReceiver(
        cookie: String,
        anchor: Anchor,
        danmuClient: DanmuClient
    ): DanmuReceiver = BilibiliDanmuReceiver(anchor, cookie, danmuClient)

    class BilibiliDanmuReceiver(
        val anchor: Anchor,
        val cookie: String,
        danmuClient: DanmuClient
    ) :
        DanmuReceiver {
        private var danmuClient: DanmuClient? = danmuClient

        private val bilibiliService: BilibiliApi =
            RetrofitUtils.retrofit.create(BilibiliApi::class.java)
        private var webSocket: WebSocket? = null
        private var heartbeatJob: Job? = null
        private var websocketListener: BilibiliWebsocketListener = BilibiliWebsocketListener()
        private var uid: String? = null
        private var token: String? = null

        override fun start() {
            val info = bilibiliService.getDanmuInfo(cookie, anchor.roomId).execute().body()
            if (info != null) {
                uid = CookieUtil.getCookieField(cookie, "DedeUserID")
                token = info.data.token
                val host = info.data.host_list[0]
                val wsUrl = "wss://${host.host}:${host.wss_port}/sub"
                val request =
                    Request.Builder().get().url(wsUrl).headers(
                        Headers.of(
                            "Accept-Encoding",
                            " gzip, deflate, br",
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Safari/537.36",
                            "Accept-Language",
                            "zh-CN,zh;q=0.9",
                            "Host",
                            " ${host.host}",
                            "Upgrade",
                            "websocket",
                            "Origin",
                            "https://live.bilibili.com",
                            "Referer",
                            "https://live.bilibili.com/",
                            "Sec-WebSocket-Extensions",
                            "permessage-deflate; client_max_window_bits",
                            "Sec-WebSocket-Key",
                            "xU+WLiKvC2xpGIbANhvzFg==",
                            "Sec-WebSocket-Version",
                            "13",
                            "Connection",
                            "Upgrade",
                        )
                    )
                        .header("Cookie", cookie)
                        .build()
                webSocket =
                    WebSocketClient.newWebSocket(request, websocketListener)
                joinRoom()

                //循环心跳包
                heartbeatJob = GlobalScope.launch(Dispatchers.IO) {
                    kotlin.runCatching {
                        while (true) {
                            webSocket?.send(
                                ByteString.of(heartbeatHeadPack, 0, heartbeatHeadPack.size)
                            )
                            delay(30000)
                        }
                    }.onFailure {
                        Log.d(
                            "acel_log@heartBeat",
                            "${anchor.platform} ${anchor.nickname}的弹幕心跳包被取消"
                        )
                        it.printStackTrace()
                    }
                }
            }
        }

        override fun stop() {
            webSocket?.close(1000, null)
            webSocket = null
            danmuClient = null
            heartbeatJob?.cancel()
            heartbeatJob = null
        }

        /**
         * 发送进入房间包
         */
        private fun joinRoom() {
            val msg =
                "{\"uid\":$uid,\"roomid\":${anchor.roomId},\"protover\":1,\"platform\":\"web\",\"clientver\":\"2.6.25\",\"type\":2,\"key\":\"$token\"}"
            val msgByteS = msg.encodeToByteArray()
            val length = 16 + msgByteS.size
            val array =
                ByteBuffer.allocate(length).putInt(length).put(joinHeadHeadPack)
                    .put(msg.encodeToByteArray()).array()
            webSocket?.send(ByteString.of(array, 0, array.size))
        }

        object DanmuDispatcher {
            fun dispatch(danmuClient: DanmuClient, string: String) {
                Log.d("acel_log@dispatch", "dispach string wait to do ")
            }

            fun dispatch(danmuClient: DanmuClient, bs: ByteString) {
                analyze(danmuClient, bs.toByteArray())
            }

            private fun analyze(danmuClient: DanmuClient, sourceArray: ByteArray) {
                //API说明参照 https://github.com/lovelyyoshino/Bilibili-Live-API/blob/master/API.WebSocket.md
                //代码来源 https://github.com/DbgDebug/dbg-project/
                var lengthSum = 0
                while (lengthSum < sourceArray.size) {
                    val headerByte = ByteArray(4)
                    System.arraycopy(sourceArray, lengthSum, headerByte, 0, headerByte.size)
                    val byteBuffer = ByteBuffer.wrap(headerByte)
                    val length = byteBuffer.int
                    // 头大小
                    byteBuffer.short
                    val protocolVersion = byteBuffer.short.toInt()
                    val operation = byteBuffer.int
                    // int sequence = byteBuffer.getInt();
                    val contentBytes = ByteArray(length - 16)
                    System.arraycopy(
                        sourceArray,
                        lengthSum + 16,
                        contentBytes,
                        0,
                        contentBytes.size
                    )
                    when (operation) {
                        //Int 32 Big Endian	心跳回应 Body 内容为房间人气值
                        3 -> {
                        }
                        //通知	弹幕、广播等全部信息
                        5 -> when (protocolVersion) {
                            //JSON JSON纯文本
                            0 -> {
                                handleDanmuJson(contentBytes, danmuClient)
                            }
                            //Int 32 Big Endian	Body 内容为房间人气值
                            1 -> {

                            }
                            //zlib压缩过的 Buffer
                            2 -> {
                                ZlibUtil.decompress(contentBytes)?.let { analyze(danmuClient, it) }
                            }
                        }
                        //进房回应
                        8 -> {
                        }
                    }
                    lengthSum += length
                }
            }

            private fun handleDanmuJson(contentBytes: ByteArray, danmuClient: DanmuClient) {
                val msg = String(contentBytes, Charsets.UTF_8)
                var msgType: String? = ""
                val mCmd: Matcher = DanmuPatternUtils.readCmd.matcher(msg)
                if (mCmd.find()) {
                    msgType = mCmd.group(1)
                }
                if (msgType != null) {
                    when (msgType) {
                        "DANMU_MSG" -> {
                            val danmu = MessageHandleService.handleDanmu(msg)
                            danmu?.let { danmuClient.newDanmuCallback(it) }
                        }
                        // TODO: 2021/2/12 其他类型
                        //https://github.com/DbgDebug/dbg-project/blob/1c0faaaf577d04a02d50293337339900f0107cb9/dbg-service-admin/src/main/java/club/dbg/cms/admin/service/bilibili/DanmuReceiveThread.java#L171
                    }
                } else {
                    Log.d("handleDanmuJson", "msg type null")
                }
            }
        }

        object MessageHandleService {
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            fun handleDanmu(msg: String): Danmu? {
                try {
                    val mMsg: Matcher = DanmuPatternUtils.readDanmuInfo.matcher(msg)
                    val mUid: Matcher = DanmuPatternUtils.readDanmuUid.matcher(msg)
                    val mNickname: Matcher = DanmuPatternUtils.readDanmuUser.matcher(msg)
                    val mSendTime: Matcher = DanmuPatternUtils.readDanmuSendTime.matcher(msg)
                    if (!(mMsg.find() && mUid.find() && mNickname.find() && mSendTime.find()))
                        return null
                    return Danmu(
                        mMsg.group(1),
                        mUid.group(1),
                        mNickname.group(1),
                        mSendTime.group(1)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }
        }

        companion object Const {
            val joinHeadHeadPack = byteArrayOf(
                0x00,
                0x10,
                0x00,
                0x01,
                0x00,
                0x00,
                0x00,
                0x07,
                0x00,
                0x00,
                0x00,
                0x01
            )

            val heartbeatHeadPack = byteArrayOf(
                0x00,
                0x00,
                0x00,
                0x10,
                0x00,
                0x10,
                0x00,
                0x01,
                0x00,
                0x00,
                0x00,
                0x02,
                0x00,
                0x00,
                0x00,
                0x01
            )
        }

        inner class BilibiliWebsocketListener : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                danmuClient?.startCallback()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                danmuClient?.let { DanmuDispatcher.dispatch(it, text) }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                danmuClient?.let { DanmuDispatcher.dispatch(it, bytes) }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                danmuClient?.stopCallBack(reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                t.printStackTrace()
                danmuClient?.errorCallback("发生错误")
            }
        }
    }

}