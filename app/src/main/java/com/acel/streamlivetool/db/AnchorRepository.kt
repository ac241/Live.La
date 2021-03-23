package com.acel.streamlivetool.db

import android.util.Log
import androidx.lifecycle.LiveData
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.util.MainExecutor

class AnchorRepository {

    companion object {
        private val INSTANCE by lazy {
            AnchorRepository()
        }

        fun getInstance(): AnchorRepository {
            return INSTANCE
        }
    }

    private val anchorDatabase = AnchorDatabase.getInstance()
    private val anchorDao = anchorDatabase.getDao()
    internal val anchorList = getAllAnchors()
    private val insertList = mutableListOf<Anchor>()

    /**
     * 插入anchor
     * @return Pair<结果,信息>
     */
    fun insertAnchor(anchor: Anchor): Pair<Boolean, String> {
        with(anchorList.value) {
            //如果list为空 或者 list和插入历史中不含anchor
            return if (this == null || (!this.contains(anchor) && !insertList.contains(anchor))) {
                MainExecutor.execute { anchorDao.insertAnchor(anchor) }
                insertList.add(anchor)
                Log.d("insertAnchor", "insert anchor $anchor")
                Pair(true, MyApplication.application.getString(R.string.add_anchor_success))
            } else {
                Pair(false, MyApplication.application.getString(R.string.anchor_already_exist))
            }
        }
    }

    fun deleteAnchor(anchor: Anchor) = MainExecutor.execute {
        anchorDao.deleteAnchor(anchor)
        insertList.remove(anchor)
        Log.d("deleteAnchor", "delete anchor $anchor")
    }

    fun updateAnchor(anchor: Anchor) = MainExecutor.execute { anchorDao.updateAnchor(anchor) }
    private fun getAllAnchors(): LiveData<MutableList<Anchor>> = anchorDao.getAllAnchors()
    @Suppress("unused")
    fun deleteAllAnchors() = MainExecutor.execute { anchorDao.deleteAllAnchors() }
}