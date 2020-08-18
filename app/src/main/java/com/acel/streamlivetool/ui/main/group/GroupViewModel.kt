package com.acel.streamlivetool.ui.main.group

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.util.AnchorListUtil
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.MainExecutor
import java.util.*

class GroupViewModel(private val groupFragment: GroupFragment) : ViewModel() {

    class ViewModeFactory(private val groupFragment: GroupFragment) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GroupViewModel(
                groupFragment
            ) as T
        }
    }

    //数据库读取的anchorList
    private val anchorRepository =
        AnchorRepository.getInstance()

    //排序后的anchorList
    val sortedAnchorList = MediatorLiveData<MutableList<Anchor>>().also {
        it.value = Collections.synchronizedList(mutableListOf())
        it.addSource(anchorRepository.anchorList) { sourceList ->
            it.value?.clear()
            it.value?.addAll(sourceList)
            it.postValue(it.value)
            getAllAnchorsAttribute()
        }
        it.observe(groupFragment, Observer {
            groupFragment.refreshAnchorAttribute()
        })
    }

    /**
     * 更新主播属性
     */
    @Synchronized
    fun updateAnchorAttribute(anchorAttribute: AnchorAttribute) {
        sortedAnchorList.value?.let {
            val index = it.indexOf(anchorAttribute.anchor)
            if (index != -1) {
                with(it[index]) {
                    status = anchorAttribute.status
                    title = anchorAttribute.title
                    anchorAttribute.avatar?.apply { avatar = this }
                    anchorAttribute.keyFrame?.apply { keyFrame = this }
                    anchorAttribute.secondaryStatus?.apply { secondaryStatus = this }
                    anchorAttribute.typeName?.apply { typeName = this }
                }
            }
        }
        AnchorListUtil.sortAnchorListByStatus(sortedAnchorList.value!!)
        AnchorListUtil.insertStatusPlaceHolder(sortedAnchorList.value!!)
        sortedAnchorList.postValue(sortedAnchorList.value)
    }

    private fun getAnchorsAttribute(anchor: Anchor) {
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
            } finally {
                runOnUiThread {
                    groupFragment.hideSwipeRefreshBtn()
                }
            }
        }
    }

    fun getAllAnchorsAttribute() {
        sortedAnchorList.value?.forEach { anchor ->
            getAnchorsAttribute(anchor)
        }
    }

    fun deleteAnchor(anchor: Anchor) {
        anchorRepository.deleteAnchor(anchor)
    }
}