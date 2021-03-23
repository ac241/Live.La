package com.acel.streamlivetool.anchor_extension.action

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor

/**
 * 主播扩展功能，比如显示LPL比赛信息
 */
interface AnchorExtensionInterface {
    companion object {
        val iconDrawableDefault = ResourcesCompat.getDrawable(
            MyApplication.application.resources,
            R.drawable.ic_additional_button,
            null
        )
    }

    val iconResourceId: Int

    val iconDrawable: Drawable

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