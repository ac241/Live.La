package com.acel.streamlivetool.util

import com.acel.streamlivetool.anchor_extension.AnchorExtensionManager
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.ui.main.adapter.AnchorSection

object AnchorListUtil {
    /**
     * 对anchor list排序
     */
    fun sortAnchorListByStatus(anchorList: MutableList<Anchor>) {
        //状态排序
        anchorList.sortWith(Comparator { o1, o2 ->
            if (o1.status == o2.status)
                return@Comparator 0
            if (o2.status)
                return@Comparator 1
            if (o1.status)
                return@Comparator -1
            return@Comparator 0
        })
        //ID再排序一次
        anchorList.sortWith(Comparator { o1, o2 ->
            if (o1.status == o2.status) {
                if (o1.id < o2.id)
                    return@Comparator -1
                else
                    return@Comparator 1
            } else {
                return@Comparator 0
            }
        })
    }

    /**
     * 插入分组
     */
    fun insertSection(list: MutableList<Anchor>) {
        list.remove(AnchorSection.ANCHOR_SECTION_LIVING)
        list.remove(AnchorSection.ANCHOR_SECTION_NOT_LIVING)
        var livingIndex: Int = -1
        var notLivingIndex: Int = -1
        run breaking@{
            list.forEachIndexed { index, anchor ->
                if (anchor.status) {
                    if (livingIndex == -1)
                        livingIndex = 0
                } else {
                    notLivingIndex = index
                    return@breaking
                }
            }
        }
        if (livingIndex != -1)
            list.add(livingIndex, AnchorSection.ANCHOR_SECTION_LIVING)
        if (notLivingIndex != -1)
            list.add(
                if (list.contains(AnchorSection.ANCHOR_SECTION_LIVING)) notLivingIndex + 1 else notLivingIndex,
                AnchorSection.ANCHOR_SECTION_NOT_LIVING
            )
    }

    /**
     * 移除分组
     */
    fun removeSection(anchorList: List<Anchor>): List<Anchor> {
        val list = mutableListOf<Anchor>()
        anchorList.forEach {
            if (it != AnchorSection.ANCHOR_SECTION_LIVING && it != AnchorSection.ANCHOR_SECTION_NOT_LIVING)
                list.add(it)
        }
        return list
    }

    private val additionalActionManager = AnchorExtensionManager.instance

    /**
     *
     */
    fun appointAdditionalActions(anchorList: List<Anchor>) {
        for (anchor in anchorList) {
            if (anchor.anchorExtensions != null)
                continue
            anchor.anchorExtensions = additionalActionManager.getActions(anchor)
        }
    }

    /**
     * 获取直播中的主播
     */
    fun getLivingAnchors(anchorList: List<Anchor>): List<Anchor> {
        val list = mutableListOf<Anchor>()
        anchorList.forEach {
            if (it.status)
                list.add(it)
        }
        return list
    }

}