package com.acel.streamlivetool.anchor_additional.action

import android.content.Context
import com.acel.streamlivetool.bean.Anchor

interface AdditionalActionInterface {
    /**
     * 功能名
     */
    val actionName: String

    /**
     * 传入的anchor是否匹配扩展功能
     */
    fun match(anchor: Anchor): Boolean

    /**
     * 执行扩展功能
     */
    fun doAction(context: Context, anchor: Anchor)
}