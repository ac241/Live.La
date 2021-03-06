/*
 * Copyright (c) 2020.
 * @author acel
 */

package com.acel.streamlivetool.anchor_extension

import android.content.Context
import com.acel.streamlivetool.anchor_extension.action.AnchorExtensionInterface
import com.acel.streamlivetool.anchor_extension.action.GetLOLMatchExtension
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.custom.AlertDialogTool
import com.acel.streamlivetool.util.AppUtil.mainThread
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.ToastUtil.toast

class AnchorExtensionManager {
    companion object {
        val instance by lazy { AnchorExtensionManager() }
    }

    private val actionList = listOf(
        GetLOLMatchExtension.instance
    )

    fun match(anchor: Anchor): Boolean {
        for (action in actionList) {
            if (action.match(anchor))
                return true
        }
        return false
    }

    fun getActions(anchor: Anchor): List<AnchorExtensionInterface>? {
        val list = mutableListOf<AnchorExtensionInterface>()
        actionList.forEach {
            if (it.match(anchor))
                list.add(it)
        }
        return if (list.isNotEmpty()) list else null
    }

    fun doActions(anchor: Anchor, context: Context) {
        val actions = getActions(anchor)
        actions?.let {
            when (it.size) {
                0 -> {
                    mainThread {
                        toast("nothing")
                    }
                    return@let
                }
                1 ->
                    actions[0].doAction(context, anchor)
                else -> {
                    val builder = AlertDialogTool.newAlertDialog(context)
                    val arrayList = arrayListOf<String>()
                    it.forEach { action ->
                        arrayList.add(action.actionName)
                    }
                    builder.setItems(arrayList.toTypedArray()) { _, which ->
                        MainExecutor.execute {
                            actions[which].doAction(context, anchor)
                        }
                    }
                    mainThread {
                        builder.show()
                    }
                }
            }
        }
    }
}