package com.acel.streamlivetool.ui.main

import android.content.Context
import com.acel.streamlivetool.MainExecutor
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.db.DbManager
import com.acel.streamlivetool.platform.PlatformDispatcher
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast


class MainPresenter(private var view: MainConstract.View?) : MainConstract.Presenter, AnkoLogger {
    val context = view as Context
    var anchorList: MutableList<Anchor> = mutableListOf()
    val anchorStatusMap = mutableMapOf<String, Boolean>()
    val anchorTitleMap = mutableMapOf<String, String>()
    private val anchorDao = DbManager.getInstance(context)?.getDaoSession(context)?.anchorDao

    override fun addAnchor(queryAnchor: Anchor) {
        MainExecutor.execute(AddAnchorRunnable(queryAnchor))
    }

    inner class AddAnchorRunnable(val queryAnchor: Anchor) : Runnable {
        override fun run() {
            val platformImpl = PlatformDispatcher.getPlatformImpl(queryAnchor.platform)
            try {
                val anchor = platformImpl?.getAnchor(queryAnchor)
                if (anchor != null) {
//                Log.d("ACEL_LOG", anchorList.indexOf(anchor).toString())
                    if (anchorList.indexOf(anchor) == -1) {
                        anchorDao?.insert(anchor)
                        initAnchorList()
                        view?.addAnchorSuccess(anchor)
                        //添加后获取状态
                        getAnchorsStatus(anchor)
                    } else {
                        view?.addAnchorFailed("该直播间已存在——${anchor.nickname}")
                    }

                } else {
                    view?.addAnchorFailed("该直播间找寻不到")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                context.runOnUiThread {
                    context.toast("发生错误。")
                }
            }
        }
    }

    init {
        initAnchorList()
        getAllAnchorsStatus()
    }

    @Synchronized
    fun sortAnchorListByStatus() {
        //状态排序
        anchorList.sortWith(Comparator { o1, o2 ->
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
//            if (anchorStatusMap.get(o2.anchorKey) == null)
//
//            anchorStatusMap.get(o1.anchorKey) == anchorStatusMap.get(o2.anchorKey)
        })
        //ID再排序一次
        anchorList.sortWith(Comparator { o1, o2 ->
            if (anchorStatusMap[o1.anchorKey] == anchorStatusMap[o2.anchorKey]) {
                if (o1.id < o2.id)
                    return@Comparator -1
                else
                    return@Comparator 1
            } else {
                return@Comparator 0
            }

        })
        view?.refreshAnchorList()
    }

    private fun initAnchorList() {
        //get anchors from database
        val dataAnchorList = anchorDao?.loadAll() as ArrayList
        with(anchorList) {
            clear()
            addAll(dataAnchorList)
        }
        context.runOnUiThread {
            view?.refreshAnchorList()
        }
    }

    override fun getAnchorsStatus(anchor: Anchor) {
        MainExecutor.execute(GetStatusRunnable(anchor))
    }

    inner class GetStatusRunnable(val anchor: Anchor) : Runnable {
        override fun run() {
            try {
                val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
                val anchorStatus = platformImpl?.getStatus(anchor)
                if (anchorStatus != null) {
                    anchorStatusMap[anchorStatus.getAnchorKey()] = anchorStatus.isLive
                    anchorTitleMap[anchorStatus.getAnchorKey()] = anchorStatus.title
                    context.runOnUiThread {
                        //                    view?.refreshAnchorStatus(anchor)
                        sortAnchorListByStatus()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    override fun getAllAnchorsStatus() {
        anchorList.forEach { anchor ->
            getAnchorsStatus(anchor)
        }
    }


    override fun onDestroy() {
        view = null
    }

    override fun deleteAnchor(queryAnchor: Anchor) {
        anchorDao?.delete(queryAnchor)
        initAnchorList()
        sortAnchorListByStatus()
    }

}
