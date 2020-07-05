package com.acel.streamlivetool

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.db.DbManager

object MainAnchorHelper {
    val anchorList by lazy {
        MutableLiveData<MutableList<Anchor>>()
    }
    private val anchorDao = DbManager.getInstance(MyApplication.application)
        ?.getDaoSession(MyApplication.application)?.anchorDao
    var lastStatusMap = mutableMapOf<String, Boolean>()
    private fun setAnchorListValue(list: MutableList<Anchor>) {
        anchorList.value = list
    }

    private fun postAnchorListValue(list: MutableList<Anchor>) {
        anchorList.postValue(list)
    }

    fun insertAnchor(anchor: Anchor) {
        if (anchor.platform != null)
            if (!anchorList.value!!.contains(anchor))
                anchorDao?.insert(anchor)
        loadAnchorList()
        sortAnchorListByStatus(lastStatusMap)
    }

    fun deleteAnchor(queryAnchor: Anchor) {
        anchorDao?.delete(queryAnchor)
        loadAnchorList()
        sortAnchorListByStatus(lastStatusMap)
    }

    internal fun loadAnchorList() {
        //get anchors from database
        val dataAnchorList = anchorDao?.loadAll() as ArrayList
        setAnchorListValue(dataAnchorList)
    }

    @Synchronized
    fun sortAnchorListByStatus(anchorStatusMap: MutableMap<String, Boolean>) {
        lastStatusMap = anchorStatusMap
        val list = anchorList.value
        if (list?.size == 0)
            return
        //状态排序
        list?.sortWith(Comparator { o1, o2 ->
            if (anchorStatusMap[o2.anchorKey] == null)
                return@Comparator 1
            if (anchorStatusMap[o1.anchorKey] == null)
                return@Comparator -1
            if (anchorStatusMap[o1.anchorKey] == anchorStatusMap[o2.anchorKey])
                return@Comparator 0
            if (anchorStatusMap[o2.anchorKey] == true) {
                return@Comparator 1
            } else
                return@Comparator -1
        })

        //ID再排序一次
        list?.sortWith(Comparator { o1, o2 ->
            if (anchorStatusMap[o1.anchorKey] == anchorStatusMap[o2.anchorKey]) {
                if (o1.id < o2.id)
                    return@Comparator -1
                else
                    return@Comparator 1
            } else {
                return@Comparator 0
            }
        })

        list?.let { postAnchorListValue(it) }
    }
}