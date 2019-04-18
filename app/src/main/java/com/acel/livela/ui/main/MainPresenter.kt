package com.acel.livela.ui.main

import android.content.Context
import android.os.Parcelable
import android.util.Log
import com.acel.livela.bean.Anchor
import com.acel.livela.db.AnchorDao
import com.acel.livela.db.DbManager
import com.acel.livela.platform.PlatformPitcher
import com.acel.livela.ui.player.PlayerActivity
import org.jetbrains.anko.*

class MainPresenter(var view: MainConstract.View?) : MainConstract.Presenter, AnkoLogger {
    val context = view as Context
    var anchorList: MutableList<Anchor> = mutableListOf()
    val anchorStatusMap = mutableMapOf<String, Boolean>()
    val anchorDao = DbManager.getInstance(view as Context)?.getDaoSession(view as Context)?.anchorDao

    override fun addAnchor(queryAnchor: Anchor) {
        doAsync {
            val platformImpl = PlatformPitcher.getPlatformImpl(queryAnchor.platform)
            val anchor = platformImpl?.getAnchor(queryAnchor)
            if (anchor != null) {
                Log.d("ACEL_LOG", anchorList.indexOf(anchor).toString())
                if (anchorList.indexOf(anchor) == -1) {
                    anchorDao?.insert(anchor)
                    initAnchorList()
                    view?.addAnchorSuccess(anchor)
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
        getAnchorsStatus()
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

    override fun getAnchorsStatus() {
        anchorList.forEach { anchor ->
            doAsync {
                val platformImpl = PlatformPitcher.getPlatformImpl(anchor.platform)
                val anchorStatus = platformImpl?.getStatus(anchor)
                if (anchorStatus != null) {
                    anchorStatusMap.put(anchorStatus.getAnchorKey(), anchorStatus.living)
                    uiThread {
                        view?.refreshAnchorStatus(anchor)
                    }
                }
            }
        }
    }

    override fun startPlay(anchor: Anchor) {
        doAsync {
            val context = view as Context
            context.startActivity<PlayerActivity>("anchor" to anchor)
        }
    }

    override fun onDestroy() {
        view = null
    }

    override fun deleteAnchor(queryAnchor: Anchor) {
        anchorDao?.delete(queryAnchor)
        initAnchorList()
    }
}