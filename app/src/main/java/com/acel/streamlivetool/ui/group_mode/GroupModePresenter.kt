package com.acel.streamlivetool.ui.group_mode

import android.content.Context
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.bean.AnchorPlaceHolder
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.ToastUtil.toast
import java.util.*

class GroupModePresenter(private var view: GroupModeConstract.View?) :
    GroupModeConstract.Presenter {
    val context = view as Context
    val anchorRepository = AnchorRepository.getInstance(context.applicationContext)
    val anchorAttributeMap = MutableLiveData<MutableMap<String, Anchor>>().also {
        it.value = mutableMapOf()
    }

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

    /**
     * 插入直播状态分组提示
     */
    @Synchronized
    private fun insertStatusPlaceHolder(list: MutableList<Anchor>) {
        list.remove(AnchorPlaceHolder.anchorIsLiving)
        list.remove(AnchorPlaceHolder.anchorNotLiving)
        var livingIndex: Int = -1
        var sleepingIndex: Int = -1
        run breaking@{
            list.forEachIndexed { index, anchor ->
                if (anchor.status) {
                    if (livingIndex == -1)
                        livingIndex = 0
                } else {
                    sleepingIndex = index
                    return@breaking
                }
            }
        }
        if (livingIndex != -1)
            list.add(livingIndex, AnchorPlaceHolder.anchorIsLiving)
        if (sleepingIndex != -1)
            list.add(
                if (list.contains(AnchorPlaceHolder.anchorIsLiving)) sleepingIndex + 1 else sleepingIndex,
                AnchorPlaceHolder.anchorNotLiving
            )
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

    /**
     * 对anchor list排序
     */
    private fun sortAnchorListByStatus(anchorList: MutableList<Anchor>) {
        //状态排序
        anchorList.sortWith(Comparator { o1, o2 ->
            if (o1.status == o2.status)
                return@Comparator 0
            if (o2.status)
                return@Comparator 1
            if (o1.status)
                return@Comparator -1
            return@Comparator 0
        })
        //ID再排序一次
        anchorList.sortWith(Comparator { o1, o2 ->
            if (o1.status == o2.status) {
                if (o1.id < o2.id)
                    return@Comparator -1
                else
                    return@Comparator 1
            } else {
                return@Comparator 0
            }
        })
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
