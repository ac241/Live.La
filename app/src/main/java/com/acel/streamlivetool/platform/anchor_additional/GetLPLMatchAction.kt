package com.acel.streamlivetool.platform.anchor_additional

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.platform.anchor_additional.bean.LPLMatch
import com.acel.streamlivetool.util.TextUtil
import com.google.gson.Gson
import okhttp3.Request
import java.lang.StringBuilder

class GetLPLMatchAction : AdditionalActionInterface {
    override fun get(): String {
        val html = RetrofitUtils.okHttpClient.newCall(
            Request.Builder().get().url("https://www.scoregg.com/match_pc?tournamentID=172").build()
        ).execute().body()?.string()
        if (html != null) {
            val jsonStr = TextUtil.subStringAfterWhat(
                html,
                "<div class=\"match-wrap\">",
                "<script>var t_data = ",
                "</script>"
            )
            val lplMatch = Gson().fromJson(jsonStr, LPLMatch::class.java)
            val matches = StringBuilder()
            lplMatch.this_week_match.forEach {
                matches.append("<li>${it.start_date} ${it.start_time} ${it.team_a_name} ${it.team_b_name}</li>")
            }
            return MyApplication.application.resources.getString(
                R.string.lpl_match,
                matches.toString()
            )
        }
        return ""
    }
}