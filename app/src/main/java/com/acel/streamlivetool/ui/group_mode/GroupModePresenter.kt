package com.acel.streamlivetool.ui.group_mode

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.AnchorListHelper.insertStatusPlaceHolder
import com.acel.streamlivetool.util.AnchorListHelper.sortAnchorListByStatus
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.ToastUtil.toast
import java.util.*

class GroupModePresenter(private var view: GroupModeConstract.View?) :
    GroupModeConstract.Presenter {
    val context = view as Context
    val anchorRepository = AnchorRepository.getInstance(context.applicationContext)

    @Synchronized
    fun updateAnchorAttribute(anchorAttribute: AnchorAttribute) {
        sortedAnchorList.value?.let {
            val index = it.indexOf(anchorAttribute.anchor)
            if (index != -1) {
                with(it[index]) {
                    status = anchorAttribute.status
                    title = anchorAttribute.title
                    avatar = anchorAttribute.avatar
                    keyFrame = anchorAttribute.keyFrame
                }
            }
        }
        sortAnchorListByStatus(sortedAnchorList.value!!)
        insertStatusPlaceHolder(sortedAnchorList.value!!)
        sortedAnchorList.postValue(sortedAnchorList.value)
    }



    val sortedAnchorList = MediatorLiveData<MutableList<Anchor>>().also {
        it.value = Collections.synchronizedList(mutableListOf())
        it.addSource(anchorRepository.anchorList) { sourceList ->
            it.value?.clear()
            it.value?.addAll(sourceList)
            it.postValue(it.value)
            getAllAnchorsAttribute()
        }
    }


    override fun addAnchor(queryAnchor: Anchor) {
        MainExecutor.execute(AddAnchorRunnable(queryAnchor))
    }

    inner class AddAnchorRunnable(val queryAnchor: Anchor) : Runnable {
        override fun run() {
            val platformImpl = PlatformDispatcher.getPlatformImpl(queryAnchor.platform)
            try {
                val anchor = platformImpl?.getAnchor(queryAnchor)
                if (anchor != null) {
                    if (anchorRepository.anchorList.value!!.indexOf(anchor) == -1) {
                        insertAnchor(anchor)
                    } else {
                        view?.addAnchorFailed("该直播间已存在——${anchor.nickname}")
                    }
                } else {
                    view?.addAnchorFailed("该直播间找寻不到")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    toast("发生错误。")
                }
            }
        }

        private fun insertAnchor(anchor: Anchor) {
            anchorRepository.insertAnchor(anchor)
            view?.addAnchorSuccess(anchor)
        }
    }

    init {
        //初始化LiveData 观察
        sortedAnchorList.observe(context as GroupModeActivity, Observer {
            view?.refreshAnchorAttribute()
        })
    }

    override fun getAnchorsAttribute(anchor: Anchor) {
        MainExecutor.execute(GetAnchorAttributeRunnable(anchor))
    }

    inner class GetAnchorAttributeRunnable(val anchor: Anchor) : Runnable {
        override fun run() {
            try {
                val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
                val anchorAttribute = platformImpl?.getAnchorAttribute(anchor)
                if (anchorAttribute != null) {
                    updateAnchorAttribute(anchorAttribute)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getAllAnchorsAttribute() {
        sortedAnchorList.value?.forEach { anchor ->
            getAnchorsAttribute(anchor)
        }
    }

    override fun onDestroy() {
        view = null
    }

}
