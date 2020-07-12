package com.acel.streamlivetool.db

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.acel.streamlivetool.MainExecutor
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.util.ToastUtil.toast

class AnchorRepository {

    companion object {
        private lateinit var thisContext: Context
        private val INSTANCE by lazy {
            AnchorRepository()
        }

        fun getInstance(context: Context): AnchorRepository {
            thisContext = context
            return INSTANCE
        }
    }

    private val anchorDatabase = AnchorDatabase.getInstance(thisContext.applicationContext)
    private val anchorDao = anchorDatabase.getDao()
    internal val anchorList = anchorDao.getAllAnchors()

    /**
     * 插入anchor
     * @return Pair<结果,信息>
     */
    fun insertAnchor(anchor: Anchor): Pair<Boolean, String> {
        anchorList.value?.let {
            return if (!it.contains(anchor)) {
                MainExecutor.execute { anchorDao.insertAnchor(anchor) }
                Pair(true, thisContext.getString(R.string.add_anchor_success))
            } else {
                Pair(false, thisContext.getString(R.string.anchor_already_exist))
            }
        }
        return Pair(false, "list empty code 113")
    }

    fun deleteAnchor(anchor: Anchor) = MainExecutor.execute { anchorDao.deleteAnchor(anchor) }
    fun updateAnchor(anchor: Anchor) = MainExecutor.execute { anchorDao.updateAnchor(anchor) }
    fun getAllAnchors(): LiveData<List<Anchor>> = anchorDao.getAllAnchors()
    fun deleteAllAnchors() = MainExecutor.execute { anchorDao.deleteAllAnchors() }
}