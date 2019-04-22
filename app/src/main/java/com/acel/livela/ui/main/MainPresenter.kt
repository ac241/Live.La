package com.acel.livela.ui.main

import android.content.Context
import android.util.Log
import com.acel.livela.R
import com.acel.livela.bean.Anchor
import com.acel.livela.db.DbManager
import com.acel.livela.platform.PlatformPitcher
import com.acel.livela.ui.player.PlayerActivity
import org.jetbrains.anko.*


class MainPresenter(var view: MainConstract.View?) : MainConstract.Presenter, AnkoLogger {
    val context = view as Context
    var anchorList: MutableList<Anchor> = mutableListOf()
    val anchorStatusMap = mutableMapOf<String, Boolean>()
    val anchorDao = DbManager.getInstance(context)?.getDaoSession(context)?.anchorDao

    override fun addAnchor(queryAnchor: Anchor) {
        doAsync {
            val platformImpl = PlatformPitcher.getPlatformImpl(queryAnchor.platform)
            val anchor = platformImpl?.getAnchor(queryAnchor)
            Log.d("ACEL_LOG", "a:" + anchor)
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
        anchorList.sortWith(Comparator { o1, o2 ->
            if (anchorStatusMap.get(o2.anchorKey) == null)
                return@Comparator 1
            if (anchorStatusMap.get(o1.anchorKey) == null)
                return@Comparator -1
            if (anchorStatusMap.get(o1.anchorKey) == anchorStatusMap.get(o2.anchorKey))
                return@Comparator 0
            if (anchorStatusMap.get(o2.anchorKey) == true) {
                return@Comparator 1
            } else
                return@Comparator -1
//            if (anchorStatusMap.get(o2.anchorKey) == null)
//
//            anchorStatusMap.get(o1.anchorKey) == anchorStatusMap.get(o2.anchorKey)
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
        doAsync {
            val platformImpl = PlatformPitcher.getPlatformImpl(anchor.platform)
            val anchorStatus = platformImpl?.getStatus(anchor)
            if (anchorStatus != null) {
                anchorStatusMap.put(anchorStatus.getAnchorKey(), anchorStatus.living)
                uiThread {
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
            "inner_player" -> {
                doAsync {
                    val context = view as Context
                    context.startActivity<PlayerActivity>("anchor" to anchor)
                }
            }
            "open_app" -> {
                doAsync {
                    val platformImpl = PlatformPitcher.getPlatformImpl(anchor.platform)
                    platformImpl?.startApp(context, anchor)
                }
            }
            "outer_player" -> {
                doAsync {
                    val platformImpl = PlatformPitcher.getPlatformImpl(anchor.platform)
                    platformImpl?.callOuterPlayer(context, anchor)
                }
            }
            else -> {
                context.toast("未定义的功能0_0")
            }
        }
    }
}
