/*
 * Copyright (c) 2020.
 * @author acel
 * 用于保存各平台anchor list数据
 */

package com.acel.streamlivetool.manager

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.manager.UpdateResultReceiver.*
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.platform.base.AbstractPlatformImpl
import com.acel.streamlivetool.platform.bean.ApiResult
import com.acel.streamlivetool.ui.main.adapter.AnchorSection.isSection
import com.acel.streamlivetool.util.AnchorListUtil.appointAdditionalActions
import com.acel.streamlivetool.util.AnchorListUtil.insertSection
import com.acel.streamlivetool.util.AnchorUtil.setNotFollowedHint
import com.acel.streamlivetool.util.AnchorUtil.update
import kotlinx.coroutines.*
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AnchorUpdateManager {
    companion object {
        val instance by lazy {
            AnchorUpdateManager()
        }
    }

    private val platformAnchorListMap = mutableMapOf<AbstractPlatformImpl, MutableList<Anchor>>()
    private val lastUpdateTimeMap = mutableMapOf<AbstractPlatformImpl, MutableLiveData<Long>>()

    init {
        PlatformDispatcher.getAllPlatformImpl().also { it ->
            it.values.forEach {
                initPlatform(it)
            }
        }
    }

    fun initPlatform(iPlatform: AbstractPlatformImpl) {
        if (lastUpdateTimeMap[iPlatform] == null)
            lastUpdateTimeMap[iPlatform] = MutableLiveData()

        if (platformAnchorListMap[iPlatform] == null)
            platformAnchorListMap[iPlatform] = mutableListOf()
    }

    @Suppress("unused")
    fun getUpdateTimeLiveData(iPlatform: AbstractPlatformImpl): MutableLiveData<Long> {
        return lastUpdateTimeMap[iPlatform]!!
    }

    fun getAnchorList(iPlatform: AbstractPlatformImpl): List<Anchor> {
        return platformAnchorListMap[iPlatform]!!
    }

    /**
     * 获取单个平台的关注列表
     */
    fun getAnchorsByCookie(iPlatform: AbstractPlatformImpl): ApiResult<List<Anchor>>? {
        var result: ApiResult<List<Anchor>>? = null
        runBlocking {
            val res = async(Dispatchers.IO) {
                val res = iPlatform.anchorCookieModule?.getAnchorsByCookieMode()
                if (res != null) {
                    if (res.cookieValid)
                        res.data?.let {
                            platformAnchorListMap[iPlatform]?.apply {
                                clear()
                                addAll(it)
                                insertSection(this)
                                appointAdditionalActions(this)
                            }
                        }
                }
                res
            }
            result = res.await()
        }
        return result
    }

    private var updateAnchorsTask: Job? = null

    /**
     * 更新列表中的anchor
     */
    internal fun updateAnchors(
        resultReceiver: UpdateResultReceiver,
        list: List<Anchor>,
        scope: CoroutineScope
    ) {
        updateAnchorsTask?.cancel()
        updateAnchorsTask = scope.launch(Dispatchers.IO) {
            val updateTaskList = mutableListOf<Deferred<ResultSingleAnchor>>()
            list.forEach {
                if (it.isSection()) {
                    return@forEach
                }
                val task = async {
                    updateSingleAnchor(it)
                }
                updateTaskList.add(task)
            }
            val resultList = mutableListOf<ResultSingleAnchor>()
            updateTaskList.forEach {
                resultList.add(it.await())
            }
            resultReceiver.onUpdateFinish(resultList)
        }
        updateAnchorsTask?.start()
    }

    /**
     * 单个anchor更新
     */
    private fun updateSingleAnchor(anchor: Anchor): ResultSingleAnchor {
        var updateResult: ResultSingleAnchor? = null
        runCatching {
            val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
            platformImpl?.let {
                val result = platformImpl.anchorModule?.updateAnchorData(anchor)
                updateResult = ResultSingleAnchor(
                    result ?: false, anchor,
                    if (result != null && result) ResultType.SUCCESS else ResultType.FAILED
                )
            }
        }.onFailure {
            Log.d(
                "updateAllAnchorByCookie",
                "更新主播信息失败：cause:${it.javaClass.name}------"
            )
            updateResult = ResultSingleAnchor(
                success = false,
                anchor = anchor,
                resultType = it.getThrowableResultType(),
            )
            it.printStackTrace()
        }
        return updateResult ?: ResultSingleAnchor(false, anchor, ResultType.ERROR)
    }

    private var updateAnchorsByCookieTask: Job? = null

    /**
     * 以cookie方式更新列表中的主播信息
     */
    internal fun updateAllAnchorByCookie(
        resultReceiver: UpdateResultReceiver,
        queryList: List<Anchor>,
        scope: CoroutineScope
    ) {
        updateAnchorsByCookieTask?.cancel()
        updateAnchorsByCookieTask =
            scope.launch(Dispatchers.IO) {
                val platforms = PlatformDispatcher.getAllPlatformImpl()
                val updateTaskList = mutableListOf<Deferred<ResultCookieMode>>()
                //遍历所有平台
                platforms.forEach { platformEntry ->
                    //同平台的anchors
                    val samePlatformAnchorList = mutableListOf<Anchor>()
                    queryList.forEach {
                        if (it.platform == platformEntry.key)
                            samePlatformAnchorList.add(it)
                    }
                    if (samePlatformAnchorList.size > 0) {
                        if (platformEntry.value.anchorCookieModule != null) {
                            //支持cookie方式
                            val task = async {
                                updateAnchorsForSinglePlatform(
                                    platformEntry.value,
                                    samePlatformAnchorList
                                )
                            }
                            updateTaskList.add(task)
                        } else {
                            //不支持cookie方式，使用逐条更新
                            updateAnchors(resultReceiver, samePlatformAnchorList, scope)
                        }
                    }
                }

                val resultList = mutableListOf<ResultCookieMode>()
                updateTaskList.forEach { resultList.add(it.await()) }
                resultReceiver.onCookieModeUpdateFinish(resultList)
            }
        updateAnchorsByCookieTask?.start()
    }

    /**
     * 更新主页中某平台的anchor list
     * @param iPlatform 平台impl
     */
    private fun updateAnchorsForSinglePlatform(
        iPlatform: AbstractPlatformImpl,
        anchorList: MutableList<Anchor>
    ): ResultCookieMode {
        var updateResult: ResultCookieMode? = null
        runCatching {
            //更新平台anchor list
            val result = getAnchorsByCookie(iPlatform)
            result?.apply {
                updateResult = if (success && cookieValid) {
                    val targetList = getAnchorList(iPlatform)
                    anchorList.forEach {
                        val index = targetList.indexOf(it)
                        if (index == -1)
                            it.setNotFollowedHint()
                        else {
                            //更新信息
                            it.update(targetList[index])
                        }
                    }
                    ResultCookieMode(
                        isSuccess = true,
                        resultType = ResultType.SUCCESS,
                        iPlatform = iPlatform
                    )
                } else {
                    ResultCookieMode(
                        isSuccess = false,
                        resultType = ResultType.COOKIE_INVALID,
                        iPlatform = iPlatform
                    )
                }
            }
        }.onFailure {
            Log.d(
                "updateAllAnchorByCookie",
                "更新主播信息失败：cause:${it.javaClass.name}------"
            )
            updateResult = ResultCookieMode(
                isSuccess = false,
                resultType = it.getThrowableResultType(),
                iPlatform = iPlatform
            )
            it.printStackTrace()
        }
        return updateResult ?: ResultCookieMode(
            isSuccess = false,
            resultType = ResultType.ERROR,
            iPlatform = iPlatform
        )
    }

    private fun Throwable.getThrowableResultType(): ResultType {
        return when (this) {
            is SocketTimeoutException -> ResultType.NET_TIME_OUT
            is UnknownHostException -> ResultType.NET_ERROR
            is ConnectException -> ResultType.NET_ERROR
            else -> ResultType.ERROR
        }
    }

}