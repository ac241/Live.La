package com.acel.streamlivetool.platform.anchor_additional

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.platform.anchor_additional.bean.LPLMatch
import com.acel.streamlivetool.util.TextUtil
import com.google.gson.Gson
import okhttp3.Request
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class GetLPLMatchAction : AdditionalActionInterface {
    @SuppressLint("ResourceType")
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
            val currentTime = System.currentTimeMillis()
            val mdFormat = SimpleDateFormat("MM-dd", Locale.CHINA)
            val color = MyApplication.application.resources.getString(R.color.colorPrimary).trim().replaceFirst("FF", "",  true)
            lplMatch.this_week_match.forEach {
                val date = it.start_time.split(" ")[0]
                val dateMatch = date == mdFormat.format(currentTime)
                matches.append(
                    "<li ${if (dateMatch) "style='color:$color'" else ""}>${if (dateMatch) "<b>" else ""}${it.start_date} ${it.start_time} ${it.team_a_name} vs ${it.team_b_name}${if (dateMatch) "</b>" else ""}</li>"
                )
            }
            return MyApplication.application.resources.getString(
                R.string.lpl_match,
                matches.toString()
            )
        }
        return ""
    }
}