package com.acel.streamlivetool.platform.base

import com.acel.streamlivetool.bean.Anchor

interface IAnchor {
    /**
     * 获取直播间信息
     * @param queryAnchor Anchor
     * @return Anchor ,if fail return null
     */
    fun getAnchor(queryAnchor: Anchor): Anchor?

    /**
     * 更新主播信息
     * @return result
     */
    fun updateAnchorData(queryAnchor: Anchor): Boolean

    /**
     * 搜索直播间
     * @return 返回null表示不支持搜索，无数据返回空List
     */
    fun searchAnchor(keyword: String): List<Anchor>? = null
}