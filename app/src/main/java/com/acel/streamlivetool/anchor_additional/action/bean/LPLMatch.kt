package com.acel.streamlivetool.anchor_additional.action.bean

data class LPLMatch(
    val this_week_match: List<ThisWeekMatch>
) {

    data class ThisWeekMatch(
        val game_count: String,
        val homesite: String,
        val homesite_a: String,
        val homesite_b: String,
        val is_have_video_link: String,
        val is_publist: Int,
        val is_real_time: Int,
        val is_remind: Int,
        val live_video_url1: String,
        val matchID: String,
        val start_date: String,
        val start_time: String,
        val status: String,
        val teamID_a: String,
        val teamID_b: String,
        val team_a_image_thumb: String,
        val team_a_name: String,
        val team_a_win: String,
        val team_b_image_thumb: String,
        val team_b_name: String,
        val team_b_win: String,
        val tournament_name: String
    )

}
