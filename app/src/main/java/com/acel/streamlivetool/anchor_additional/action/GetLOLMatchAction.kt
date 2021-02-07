/*
 * Copyright (c) 2020.
 * @author acel
 */

@file:Suppress("UNCHECKED_CAST")

package com.acel.streamlivetool.anchor_additional.action

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Html
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.acel.streamlivetool.R
import com.acel.streamlivetool.anchor_additional.action.bean.LPLMatch
import com.acel.streamlivetool.anchor_additional.action.bean.WanPlusLOLSchedule
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.net.RetrofitUtils
import com.acel.streamlivetool.util.AppUtil
import com.acel.streamlivetool.util.AppUtil.mainThread
import com.acel.streamlivetool.util.TextUtil
import com.google.gson.*
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.alert_browser_page.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class GetLOLMatchAction :
    AdditionalActionInterface {
    companion object {
        val instance by lazy {
            GetLOLMatchAction()
        }
    }

    override val iconResourceId: Int
        get() = R.drawable.ic_lpl_schedule

    override val actionName: String
        get() = "英雄联盟本周赛程"

    val anchorList = listOf(
        Anchor("douyu", "英雄联盟赛事", "288016", "288016"),
        Anchor("douyu", "英雄联盟赛事", "664810", "664810"),
        Anchor("douyu", "英雄联盟赛事", "522424", "522424"),
        Anchor("huya", "英雄联盟赛事", "660000", "1346609715"),
        Anchor("bilibili", "哔哩哔哩英雄联盟赛事", "7734200", "7734200"),
        Anchor("egameqq", "LPL夏季赛主舞台", "58049", "367958257")
    )

    override fun match(anchor: Anchor) = anchorList.contains(anchor)

    @SuppressLint("ResourceType")
    override fun doAction(context: Context, anchor: Anchor) {
//        showWanplusMatch(context)
        showWanplusPage(context)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showWanplusPage(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.alert_browser_page)
        mainThread {
            val dialog = builder.show()
            dialog.alert_webView.apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        loadUrl("javascript:\$(\".app-download\").remove()")
                        loadUrl("javascript:\$(\".filter\").click()")
                        loadUrl("javascript:\$(\".slide-list li:contains('LPL')\")[0].click()\n")
                        loadUrl("javascript:\$(\".tip-list button\").click()")
                    }
                }
                loadUrl("https://m.wanplus.com/schedule")
            }
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ResourceType")
    private fun showWanplusMatch(context: Context) {
        val requestBody = RequestBody.create(
            MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"),
            "_gtk=1087737148&game=0&time=1601856000&eids=1003%2C"
        )
        val jsonStr = RetrofitUtils.okHttpClient.newCall(
            Request.Builder().post(requestBody)
                .url("https://www.wanplus.com/ajax/schedule/list")
                .header("X-Requested-With", "XMLHttpRequest")
                .build()
        ).execute().body()?.string()
//        val schedule = com.alibaba.fastjson.JSONObject.parseObject(jsonStr,WanPlusLOLSchedule::class.java)
        val gson =
            GsonBuilder().registerTypeAdapter(
                object : TypeToken<WanPlusLOLSchedule>() {}.type,
                WanPlusMatchDeserializer()
            )
                .create()
        val schedule =
            gson.fromJson<WanPlusLOLSchedule>(
                jsonStr,
                object : TypeToken<WanPlusLOLSchedule>() {}.type
            )
        val scheduleList = schedule.data.scheduleList
        val matchStr = StringBuilder()
        val hintColor =
            MyApplication.application.resources.getString(R.color.colorPrimary).trim()
                .replaceFirst("FF", "", true)
        val currentTime = System.currentTimeMillis()
        val nowDate = SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(currentTime)
        scheduleList.forEach {
            val isToday = it.key == nowDate
            it.value.let { schedule ->
                schedule?.list?.apply {
                    if (isNotEmpty()) {
                        matchStr.append("<h4${if (isToday) " style='color:$hintColor'" else ""}>${schedule.lDate}</h4>")
                        matchStr.append("<ul>")
                        forEach { match ->
                            matchStr.append("<li>${match.starttime} ${match.oneseedname} vs ${match.twoseedname}\n</li>")
                        }
                        matchStr.append("</ul>")
                    }
                }
            }
        }
        val htmlText = MyApplication.application.resources.getString(
            R.string.lol_match_html,
            matchStr.toString()
        )
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.alert_additional_action)
        AppUtil.mainThread {
            val dialog = builder.show()
            val textView =
                dialog.findViewById<TextView>(R.id.textView_additional_action)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                textView?.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
            else
                textView?.text = Html.fromHtml(htmlText)
        }
    }

    /**
     * 修正match list为false时的错误
     */
    class WanPlusMatchDeserializer : JsonDeserializer<WanPlusLOLSchedule> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): WanPlusLOLSchedule {
            val gson = Gson()
            val scheduleList =
                json.asJsonObject.getAsJsonObject("data").getAsJsonObject("scheduleList")
            val map = gson.fromJson<MutableMap<String, Any?>>(
                scheduleList as JsonElement,
                object : TypeToken<Map<String, Any>>() {}.type
            )
            map.forEach {
                try {
                    val list = (it.value as LinkedTreeMap<String, Any>).get("list")
                    if (list is Boolean) {
                        (it.value as LinkedTreeMap<String, Any>).put(
                            "list",
                            null
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            json.asJsonObject.getAsJsonObject("data")
                .apply {
                    remove("scheduleList")
                    add("scheduleList", gson.toJsonTree(map).asJsonObject)
                }
            return Gson().fromJson<WanPlusLOLSchedule>(
                json,
                object : TypeToken<WanPlusLOLSchedule>() {}.type
            )
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ResourceType")
    private fun showLPLMatch(context: Context) {
        val html = RetrofitUtils.okHttpClient.newCall(
            Request.Builder().get().url("https://www.scoregg.com/match_pc?tournamentID=172")
                .build()
        ).execute().body()?.string()
        if (html != null) {
            val jsonStr = TextUtil.subStringAfterAny(
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
                R.string.lol_match_html,
                matches.toString()
            )
            val builder = AlertDialog.Builder(context)
            builder.setView(R.layout.alert_additional_action)
            AppUtil.mainThread {
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