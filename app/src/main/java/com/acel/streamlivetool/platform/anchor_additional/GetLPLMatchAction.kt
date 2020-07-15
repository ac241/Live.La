@file:Suppress("DEPRECATION")

package com.acel.streamlivetool.platform.anchor_additional

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Html
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.acel.streamlivetool.MainExecutor
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.platform.anchor_additional.bean.LPLMatch
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.TextUtil
import com.google.gson.Gson
import okhttp3.Request
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class GetLPLMatchAction : AdditionalActionInterface {
    @SuppressLint("ResourceType")
    override fun doAction(context: Context) {
            val html = RetrofitUtils.okHttpClient.newCall(
                Request.Builder().get().url("https://www.scoregg.com/match_pc?tournamentID=172")
                    .build()
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
                val color =
                    MyApplication.application.resources.getString(R.color.colorPrimary).trim()
                        .replaceFirst("FF", "", true)
                lplMatch.this_week_match.forEach {
                    val date = it.start_time.split(" ")[0]
                    val dateMatch = date == mdFormat.format(currentTime)
                    matches.append(
                        "<li ${if (dateMatch) "style='color:$color'" else ""}>${if (dateMatch) "<b>" else ""}${it.start_date} ${it.start_time} ${it.team_a_name} vs ${it.team_b_name}${if (dateMatch) "</b>" else ""}</li>"
                    )
                }
                val htmlText = MyApplication.application.resources.getString(
                    R.string.lpl_match,
                    matches.toString()
                )
                val builder = AlertDialog.Builder(context)
                builder.setView(R.layout.alert_additional_action)
                AppUtil.runOnUiThread {
                    val dialog = builder.show()
                    val textView =
                        dialog.findViewById<TextView>(R.id.textView_additional_action)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        textView?.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
                    else
                        textView?.text = Html.fromHtml(htmlText)
                }
            }
    }
}