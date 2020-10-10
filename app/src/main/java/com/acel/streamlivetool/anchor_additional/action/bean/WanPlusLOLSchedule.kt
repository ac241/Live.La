/*
 * Copyright (c) 2020.
 * @author acel
 */

package com.acel.streamlivetool.anchor_additional.action.bean

import com.google.gson.annotations.SerializedName

data class WanPlusLOLSchedule(
    val code: Int,
    val `data`: Data,
    val msg: String,
    val ret: Int
) {

    data class Data(
        val isShowList: Int,
        val nextdate: String,
        val nexttime: Int,
        val prevdate: String,
        val prevtime: Int,
        val scheduleList: Map<String, Schedule?>
    )

    open class Schedule(
        val date: String,
        val filterdate: String,
        val lDate: String,
        val list: List<Match>?,
        val selected: Boolean,
        val time: Int,
        val week: String
    )

    open class Match(
        val bonum: Int? = null,
        val date: String? = null,
        val eid: String? = null,
        val ename: String? = null,
        val endtime: String? = null,
        val esportscache: String? = null,
        val gamename: String? = null,
        val gametype: String? = null,
        val groupname: String? = null,
        val hasvideo: Boolean? = null,
        val isover: Boolean? = null,
        val live: Boolean? = null,
        val liveids: String? = null,
        val oneScore: List<Int>? = null,
        val oneicon: String? = null,
        val oneseedid: String? = null,
        val oneseedname: String? = null,
        val onewin: String? = null,
        val poster: String? = null,
        val relation: String? = null,
        val scheduleid: String? = null,
        val seedscores: String? = null,
        val stageid: String? = null,
        val starttime: String? = null,
        val twoScore: List<Int>? = null,
        val twoicon: String? = null,
        val twoseedid: String? = null,
        val twoseedname: String? = null,
        val twowin: String? = null,
        val error: Boolean = false
    )

    object NullMatch : Match(error = true) {
        override fun toString(): String {
            return "scheduleid = $scheduleid"
        }
    }
}
