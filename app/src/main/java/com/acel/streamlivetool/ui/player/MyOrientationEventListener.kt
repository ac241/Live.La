package com.acel.streamlivetool.ui.player

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.OrientationEventListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlin.math.abs

class MyOrientationEventListener(private val activity: AppCompatActivity) :
    OrientationEventListener(activity),
    LifecycleObserver {

    private var nowOrientation = when (activity.resources.configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> 270
        Configuration.ORIENTATION_PORTRAIT -> 0
        else -> 0
    }

    private var nowScreenOrientation = when (activity.resources.configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onOrientationChanged(orientation: Int) {
        if (abs(orientation - nowOrientation) > 50) {
            val newScreenOrientation = when (orientation) {
                in (-5..5) ->
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                in (85..95) ->
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                in (265..275) ->
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else ->
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            if (newScreenOrientation != nowScreenOrientation) {
                nowOrientation = orientation
                nowScreenOrientation = newScreenOrientation
                activity.requestedOrientation = newScreenOrientation
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

}