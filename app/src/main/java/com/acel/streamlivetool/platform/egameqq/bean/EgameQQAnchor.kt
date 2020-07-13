package com.acel.streamlivetool.platform.egameqq.bean

import com.google.gson.annotations.SerializedName


data class EgameQQAnchor(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("ecode")
    val ecode: Int,
    @SerializedName("login_cost")
    val loginCost: Int,
    @SerializedName("time_cost")
    val timeCost: Int,
    @SerializedName("uid")
    val uid: Int
) {

    data class Data(
        @SerializedName("key")
        val key: Key
    )

    data class Key(
        @SerializedName("method")
        val method: String,
        @SerializedName("module")
        val module: String,
        @SerializedName("retBody")
        val retBody: RetBody,
        @SerializedName("retCode")
        val retCode: Int,
        @SerializedName("retMsg")
        val retMsg: String
    )

    data class RetBody(
        @SerializedName("data")
        val `data`: DataX,
        @SerializedName("message")
        val message: String,
        @SerializedName("result")
        val result: Int,
        @SerializedName("svr_time")
        val svrTime: Int,
        @SerializedName("time_cost")
        val timeCost: Int
    )

    data class DataX(
        @SerializedName("alias_id")
        val aliasId: Int,
        @SerializedName("anchor_medals")
        val anchorMedals: List<Any>,
        @SerializedName("app_package_name")
        val appPackageName: String,
        @SerializedName("app_scheme")
        val appScheme: String,
        @SerializedName("appicon")
        val appicon: String,
        @SerializedName("appid")
        val appid: String,
        @SerializedName("appname")
        val appname: String,
        @SerializedName("bg_image_url")
        val bgImageUrl: String,
        @SerializedName("cache_ts")
        val cacheTs: Int,
        @SerializedName("channel_id")
        val channelId: String,
        @SerializedName("download_state")
        val downloadState: Int,
        @SerializedName("face_url")
        val faceUrl: String,
        @SerializedName("fans_count")
        val fansCount: Int,
        @SerializedName("fans_group_list")
        val fansGroupList: List<FansGroup>,
        @SerializedName("fans_group_list_v2")
        val fansGroupListV2: List<FansGroupV2>,
        @SerializedName("follow_count")
        val followCount: Int,
        @SerializedName("game_certified_status")
        val gameCertifiedStatus: Int,
        @SerializedName("game_download_url")
        val gameDownloadUrl: String,
        @SerializedName("gang_info")
        val gangInfo: GangInfo,
        @SerializedName("guardian_count")
        val guardianCount: Int,
        @SerializedName("identity_desc")
        val identityDesc: String,
        @SerializedName("identity_type")
        val identityType: Int,
        @SerializedName("is_anchor")
        val isAnchor: Boolean,
        @SerializedName("is_attention")
        val isAttention: Int,
        @SerializedName("is_live")
        val isLive: Int,
        @SerializedName("jump")
        val jump: Jump,
        @SerializedName("live_addr")
        val liveAddr: String,
        @SerializedName("max_face_size")
        val maxFaceSize: Int,
        @SerializedName("nick_name")
        val nickName: String,
        @SerializedName("notice_next_ts")
        val noticeNextTs: Int,
        @SerializedName("notice_status_switch")
        val noticeStatusSwitch: Int,
        @SerializedName("poster_url")
        val posterUrl: String,
        @SerializedName("profile")
        val profile: String,
        @SerializedName("sex")
        val sex: Int,
        @SerializedName("sociaty_name")
        val sociatyName: String,
        @SerializedName("source_type")
        val sourceType: Int,
        @SerializedName("src_author_id")
        val srcAuthorId: String,
        @SerializedName("start_tm")
        val startTm: Int,
        @SerializedName("stream_id")
        val streamId: String,
        @SerializedName("uid")
        val uid: Int,
        @SerializedName("user_priv")
        val userPriv: UserPriv,
        @SerializedName("vec_feeds_type")
        val vecFeedsType: List<VecFeedsType>,
        @SerializedName("video_count")
        val videoCount: Int
    )

    data class FansGroup(
        @SerializedName("group_id")
        val groupId: Int,
        @SerializedName("group_name")
        val groupName: String,
        @SerializedName("index")
        val index: Int,
        @SerializedName("logo")
        val logo: String
    )

    data class FansGroupV2(
        @SerializedName("group_id")
        val groupId: Int,
        @SerializedName("group_name")
        val groupName: String
    )

    data class GangInfo(
        @SerializedName("flag_name")
        val flagName: String,
        @SerializedName("gang_id")
        val gangId: Int,
        @SerializedName("gang_level")
        val gangLevel: Int,
        @SerializedName("gang_name")
        val gangName: String,
        @SerializedName("is_leader")
        val isLeader: Int,
        @SerializedName("jump_url")
        val jumpUrl: String
    )

    data class Jump(
        @SerializedName("ext")
        val ext: Ext,
        @SerializedName("json_ext")
        val jsonExt: String,
        @SerializedName("room_style")
        val roomStyle: Int,
        @SerializedName("show_time_info")
        val showTimeInfo: ShowTimeInfo
    )

    data class Ext(
        @SerializedName("is_pay")
        val isPay: String,
        @SerializedName("show_screen_type")
        val showScreenType: String
    )

    data class ShowTimeInfo(
        @SerializedName("data")
        val `data`: String,
        @SerializedName("type")
        val type: Int
    )

    data class UserPriv(
        @SerializedName("noble_info")
        val nobleInfo: NobleInfo,
        @SerializedName("priv_base")
        val privBase: PrivBase,
        @SerializedName("priv_base_new")
        val privBaseNew: PrivBaseNew,
        @SerializedName("used_medals")
        val usedMedals: List<Any>
    )

    data class NobleInfo(
        @SerializedName("level")
        val level: Int,
        @SerializedName("u_sh")
        val uSh: String
    )

    data class PrivBase(
        @SerializedName("level")
        val level: Int,
        @SerializedName("level_pic")
        val levelPic: String
    )

    data class PrivBaseNew(
        @SerializedName("level")
        val level: Int,
        @SerializedName("level_pic")
        val levelPic: String
    )

    data class VecFeedsType(
        @SerializedName("feeds_type")
        val feedsType: Int,
        @SerializedName("name")
        val name: String
    )
}