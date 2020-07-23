package com.acel.streamlivetool.util

import androidx.lifecycle.MutableLiveData
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute

object AnchorListHelper {
    @Synchronized
    fun sortAnchorListByStatus(
        anchorList: MutableLiveData<MutableList<Anchor>>,
        map: MutableLiveData<MutableMap<String, AnchorAttribute>>
    ): List<Anchor> {
        var list: MutableList<Anchor>
        with(anchorList.value) {
            list = if (this != null) (this as MutableList<Anchor>) else mutableListOf()
        }
        if (list.size == 0)
            return list
        val anchorStatusMap = map.value ?: return list
        //状态排序
        list.sortWith(Comparator { o1, o2 ->
            if (anchorStatusMap[o2.anchorKey()]?.status == null)
                return@Comparator 1
            if (anchorStatusMap[o1.anchorKey()]?.status == null)
                return@Comparator -1
            if (anchorStatusMap[o1.anchorKey()]?.status == anchorStatusMap[o2.anchorKey()]?.status)
                return@Comparator 0
            if (anchorStatusMap[o2.anchorKey()]?.status == true) {
                return@Comparator 1
            } else
                return@Comparator -1
        })

        //ID再排序一次
        list.sortWith(Comparator { o1, o2 ->
            if (anchorStatusMap[o1.anchorKey()]?.status == anchorStatusMap[o2.anchorKey()]?.status) {
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