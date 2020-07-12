package com.acel.streamlivetool.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute

object AnchorListHelper {
    @Synchronized
    fun sortAnchorListByStatus(
        anchorList: LiveData<List<Anchor>>,
        map: MutableLiveData<MutableMap<String, AnchorAttribute>>
    ): List<Anchor> {
        var list: MutableList<Anchor>
        anchorList.value.let {
            list = if (it != null) (it as MutableList<Anchor>) else mutableListOf()
        }
        if (list.size == 0)
            return list
        val anchorStatusMap = map.value ?: return list
        //状态排序
        list.sortWith(Comparator { o1, o2 ->
            if (anchorStatusMap[o2.anchorKey()]?.isLive == null)
                return@Comparator 1
            if (anchorStatusMap[o1.anchorKey()]?.isLive == null)
                return@Comparator -1
            if (anchorStatusMap[o1.anchorKey()]?.isLive == anchorStatusMap[o2.anchorKey()]?.isLive)
                return@Comparator 0
            if (anchorStatusMap[o2.anchorKey()]?.isLive == true) {
                return@Comparator 1
            } else
                return@Comparator -1
        })

        //ID再排序一次
        list.sortWith(Comparator { o1, o2 ->
            if (anchorStatusMap[o1.anchorKey()]?.isLive == anchorStatusMap[o2.anchorKey()]?.isLive) {
                if (o1.id < o2.id)
                    return@Comparator -1
                else
                    return@Comparator 1
            } else {
                return@Comparator 0
            }
        })
        return list
    }
}