package com.acel.streamlivetool.ui.custom_view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import androidx.annotation.Keep
import androidx.constraintlayout.utils.widget.ImageFilterView

class FloatingAvatar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageFilterView(context, attrs, defStyleAttr) {

    var progress = 0f
    private var animator: ObjectAnimator? = null
    var currentLocation = Pair(0f, 0f)
    var targetLocation = Pair(0f, 0f)
    var currentDimens = Pair(0f, 0f)
    var targetDimens = Pair(0f, 0f)
    private val animateValueStart = 0f
    private val animateValueEnd = 1000f
    val decelerateInterpolator = DecelerateInterpolator()

    @Synchronized
    fun move(
        startLocation: Pair<Float, Float>,
        targetLocation: Pair<Float, Float>,
        startDimens: Pair<Float, Float>,
        targetDimens: Pair<Float, Float>,
        duration: Long,
        doOnCancel: () -> Unit,
        doOnEnd: () -> Unit
    ) {
        this.currentLocation = startLocation
        this.currentDimens = startDimens
        this.targetLocation = targetLocation
        this.targetDimens = targetDimens
        animator = ObjectAnimator.ofFloat(this, "moveTo", animateValueStart, animateValueEnd)
            .setDuration(duration)
        animator?.addListener(object : Animator.AnimatorListener {
            var isCancel = false
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                if (!isCancel)
                    doOnEnd.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {
                doOnCancel.invoke()
                isCancel = true
            }
        })
        animator?.interpolator = decelerateInterpolator
        animator?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator = null
    }

    fun getMoveTo(): Float {
        return progress
    }

    @Keep
    fun setMoveTo(progress: Float) {
        val params = layoutParams.apply {
            x =
                currentLocation.first + progress / animateValueEnd * (targetLocation.first - currentLocation.first)
            y =
                currentLocation.second + progress / animateValueEnd * (targetLocation.second - currentLocation.second)
            width =
                (currentDimens.first + progress / animateValueEnd * (targetDimens.first - currentDimens.first)).toInt()
            height =
                (currentDimens.second + progress / animateValueEnd * (targetDimens.second - currentDimens.second)).toInt()
        }
        layoutParams = params
        this.progress = progress
    }

}