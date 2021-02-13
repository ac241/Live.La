package com.acel.streamlivetool.platform.douyu

import android.util.Log
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.Danmu
import com.acel.streamlivetool.net.WebSocketClient
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.ui.player.DanmuClient
import kotlinx.coroutines.*
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.regex.Matcher

class DouyuDanmuManager : IPlatform.DanmuManager() {

    override fun generateReceiver(
        cookie: String,
        anchor: Anchor,
        danmuClient: DanmuClient
    ): DanmuReceiver = DouyuDanmuReceiver(anchor, cookie, danmuClient)

    class DouyuDanmuReceiver(val anchor: Anchor, val cookie: String, danmuClient: DanmuClient) :
        DanmuReceiver {

        var roomid = anchor.roomId
        private var webSocket: WebSocket? = null
        private var heartbeatJob: Job? = null
        private var danmuClient: DanmuClient? = danmuClient

        inner class DouyuWebsocketListener : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                val result = messageDecoder(bytes.toByteArray())
                result?.forEach { msg ->
                    val matcher = DanmuPatternUtils.readType.matcher(msg)
                    if (matcher.find()) {
                        when (matcher.group(1)) {
                            "chatmsg" -> {
                                val danmu = handleDanmu(msg)
                                danmu?.let { danmuClient?.newDanmuCallback(it) }
                            }
                        }
                    }
                }
            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                login()
                joinGroup()
                receiveAllMsg()
                heartBeat()
            }

            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            private fun handleDanmu(it: String): Danmu? {
                val mMsg: Matcher = DanmuPatternUtils.readDanmuInfo.matcher(it)
                val mUid: Matcher = DanmuPatternUtils.readDanmuUid.matcher(it)
                val mNickname: Matcher = DanmuPatternUtils.readDanmuUser.matcher(it)
                val mSendTime: Matcher =
                    DanmuPatternUtils.readDanmuSendTime.matcher(it)
                if (!(mMsg.find() && mUid.find() && mNickname.find() && mSendTime.find()))
                    return null
                return Danmu(
                    mMsg.group(1),
                    mUid.group(1),
                    mNickname.group(1),
                    mSendTime.group(1)
                )
            }
        }

        override fun start() {
            val request =
                Request.Builder().get().url("wss://danmuproxy.douyu.com:8504")
                    .header("Cookie", cookie).build()
            webSocket =
                WebSocketClient.newWebSocket(request, DouyuWebsocketListener())
        }

        override fun stop() {
            webSocket?.close(1000, "")
            webSocket = null
            danmuClient = null
            heartbeatJob?.cancel()
            heartbeatJob = null
        }


        /**
         * 发送登录包
         */
        private fun login() {
            val bytes = messageEncoder("type@=loginreq/roomid@=$roomid/")
            if (bytes != null)
                webSocket?.send(ByteString.of(bytes, 0, bytes.size))
        }

        /**
         * 发送进入分组包
         */
        private fun joinGroup() {
            val bytes = messageEncoder("type@=joingroup/rid@=${roomid}/gid@=-9999/")
            if (bytes != null)
                webSocket?.send(ByteString.of(bytes, 0, bytes.size))
        }

        /**
         * 发送进入分组包
         */
        private fun receiveAllMsg() {
            val bytes = messageEncoder(
                "type@=dmfbdreq/dfl@=sn@AA=105@ASss@AA=0@AS@Ssn@AA=106@ASss@AA=0@AS@Ssn@AA=107@ASss@AA=0@AS@Ssn@AA=108@ASss@AA=0@AS@Ssn@AA=110@ASss@AA=0@AS@S/"
            )
            if (bytes != null)
                webSocket?.send(ByteString.of(bytes, 0, bytes.size))
        }


        /**
         * 循环心跳包
         */
        private fun heartBeat() {
            heartbeatJob = GlobalScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    while (true) {
                        delay(45000)
                        val bytes = messageEncoder("type@=mrkl/")
                        if (bytes != null)
                            webSocket?.send(ByteString.of(bytes, 0, bytes.size))
                    }
                }.onFailure {
                    Log.d("acel_log@start", "${anchor.nickname}的弹幕心跳包被取消")
                    it.printStackTrace()
                }
            }

        }

        companion object {

            /**
             * @author https://github.com/duzouw/WebSocketClient
             * 斗鱼发送消息
             * 消息类型 689 客户端发送给斗鱼服务器
             * 斗鱼消息 = 消息头部
             * (
             * 消息总长度 [ 小端模式转换 4 ( 这个不算总长度中 )]
             * 消息总长度 [ 小端模式转换 4 ]
             * 消息类型 [ 小端模式转换 4 ]
             * )
             * + 消息内容
             * + 结束符号 (0)
             * @param message  发送的消息内容
             */
            private fun messageEncoder(message: String): ByteArray? {
                val len = 4 + 4 + 1 + message.length
                val bytes = ByteArray(4 + len)
                DanmuTool.arrayJoinByte(
                    bytes,
                    DanmuTool.intToBytesLittle(len),
                    DanmuTool.intToBytesLittle(len),
                    DanmuTool.intToBytesLittle(689),
                    message.toByteArray(), byteArrayOf(0)
                )
                return bytes
            }

            /**
             * @author https://github.com/duzouw/WebSocketClient
             * 斗鱼接收消息
             * 消息类型 690 斗鱼服务器推送给客户端的类型
             * 斗鱼消息 = 消息头部
             * (
             * 消息总长度 [ 小端模式转换 4 ( 这个不算总长度中 )]
             * 消息总长度 [ 小端模式转换 4 ]
             * 消息类型 [ 小端模式转换 4 ]
             * )
             * + 消息内容
             */
            private fun messageDecoder(data: ByteArray): List<String>? {
                var index = 0
                val result: MutableList<String> = ArrayList()
                while (true) {
                    if (data.size - index >= 4) {
                        // 消息总长度
                        val lenBytes1 =
                            byteArrayOf(data[index++], data[index++], data[index++], data[index++])
                        val len1: Int = DanmuTool.bytesToIntLittle(lenBytes1)
                        if (len1 > 8 && data.size >= index + len1) {

                            // 消息总长度
                            val lenBytes2 = byteArrayOf(
                                data[index++], data[index++], data[index++],
                                data[index++]
                            )
                            val len2: Int = DanmuTool.bytesToIntLittle(lenBytes2)

                            // 消息类型
                            val typeBytes = byteArrayOf(
                                data[index++], data[index++], data[index++],
                                data[index++]
                            )
                            val type: Int = DanmuTool.bytesToIntLittle(typeBytes)
                            val contentBytes = ByteArray(len1 + 4)
                            DanmuTool.arrayJoinByte(
                                contentBytes,
                                lenBytes1,
                                lenBytes2,
                                typeBytes
                            )
                            val aIndex = index
                            while (index < aIndex + len1 - 8) {
                                contentBytes[index - aIndex + 12] = data[index]
                                index++
                            }
                            // 核对一下数据
                            if (len1 == len2 && type == 690) {
                                result.add(String(contentBytes).substring(12))
                            }
                        }
                    } else break
                }
                return if (result.size != 0) result else null
            }
        }
    }
}