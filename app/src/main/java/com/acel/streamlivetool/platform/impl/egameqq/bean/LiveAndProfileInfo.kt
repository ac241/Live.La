package com.acel.streamlivetool.platform.impl.egameqq.bean

data class LiveAndProfileInfo(
    val `data`: Data,
    val ecode: Int,
    val login_cost: Int,
    val time_cost: Int,
    val uid: Int
) {
    data class Data(
        val key: Key
    )

    data class Key(
        val method: String,
        val module: String,
        val retBody: RetBody,
        val retCode: Int,
        val retMsg: String
    )

    data class RetBody(
        val `data`: DataX,
        val message: String,
        val result: Int,
        val svr_time: Int,
        val time_cost: Int
    )

    data class DataX(
        val h5_url: String,
        val pay_info: PayInfo,
        val profile_info: ProfileInfo,
        val video_info: VideoInfo
    )

    data class PayInfo(
        val buy_info: BuyInfo,
        val play_info: PlayInfo
    )

    data class ProfileInfo(
        val alias_id: Int,
        val block_effect_notify: Boolean,
        val brief: String,
        val city: String,
        val country: String,
        val face_update_ts: Int,
        val face_url: String,
        val fans_count: Int,
        val is_live: Int,
        val login_type: Int,
        val max_face_size: Int,
        val nick_name: String,
        val poster_url: String,
        val province: String,
        val register_ts: Int,
        val role: Int,
        val sex: Int,
        val uid: Int,
        val user_priv: UserPriv,
        val video_count: Int
    )

    data class VideoInfo(
        val anchor_id: Int,
        val appid: String,
        val appname: String,
        val channel_id: String,
        val cover_url_1_1: String,
        val end_tm: Int,
        val game_icon: String,
        val has_4k: Boolean,
        val level_type: Int,
        val live_room_type: Int,
        val p2p_v_attr: P2pVAttr,
        val pid: String,
        val player_type: Int,
        val provider: Int,
        val start_tm: Long,
        val stream_infos: List<StreamInfo>,
        val tag: String,
        val title: String,
        val unlogin_highest_level: Int,
        val url: String,
        val use_p2p: Boolean,
        val v_attr: VAttr,
        val vid: String,
        val video_type: Int
    )

    data class BuyInfo(
        val fee: Int,
        val ticket_gift_ids: List<Any>
    )

    data class PlayInfo(
        val free_watch_time: Int,
        val live_pay_type: Int,
        val match_info: MatchInfo,
        val pay_live: Int,
        val status: Int,
        val support_platform: List<Any>
    )

    data class MatchInfo(
        val match_id: String,
        val match_name: String,
        val player_id: String,
        val player_name: String
    )

    data class UserPriv(
        val noble_info: NobleInfo,
        val priv_base: PrivBase,
        val priv_base_new: PrivBaseNew,
        val used_medals: List<Any>
    )

    data class NobleInfo(
        val level: Int,
        val u_sh: String
    )

    data class PrivBase(
        val level: Int,
        val level_pic: String
    )

    data class PrivBaseNew(
        val level: Int,
        val level_pic: String
    )

    data class P2pVAttr(
        val conf_data: String,
        val v_cache_tm_max: Int,
        val v_cache_tm_min: Int,
        val v_play_mode: Int
    )

    data class StreamInfo(
        val bitrate: Int,
        val desc: String,
        val file_size: Int,
        val h265_decode_type: Int,
        val h265_play_url: String,
        val h265_play_url_conf_data: String,
        val height: Int,
        val level_type: Int,
        val play_time_shift_url: String,
        val play_url: String,
        val play_url_conf_data: String,
        val vp9_play_url: String,
        val width: Int
    )

    data class VAttr(
        val dual_id: Int,
        val dual_type: Int,
        val hv_direction: Int,
        val source: String,
        val v_cache_tm_max: Int,
        val v_cache_tm_min: Int,
        val v_height: Int,
        val v_play_mode: Int,
        val v_width: Int
    )
}