package com.acel.streamlivetool.ui.main

import android.content.ActivityNotFoundException
import android.content.Context
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.db.DbManager
import com.acel.streamlivetool.platform.PlatformDispatcher
import org.jetbrains.anko.*
import java.util.concurrent.Executors


class MainPresenter(private var view: MainConstract.View?) : MainConstract.Presenter, AnkoLogger {
    val context = view as Context
    var anchorList: MutableList<Anchor> = mutableListOf()
    val anchorStatusMap = mutableMapOf<String, Boolean>()
    private val anchorDao = DbManager.getInstance(context)?.getDaoSession(context)?.anchorDao
    private val PoolExecutor = Executors.newFixedThreadPool(20)

    override fun addAnchor(queryAnchor: Anchor) {
        PoolExecutor.execute(AddAnchorRunnable(queryAnchor))
    }

    inner class AddAnchorRunnable(val queryAnchor: Anchor) : Runnable {
        override fun run() {
            val platformImpl = PlatformDispatcher.getPlatformImpl(queryAnchor.platform)
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
                    view?.addAnchorFail("该直播间已存在")
                }

            } else {
                view?.addAnchorFail("该直播间找寻不到")
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
        PoolExecutor.execute(GetStatusRunnable(anchor))
    }

    inner class GetStatusRunnable(val anchor: Anchor) : Runnable {
        override fun run() {
            val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
            val anchorStatus = platformImpl?.getStatus(anchor)
            if (anchorStatus != null) {
                anchorStatusMap[anchorStatus.getAnchorKey()] = anchorStatus.isLive
                context.runOnUiThread {
                    //                    view?.refreshAnchorStatus(anchor)
                    sortAnchorListByStatus()
                }
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

    override fun itemClick(anchor: Anchor) {
        actionWhenClick(
            context.defaultSharedPreferences.getString(
                context.getString(R.string.pref_key_item_click_action),
                ""
            ), anchor
        )
    }

    override fun secondBtnClick(anchor: Anchor) {
        actionWhenClick(
            context.defaultSharedPreferences.getString(
                context.getString(R.string.pref_key_second_button_click_action),
                ""
            ), anchor
        )

    }

    private fun actionWhenClick(actionSecondBtn: String?, anchor: Anchor) {
        when (actionSecondBtn) {
            "open_app" -> {
                doAsync {
                    val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
                    try {
                        platformImpl?.startApp(context, anchor)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                        uiThread {
                            context.toast(
                                "没有找到" +
                                        platformImpl?.platformShowNameRes?.let { it1 ->
                                            context.resources.getString(
                                                it1
                                            )
                                        }
                                        + " app..."
                            )
                        }
                    }
                }
            }
            "outer_player" -> {
                doAsync {
                    val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
                    platformImpl?.callOuterPlayer(context, anchor)
                }
            }
            else -> {
                context.toast("未定义的功能，你是怎么到达这里的0_0")
            }
        }
    }
}
