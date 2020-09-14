/*
 * Copyright (c) 2020.
 * @author acel
 */

package com.acel.streamlivetool.platform.anchor_additional

import android.content.Context
import com.acel.streamlivetool.bean.Anchor

class AdditionalActionManager {
    companion object {
        val instance by lazy { AdditionalActionManager() }
    }

    private val actionList = listOf<AdditionalActionInterface>(
        GetLPLMatchAction.instance
    )

    fun match(anchor: Anchor): Boolean {
        actionList.forEach {
            if (it.match(anchor))
                return true
        }
        return false
    }

    fun doAdditionalAction(anchor: Anchor, context: Context) {
        actionList.forEach {
            if (it.match(anchor))
                it.doAction(context, anchor)
        }
    }

    fun getActionName(anchor: Anchor): String {
        actionList.forEach {
            if (it.match(anchor))
                return it.actionName
        }
        return ""
    }
}