/*
 * Copyright (c) 2020.
 * @author acel
 * 用于保存各平台anchor list数据
 */

package com.acel.streamlivetool.ui.main

import androidx.lifecycle.MutableLiveData
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.platform.bean.ResultGetAnchorListByCookieMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class AnchorListManager {
    companion object {
        val instance by lazy {
            AnchorListManager()
        }
    }

    private val platformAnchorListMap = mutableMapOf<IPlatform, MutableList<Anchor>>()
    private val lastUpdateTimeMap = mutableMapOf<IPlatform, MutableLiveData<Long>>()

    init {
        PlatformDispatcher.getAllPlatformInstance().also { it ->
            it.values.forEach {
                initPlatform(it)
            }
        }
    }

    fun initPlatform(iPlatform: IPlatform) {
        if (lastUpdateTimeMap[iPlatform] == null)
            lastUpdateTimeMap[iPlatform] = MutableLiveData()
        if (platformAnchorListMap[iPlatform] == null)
            platformAnchorListMap[iPlatform] = mutableListOf()
    }

    fun getUpdateTimeLiveData(iPlatform: IPlatform): MutableLiveData<Long> {
        return lastUpdateTimeMap[iPlatform]!!
    }

    fun getAnchorList(iPlatform: IPlatform): List<Anchor> {
        return platformAnchorListMap[iPlatform]!!
    }

    fun updateAnchorList(iPlatform: IPlatform): ResultGetAnchorListByCookieMode? {
        var result: ResultGetAnchorListByCookieMode? = null
        runBlocking {
            val res = async(Dispatchers.IO) {
                val res = iPlatform.getAnchorsWithCookieMode()
                if (res.isCookieOk)
                    res.anchorList?.let {
                        platformAnchorListMap[iPlatform]?.apply {
                            clear()
                            addAll(it)
                            com.acel.streamlivetool.util.AnchorListUtil.insertStatusPlaceHolder(
                                this
                            )
                        }
                    }
                res
            }
            result = res.await()
        }
        return result
    }
}