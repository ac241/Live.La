package com.acel.streamlivetool.platform.longzhu.bean

data class RoomStatus(
    val AppChatStatus: Int,
    val BaseRoomInfo: BaseRoomInfo,
    val Broadcast: Broadcast,
    val CityId: Int,
    val CollegeId: Int,
    val Contribution: Int,
    val FlowerCount: Int,
    val HasMatch: Boolean,
    val IsBroadcasting: Boolean,
    val IsPtoP: Boolean,
    val IsSignRoom: Boolean,
    val LastCategoryId: Int,
    val OnlineCount: Int,
    val P2pType: Int,
    val PKMatchBar: Int,
    val RoomGrade: Int,
    val RoomScreenshot: String,
    val SlzType: Int,
    val SlzTypeId: String,
    val Vid: String
)

data class BaseRoomInfo(
    val AnchorCategory: Int,
    val AnchorCertification: String,
    val Avatar: String,
    val BoardCastAddress: String,
    val BoardCastTitle: String,
    val DailyPlayHourConfig: Int,
    val Desc: String,
    val Domain: String,
    val Game: Int,
    val GameName: String,
    val Id: Int,
    val LivePermission: Int,
    val Name: String,
    val Status: Int,
    val SubscribeCount: Int,
    val Type: Int,
    val UserId: Int,
    val UserTitle: String,
    val VerifiedInformation: String,
    val VideoPermission: Int,
    val WriteTime: String
)

data class Broadcast(
    val Address: String,
    val BeginTime: String,
    val Channels: List<Channel>,
    val Cover: String,
    val GameId: Int,
    val GameName: String,
    val Html: String,
    val Latitude: Int,
    val LiveSource: Int,
    val LiveSourceType: Int,
    val LiveStreamType: Int,
    val LiveType: Int,
    val Longitude: Int,
    val MatchId: Int,
    val OS: Int,
    val ParentGameId: Int,
    val ParentGameName: String,
    val PlayId: Int,
    val PrivateRoomType: Int,
    val RoomId: Int,
    val Title: String,
    val Token: String,
    val UserId: Int
)

data class Channel(
    val Code: String,
    val Name: String
)