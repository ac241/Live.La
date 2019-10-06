package com.acel.streamlivetool.platform.huya.bean
import com.google.gson.annotations.SerializedName


data class Stream(
    @SerializedName("count")
    val count: Int,
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("iWebDefaultBitRate")
    val iWebDefaultBitRate: Int,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("status")
    val status: Int,
    @SerializedName("vMultiStreamInfo")
    val vMultiStreamInfo: List<VMultiStreamInfo>
)

data class VMultiStreamInfo(
    @SerializedName("iBitRate")
    val iBitRate: Int,
    @SerializedName("sDisplayName")
    val sDisplayName: String
)

data class Data(
    @SerializedName("gameLiveInfo")
    val gameLiveInfo: GameLiveInfo,
    @SerializedName("gameStreamInfoList")
    val gameStreamInfoList: List<GameStreamInfo>
)

data class GameStreamInfo(
    @SerializedName("iIsMaster")
    val iIsMaster: Int,
    @SerializedName("iIsMultiStream")
    val iIsMultiStream: Int,
    @SerializedName("iIsP2PSupport")
    val iIsP2PSupport: Int,
    @SerializedName("iLineIndex")
    val iLineIndex: Int,
    @SerializedName("iMobilePriorityRate")
    val iMobilePriorityRate: Int,
    @SerializedName("iPCPriorityRate")
    val iPCPriorityRate: Int,
    @SerializedName("iWebPriorityRate")
    val iWebPriorityRate: Int,
    @SerializedName("lChannelId")
    val lChannelId: Int,
    @SerializedName("lFreeFlag")
    val lFreeFlag: Int,
    @SerializedName("lPresenterUid")
    val lPresenterUid: Int,
    @SerializedName("lSubChannelId")
    val lSubChannelId: Long,
    @SerializedName("newCFlvAntiCode")
    val newCFlvAntiCode: String,
    @SerializedName("sCdnType")
    val sCdnType: String,
    @SerializedName("sFlvAntiCode")
    val sFlvAntiCode: String,
    @SerializedName("sFlvUrl")
    val sFlvUrl: String,
    @SerializedName("sFlvUrlSuffix")
    val sFlvUrlSuffix: String,
    @SerializedName("sHlsAntiCode")
    val sHlsAntiCode: String,
    @SerializedName("sHlsUrl")
    val sHlsUrl: String,
    @SerializedName("sHlsUrlSuffix")
    val sHlsUrlSuffix: String,
    @SerializedName("sP2pAntiCode")
    val sP2pAntiCode: String,
    @SerializedName("sP2pUrl")
    val sP2pUrl: String,
    @SerializedName("sP2pUrlSuffix")
    val sP2pUrlSuffix: String,
    @SerializedName("sStreamName")
    val sStreamName: String,
    @SerializedName("vFlvIPList")
    val vFlvIPList: List<Any>
)

data class GameLiveInfo(
    @SerializedName("activityCount")
    val activityCount: String,
    @SerializedName("activityId")
    val activityId: String,
    @SerializedName("attendeeCount")
    val attendeeCount: Any,
    @SerializedName("avatar180")
    val avatar180: String,
    @SerializedName("bitRate")
    val bitRate: String,
    @SerializedName("bussType")
    val bussType: String,
    @SerializedName("cameraOpen")
    val cameraOpen: String,
    @SerializedName("channel")
    val channel: String,
    @SerializedName("codecType")
    val codecType: String,
    @SerializedName("contentIntro")
    val contentIntro: String,
    @SerializedName("gameFullName")
    val gameFullName: String,
    @SerializedName("gameHostName")
    val gameHostName: String,
    @SerializedName("gameType")
    val gameType: Any,
    @SerializedName("gid")
    val gid: String,
    @SerializedName("introduction")
    val introduction: String,
    @SerializedName("isSecret")
    val isSecret: String,
    @SerializedName("level")
    val level: String,
    @SerializedName("liveChannel")
    val liveChannel: String,
    @SerializedName("liveCompatibleFlag")
    val liveCompatibleFlag: String,
    @SerializedName("liveId")
    val liveId: String,
    @SerializedName("liveSourceType")
    val liveSourceType: String,
    @SerializedName("multiStreamFlag")
    val multiStreamFlag: String,
    @SerializedName("nick")
    val nick: String,
    @SerializedName("privateHost")
    val privateHost: String,
    @SerializedName("profileHomeHost")
    val profileHomeHost: String,
    @SerializedName("profileRoom")
    val profileRoom: String,
    @SerializedName("recommendStatus")
    val recommendStatus: String,
    @SerializedName("recommendTagName")
    val recommendTagName: String,
    @SerializedName("roomName")
    val roomName: String,
    @SerializedName("screenType")
    val screenType: String,
    @SerializedName("screenshot")
    val screenshot: String,
    @SerializedName("sex")
    val sex: String,
    @SerializedName("shortChannel")
    val shortChannel: String,
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("totalCount")
    val totalCount: String,
    @SerializedName("uid")
    val uid: String,
    @SerializedName("yyid")
    val yyid: String
)