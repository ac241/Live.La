package com.acel.streamlivetool.ui.main.cookie

import android.animation.Animator
import android.util.Log
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.main.public_class.ProcessStatus
import com.acel.streamlivetool.util.AppUtil.runOnUiThread
import com.acel.streamlivetool.util.MainExecutor
import com.acel.streamlivetool.util.ToastUtil.toast
import kotlinx.android.synthetic.main.text_view_process_update_anchors.*


class CookieViewModel(private val cookieFragment: CookieFragment) :
    ViewModel() {
    class ViewModeFactory(private val cookieFragment: CookieFragment) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CookieViewModel(
                cookieFragment
            ) as T
        }
    }


    val anchorList = mutableListOf<Anchor>()
    private var updateProcessAnimate: ViewPropertyAnimator? = null
    private val showProcessToast = false

    internal fun getAnchors() {
        showUpdateProcess(ProcessStatus.WAIT)
        MainExecutor.execute {
            try {
                val anchorsCookieMode =
                    cookieFragment.platform?.let {
                        PlatformDispatcher.getPlatformImpl(it)?.getAnchorsWithCookieMode()
                    }
                if (anchorsCookieMode != null) {
                    if (!anchorsCookieMode.cookieOk) {
                        cookieFragment.showLoginTextView()
                        anchorList.clear()
                        notifyDataChange()
                        runOnUiThread {
                            toast(if (anchorsCookieMode.message.isEmpty()) "请先登录" else anchorsCookieMode.message)
                        }
                        completeUpdateProcess(ProcessStatus.COOKIE_INVALID)
                    } else {
                        with(anchorsCookieMode.anchors) {
                            if (this != null) {
                                if (this.isEmpty()) {
                                    cookieFragment.showListMsg(if (anchorsCookieMode.message.isEmpty()) "无数据" else anchorsCookieMode.message)
                                } else
                                    cookieFragment.hideListMsg()
                                anchorList.clear()
                                anchorList.addAll(this)
                                com.acel.streamlivetool.util.AnchorListUtil.insertStatusPlaceHolder(
                                    anchorList
                                )
                                completeUpdateProcess(ProcessStatus.SUCCESS)
                                notifyDataChange()
                            }
                        }
                        cookieFragment.hideLoginTextView()
                    }
                }
            } catch (e: Exception) {
                Log.d("getAnchorsCookieMode", "cookie mode获取主播属性失败：cause:${e.javaClass.name}")
                val processStatus = when (e) {
                    is java.net.SocketTimeoutException -> ProcessStatus.NET_TIME_OUT
                    is java.net.UnknownHostException -> ProcessStatus.NET_TIME_OUT
                    else -> ProcessStatus.NET_TIME_OUT
                }
                completeUpdateProcess(processStatus)
            } finally {
                runOnUiThread {
                    cookieFragment.binding?.cookieSwipeRefresh?.isRefreshing = false
                }
            }
        }
    }

    private fun notifyDataChange() {
        runOnUiThread {
            cookieFragment.nowAnchorAdapter.notifyAnchorsChange()
        }
    }

    private fun showUpdateProcess(status: ProcessStatus) {
        if (showProcessToast)
            return
        updateProcessAnimate?.cancel()
        runOnUiThread {
            cookieFragment.textView_process_update_anchors.apply {
                this.text = "更新数据...${status.getValue()}"
                visibility = View.VISIBLE
            }
        }
    }

    private fun completeUpdateProcess(status: ProcessStatus) {
        if (showProcessToast)
            return
        showUpdateProcess(status)
        cookieFragment.textView_process_update_anchors.apply {
            updateProcessAnimate = animate().alpha(0f).setDuration(2000)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(p0: Animator?) {
                        visibility = View.GONE
                        alpha = 0.5f
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                        alpha = 0.5f
                    }

                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) {}
                }).setStartDelay(3000)
        }
    }


}

