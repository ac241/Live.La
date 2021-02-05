package com.acel.streamlivetool.ui.player

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.acel.streamlivetool.R

class RotatingImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {
    private val rotateAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.rotate)

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        when (visibility) {
            View.VISIBLE -> {
                clearAnimation()
                startAnimation(rotateAnimation)
            }
            else ->
                clearAnimation()
        }
    }

}