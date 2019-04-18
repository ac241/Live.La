package com.acel.livela.ui.player

import android.content.Context
import com.dueeeke.videocontroller.StandardVideoController

class MyVideoController(context: Context) : StandardVideoController(context) {
    override fun slideToChangePosition(deltaX: Float) {
    }

    override fun slideToChangeBrightness(deltaY: Float) {
        super.slideToChangeVolume(deltaY)
    }

    override fun slideToChangeVolume(deltaY: Float) {
        super.slideToChangeBrightness(deltaY)
    }
}