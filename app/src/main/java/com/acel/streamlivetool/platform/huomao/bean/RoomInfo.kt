package com.acel.streamlivetool.platform.huomao.bean

import com.google.gson.annotations.SerializedName


data class RoomInfo(
    @SerializedName("ad_cnt")
    val adCnt: String,
    @SerializedName("addtime")
    val addtime: String,
    @SerializedName("audienceNumber")
    val audienceNumber: Int,
    @SerializedName("channel")
    val channel: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("del_time")
    val delTime: String,
    @SerializedName("eid")
    val eid: String,
    @SerializedName("event_endtime")
    val eventEndtime: String,
    @SerializedName("event_starttime")
    val eventStarttime: String,
    @SerializedName("gameCname")
    val gameCname: String,
    @SerializedName("gameEname")
    val gameEname: String,
    @SerializedName("game_logo")
    val gameLogo: String,
    @SerializedName("game_url_rule")
    val gameUrlRule: String,
    @SerializedName("gid")
    val gid: String,
    @SerializedName("has_ad")
    val hasAd: String,
    @SerializedName("headimg")
    val headimg: Headimg,
    @SerializedName("high_status")
    val highStatus: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("image")
    val image: String,
    @SerializedName("ip_show")
    val ipShow: String,
    @SerializedName("is_auto_vod")
    val isAutoVod: String,
    @SerializedName("is_bd")
    val isBd: String,
    @SerializedName("is_conmic")
    val isConmic: Int,
    @SerializedName("is_del")
    val isDel: String,
    @SerializedName("is_duanbo")
    val isDuanbo: String,
    @SerializedName("is_entertainment")
    val isEntertainment: String,
    @SerializedName("is_event")
    val isEvent: String,
    @SerializedName("is_gf")
    val isGf: String,
    @SerializedName("is_high")
    val isHigh: String,
    @SerializedName("is_index")
    val isIndex: String,
    @SerializedName("is_index_live")
    val isIndexLive: String,
    @SerializedName("is_index_top")
    val isIndexTop: String,
    @SerializedName("is_index_type")
    val isIndexType: String,
    @SerializedName("is_jingcai")
    val isJingcai: Int,
    @SerializedName("is_live")
    val isLive: Int,
    @SerializedName("is_new")
    val isNew: String,
    @SerializedName("is_pk")
    val isPk: String,
    @SerializedName("is_push")
    val isPush: String,
    @SerializedName("is_replay")
    val isReplay: String,
    @SerializedName("is_sign")
    val isSign: String,
    @SerializedName("is_sub")
    val isSub: String,
    @SerializedName("is_surviving_number")
    val isSurvivingNumber: String,
    @SerializedName("is_tuijian")
    val isTuijian: String,
    @SerializedName("is_zhuanma")
    val isZhuanma: String,
    @SerializedName("list_a_n_color")
    val listANColor: String,
    @SerializedName("live_last_start_time")
    val liveLastStartTime: String,
    @SerializedName("logo")
    val logo: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("note")
    val note: String,
    @SerializedName("opusername")
    val opusername: String,
    @SerializedName("percent")
    val percent: String,
    @SerializedName("push_info_time")
    val pushInfoTime: String,
    @SerializedName("push_live_time")
    val pushLiveTime: String,
    @SerializedName("push_time")
    val pushTime: String,
    @SerializedName("room_number")
    val roomNumber: String,
    @SerializedName("roomadmin_num")
    val roomadminNum: String,
    @SerializedName("scope")
    val scope: String,
    @SerializedName("score_count")
    val scoreCount: String,
    @SerializedName("score_total")
    val scoreTotal: String,
    @SerializedName("sh_bz")
    val shBz: String,
    @SerializedName("sh_time")
    val shTime: String,
    @SerializedName("sh_uname")
    val shUname: String,
    @SerializedName("sh_zt")
    val shZt: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("stream")
    val stream: String,
    @SerializedName("streamNoEncode")
    val streamNoEncode: String,
    @SerializedName("subscribe")
    val subscribe: String,
    @SerializedName("superstar_info")
    val superstarInfo: Any,
    @SerializedName("tag")
    val tag: String,
    @SerializedName("tel")
    val tel: String,
    @SerializedName("tj_pic")
    val tjPic: String,
    @SerializedName("tj_title")
    val tjTitle: String,
    @SerializedName("top")
    val top: String,
    @SerializedName("uid")
    val uid: String,
    @SerializedName("updatetime")
    val updatetime: String,
    @SerializedName("user_lv")
    val userLv: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("videos")
    val videos: String,
    @SerializedName("views")
    val views: Int,
    @SerializedName("wawaStatus")
    val wawaStatus: Int
) {
    data class Headimg(
        @SerializedName("big")
        val big: String,
        @SerializedName("normal")
        val normal: String,
        @SerializedName("small")
        val small: String
    )
}

