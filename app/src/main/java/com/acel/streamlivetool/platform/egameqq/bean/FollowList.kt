package com.acel.streamlivetool.platform.egameqq.bean

data class FollowList(
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
        val fans_count: Int,
        val follow_count: Int,
        val online_follow_count: Int,
        val online_follow_list: List<OnlineFollow>
    )

    data class OnlineFollow(
        val channel_id: String,
        val face_update_ts: Int,
        val follow_ts: Int,
        val last_play_time: Int,
        val live_info: LiveInfo,
        val max_face_size: Int,
        val new_dynamic_time: Int,
        val status: Int,
        val str_pid: String,
        val video_count: Int
    )


    data class LiveInfo(
        val anchor_face_url: String,
        val anchor_id: Int,
        val anchor_name: String,
        val appid: String,
        val appname: String,
        val certified_status: Int,
        val city: String,
        val ext: Ext,
        val fans_count: Int,
        val jump: Jump,
        val jump_url: String,
        val lbs_info: LbsInfo,
        val live_room_style: Int,
        val multi_pk_info: MultiPkInfo,
        val notice_next_content: String,
        val notice_next_date: String,
        val online: Int,
        val pid: String,
        val pk_info: PkInfo,
        val program_res: ProgramRes,
        val report_info: ReportInfo,
        val tag: String,
        val title: String,
        val use_p2p: Boolean,
        val user_bet_coin: Int,
        val user_bet_num: Int,
        val user_priv: UserPriv,
        val video_info: VideoInfo,
        val win_rate: Int
    )

    class Ext

    data class Jump(
        val ext: ExtX,
        val json_ext: String,
        val room_style: Int,
        val show_time_info: ShowTimeInfo
    )

    data class LbsInfo(
        val ad_code: Int,
        val lbs_desc: String
    )

    data class MultiPkInfo(
        val minor_status: Int
    )

    data class PkInfo(
        val guest_anchor_id: Int,
        val guest_cover_url: String,
        val guest_face_url: String,
        val guest_pk_level: String
    )

    data class ProgramRes(
        val cover: Cover,
        val cover_frame: CoverFrame,
        val cover_url: String,
        val icon_tag: IconTag,
        val left_tag: LeftTag,
        val right_tag: RightTag,
        val right_user_tag: RightUserTag
    )

    data class ReportInfo(
        val algo_id: Int,
        val algo_info: String,
        val algo_source: Int,
        val resource_id: Int,
        val strategy_id: Int
    )

    data class UserPriv(
        val noble_info: NobleInfo,
        val priv_base: PrivBase,
        val priv_base_new: PrivBaseNew,
        val used_medals: List<UsedMedal>
    )

    data class VideoInfo(
        val bg_color_pc: String,
        val bitrate: Int,
        val channel_id: String,
        val desc: String,
        val dst: String,
        val h265_decode_type: Int,
        val h265_url: String,
        val play_url: String,
        val player_type: Int,
        val provider: Int,
        val url: String,
        val url_high_reslution: String,
        val url_low_resolution: String,
        val v_attr: VAttr,
        val vid: String,
        val video_type: Int
    )

    class ExtX

    data class ShowTimeInfo(
        val `data`: String,
        val type: Int
    )

    data class Cover(
        val cover_url: String,
        val square_cover_url: String
    )

    data class CoverFrame(
        val url_16_9: String,
        val url_1_1: String
    )

    data class IconTag(
        val position: Int,
        val priority: Int,
        val style_type: Int,
        val url: String
    )

    data class LeftTag(
        val name: String,
        val priority: Int,
        val style_type: Int
    )

    data class RightTag(
        val name: String,
        val priority: Int,
        val style_type: Int
    )

    data class RightUserTag(
        val name: String,
        val style_type: Int
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

    data class UsedMedal(
        val medal_id: Int,
        val medal_level: Int,
        val medal_small_pic: String
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

    data class LiveInfoX(
        val anchor_face_url: String,
        val anchor_id: Int,
        val anchor_name: String,
        val appid: String,
        val appname: String,
        val certified_status: Int,
        val city: String,
        val ext: ExtXX,
        val fans_count: Int,
        val jump: JumpX,
        val jump_url: String,
        val lbs_info: LbsInfoX,
        val live_room_style: Int,
        val multi_pk_info: MultiPkInfoX,
        val notice_next_content: String,
        val notice_next_date: String,
        val online: Int,
        val pid: String,
        val pk_info: PkInfoX,
        val program_res: ProgramResX,
        val report_info: ReportInfoX,
        val tag: String,
        val title: String,
        val use_p2p: Boolean,
        val user_bet_coin: Int,
        val user_bet_num: Int,
        val user_priv: UserPrivX,
        val video_info: VideoInfoX,
        val win_rate: Int
    )

    class ExtXX

    data class JumpX(
        val ext: ExtXXX,
        val json_ext: String,
        val room_style: Int,
        val show_time_info: ShowTimeInfoX
    )

    data class LbsInfoX(
        val ad_code: Int,
        val lbs_desc: String
    )

    data class MultiPkInfoX(
        val minor_status: Int
    )

    data class PkInfoX(
        val guest_anchor_id: Int,
        val guest_cover_url: String,
        val guest_face_url: String,
        val guest_pk_level: String
    )

    data class ProgramResX(
        val cover: CoverX,
        val cover_frame: CoverFrameX,
        val cover_url: String,
        val icon_tag: IconTagX,
        val left_tag: LeftTagX,
        val right_tag: RightTagX,
        val right_user_tag: RightUserTagX
    )

    data class ReportInfoX(
        val algo_id: Int,
        val algo_info: String,
        val algo_source: Int,
        val resource_id: Int,
        val strategy_id: Int
    )

    data class UserPrivX(
        val noble_info: NobleInfoX,
        val priv_base: PrivBaseX,
        val priv_base_new: PrivBaseNewX,
        val used_medals: List<UsedMedalX>
    )

    data class VideoInfoX(
        val bg_color_pc: String,
        val bitrate: Int,
        val channel_id: String,
        val desc: String,
        val dst: String,
        val h265_decode_type: Int,
        val h265_url: String,
        val play_url: String,
        val player_type: Int,
        val provider: Int,
        val url: String,
        val url_high_reslution: String,
        val url_low_resolution: String,
        val v_attr: VAttrX,
        val vid: String,
        val video_type: Int
    )

    class ExtXXX

    data class ShowTimeInfoX(
        val `data`: String,
        val type: Int
    )

    data class CoverX(
        val cover_url: String,
        val square_cover_url: String
    )

    data class CoverFrameX(
        val url_16_9: String,
        val url_1_1: String
    )

    data class IconTagX(
        val position: Int,
        val priority: Int,
        val style_type: Int,
        val url: String
    )

    data class LeftTagX(
        val name: String,
        val priority: Int,
        val style_type: Int
    )

    data class RightTagX(
        val name: String,
        val priority: Int,
        val style_type: Int
    )

    data class RightUserTagX(
        val name: String,
        val style_type: Int
    )

    data class NobleInfoX(
        val level: Int,
        val u_sh: String
    )

    data class PrivBaseX(
        val level: Int,
        val level_pic: String
    )

    data class PrivBaseNewX(
        val level: Int,
        val level_pic: String
    )

    data class UsedMedalX(
        val medal_id: Int,
        val medal_level: Int,
        val medal_small_pic: String
    )

    data class VAttrX(
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