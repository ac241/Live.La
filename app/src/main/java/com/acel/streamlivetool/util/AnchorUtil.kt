/*
 * Copyright (c) 2020.
 * @author acel
 */

package com.acel.streamlivetool.util

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.value.ConstValue
import java.text.DecimalFormat

object AnchorUtil {

    /**
     * 更新主播数据
     */
    fun Anchor.update(newAnchor: Anchor) {
        title = newAnchor.title
        otherParams = newAnchor.otherParams
        status = newAnchor.status
        title = newAnchor.title
        avatar = newAnchor.avatar
        keyFrame = newAnchor.keyFrame
        secondaryStatus = newAnchor.secondaryStatus
        typeName = newAnchor.typeName
        online = newAnchor.online
        liveTime = newAnchor.liveTime
    }

    /**
     * 关注列表中不包含改主播时修改
     */
    fun Anchor.setNotFollowedHint() {
        title = ConstValue.FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR
        status = false
    }

    fun Anchor.isFollowed() = title != ConstValue.FOLLOW_LIST_DID_NOT_CONTAINS_THIS_ANCHOR

    private val df = DecimalFormat("#.0")
    fun formatOnlineNumber(int: Int): String {
        return if (int > 9999) {
            "${df.format(int.toFloat() / 10000)}万"
        } else int.toString()
    }
}