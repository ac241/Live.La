package com.acel.streamlivetool.ui.player

/**
 * @author acel
 * 用于横屏时自动翻转屏幕方向
 */
import android.content.pm.ActivityInfo
import android.view.OrientationEventListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class OrientationEventListener(activity: PlayerActivity) :
    OrientationEventListener(activity),
    LifecycleObserver {
    private var playerActivity: PlayerActivity? = activity

    override fun onOrientationChanged(orientation: Int) {
        playerActivity?.apply {
            //仅横屏全屏生效
            if (landscape && fullScreen.value!!) {
                when (orientation) {
                    in (85..95) ->
                        playerActivity?.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    in (265..275) ->
                        playerActivity?.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (canDetectOrientation())
            this.enable()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        if (canDetectOrientation())
            disable()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disable()
        playerActivity = null
    }
}

class OrientationEventListenerF(playerFragment: PlayerFragment) :
    OrientationEventListener(playerFragment.requireActivity()),
    LifecycleObserver {
    private var playerFragment: PlayerFragment? = playerFragment

    override fun onOrientationChanged(orientation: Int) {
        playerFragment?.apply {
            //仅横屏全屏生效
            if (landscape && fullScreen.value!!) {
                when (orientation) {
                    in (85..95) ->
                        playerFragment?.requireActivity()?.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    in (265..275) ->
                        playerFragment?.requireActivity()?.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (canDetectOrientation())
            this.enable()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        if (canDetectOrientation())
            disable()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disable()
        playerFragment = null
    }
}