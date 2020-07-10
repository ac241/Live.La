package com.acel.streamlivetool

import androidx.lifecycle.MutableLiveData
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.base.MyApplication.Companion.runOnUiThread
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.db.DbManager

object MainAnchorHelper {
    val anchorList by lazy {
        MutableLiveData<MutableList<Anchor>>()
    }
    private val anchorDao = DbManager.getInstance(MyApplication.application)
        ?.getDaoSession(MyApplication.application)?.anchorDao
    private var lastStatusMap = mutableMapOf<String, AnchorAttribute>()
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

    internal fun initAnchorList() {
        val dataAnchorList = anchorDao?.loadAll() as ArrayList
        setAnchorListValue(dataAnchorList)
    }

    internal fun loadAnchorList() {
        //get anchors from database
        val dataAnchorList = anchorDao?.loadAll() as ArrayList
        postAnchorListValue(dataAnchorList)
    }

    @Synchronized
    fun sortAnchorListByStatus(anchorStatusMap: MutableMap<String, AnchorAttribute>) {
        lastStatusMap = anchorStatusMap
        val list = anchorList.value
        if (list?.size == 0)
            return
        //状态排序
        list?.sortWith(Comparator { o1, o2 ->
            if (anchorStatusMap[o2.anchorKey]?.isLive == null)
                return@Comparator 1
            if (anchorStatusMap[o1.anchorKey]?.isLive == null)
                return@Comparator -1
            if (anchorStatusMap[o1.anchorKey]?.isLive == anchorStatusMap[o2.anchorKey]?.isLive)
                return@Comparator 0
            if (anchorStatusMap[o2.anchorKey]?.isLive == true) {
                return@Comparator 1
            } else
                return@Comparator -1
        })

        //ID再排序一次
        list?.sortWith(Comparator { o1, o2 ->
            if (anchorStatusMap[o1.anchorKey]?.isLive == anchorStatusMap[o2.anchorKey]?.isLive) {
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