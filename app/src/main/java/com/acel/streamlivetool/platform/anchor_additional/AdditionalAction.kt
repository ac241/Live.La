package com.acel.streamlivetool.platform.anchor_additional

import android.content.Context
import com.acel.streamlivetool.bean.Anchor

class AdditionalAction {
    companion object {
        val instance by lazy { AdditionalAction() }
    }

    private val anchorActionMap = mutableMapOf<Anchor, AdditionalActionInterface>().also {
        it[Anchor("douyu", "英雄联盟赛事", "288016", "288016")] = GetLPLMatchAction()
        it[Anchor("huya", "英雄联盟赛事", "660000", "1346609715")] = GetLPLMatchAction()
        it[Anchor("bilibili", "哔哩哔哩英雄联盟赛事", "7734200", "7734200")] = GetLPLMatchAction()
        it[Anchor("egameqq", "LPL夏季赛主舞台", "58049", "367958257")] = GetLPLMatchAction()
    }

    fun check(anchor: Anchor): Boolean {
        return anchorActionMap.keys.contains(anchor)
    }

    fun doAdditionalAction(anchor: Anchor, context: Context) {
        anchorActionMap[anchor]?.doAction(context)
    }
}