package com.acel.streamlivetool.anchor_additional.action

import android.content.Context
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor

interface AdditionalActionInterface {
    val iconResourceId: Int
        get() = R.drawable.ic_additional_button

    /**
     * 功能名
     */
    val actionName: String

    /**
     * 传入的anchor是否匹配扩展功能，adapter中执行，尽量不要进行复杂操作或创建对象
     */
    fun match(anchor: Anchor): Boolean

    /**
     * 执行扩展功能
     */
    fun doAction(context: Context, anchor: Anchor)
}