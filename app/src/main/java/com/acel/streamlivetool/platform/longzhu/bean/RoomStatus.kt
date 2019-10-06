package com.acel.streamlivetool.platform.longzhu.bean
import com.google.gson.annotations.SerializedName


data class RoomStatus(
    @SerializedName("Broadcast")
    val broadcast: Broadcast,
    @SerializedName("Closed")
    val closed: Boolean,
    @SerializedName("ContributionValue")
    val contributionValue: Int,
    @SerializedName("FlowerCount")
    val flowerCount: Int,
    @SerializedName("FollowUserSound")
    val followUserSound: Int,
    @SerializedName("GiftCost")
    val giftCost: Double,
    @SerializedName("GiftSound")
    val giftSound: Int,
    @SerializedName("HasMatch")
    val hasMatch: Boolean,
    @SerializedName("HeatValue")
    val heatValue: Int,
    @SerializedName("IsCertified")
    val isCertified: Boolean,
    @SerializedName("IsPtoP")
    val isPtoP: Boolean,
    @SerializedName("IsSession")
    val isSession: Int,
    @SerializedName("Managers")
    val managers: List<Any>,
    @SerializedName("Medal")
    val medal: List<Any>,
    @SerializedName("OnlineCount")
    val onlineCount: Int,
    @SerializedName("P2pType")
    val p2pType: Int,
    @SerializedName("PKMatchBar")
    val pKMatchBar: Int,
    @SerializedName("PkSessionContribution")
    val pkSessionContribution: List<Any>,
    @SerializedName("RoomGradeInfo")
    val roomGradeInfo: RoomGradeInfo,
    @SerializedName("RoomSubscribeCount")
    val roomSubscribeCount: Int,
    @SerializedName("SafeUserGrade")
    val safeUserGrade: Int,
    @SerializedName("ServerTime")
    val serverTime: String,
    @SerializedName("SessionContribution")
    val sessionContribution: Int,
    @SerializedName("Sessions")
    val sessions: List<Any>,
    @SerializedName("Sofas")
    val sofas: List<Any>,
    @SerializedName("SpecificGradeSound")
    val specificGradeSound: Int,
    @SerializedName("UserRankLists")
    val userRankLists: List<Any>
)

data class Broadcast(
    @SerializedName("Address")
    val address: String,
    @SerializedName("BeginTime")
    val beginTime: String,
    @SerializedName("Channels")
    val channels: List<Channel>,
    @SerializedName("Cover")
    val cover: String,
    @SerializedName("GameId")
    val gameId: Int,
    @SerializedName("GameName")
    val gameName: String,
    @SerializedName("Html")
    val html: String,
    @SerializedName("Latitude")
    val latitude: Int,
    @SerializedName("LiveSource")
    val liveSource: Int,
    @SerializedName("LiveSourceType")
    val liveSourceType: Int,
    @SerializedName("LiveStreamType")
    val liveStreamType: Int,
    @SerializedName("LiveType")
    val liveType: Int,
    @SerializedName("Longitude")
    val longitude: Int,
    @SerializedName("MatchId")
    val matchId: Int,
    @SerializedName("OS")
    val oS: Int,
    @SerializedName("ParentGameId")
    val parentGameId: Int,
    @SerializedName("ParentGameName")
    val parentGameName: String,
    @SerializedName("PlayId")
    val playId: Int,
    @SerializedName("PrivateRoomType")
    val privateRoomType: Int,
    @SerializedName("RoomId")
    val roomId: Int,
    @SerializedName("Title")
    val title: String,
    @SerializedName("Token")
    val token: String,
    @SerializedName("UpStreamUrl")
    val upStreamUrl: String,
    @SerializedName("UserId")
    val userId: Int
)

data class Channel(
    @SerializedName("Code")
    val code: String,
    @SerializedName("Name")
    val name: String
)

data class RoomGradeInfo(
    @SerializedName("CurrentExp")
    val currentExp: Int,
    @SerializedName("CurrentGrade")
    val currentGrade: Int,
    @SerializedName("CurrentGradeExp")
    val currentGradeExp: Int,
    @SerializedName("NextGradeExp")
    val nextGradeExp: Int
)