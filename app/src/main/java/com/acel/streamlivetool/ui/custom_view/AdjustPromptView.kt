package com.acel.streamlivetool.ui.custom_view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import com.acel.streamlivetool.R

class AdjustPromptView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var type = AdjustType.VOLUME

    private var volumeBitmap: Bitmap =
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_volume, null)!!.toBitmap()
    private var volumeMute =
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_volume_mute, null)!!.toBitmap()
    private var brightnessBitmap =
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_brightness, null)!!.toBitmap()
    private var currentBitmap: Bitmap = volumeBitmap

    private val bitmapPercent = 0.6
    private var bitmapLeft = 0f
    private var bitmapTop = 0f

    private fun resetBitmap() {
        val newHeight = (height * bitmapPercent).toInt()
        val newWidth = newHeight * volumeBitmap.width / volumeBitmap.height
        volumeBitmap = volumeBitmap.scale(newWidth, newHeight)
        volumeMute = volumeMute.scale(newWidth, newHeight)
        brightnessBitmap = brightnessBitmap.scale(newWidth, newHeight)

        bitmapLeft = width * 0.05f
        bitmapTop = (height - volumeBitmap.height) / 2.toFloat()
    }

    private var backgroundRectF = newBackgroundRectF
    private var progress = 60
    private val maxProgress = 100
    private val progressHeight = 2f
    private val progressMarginHorizontalPercent = 0.1f
    private val progressBGRectParam = ProgressRectParam(height = progressHeight, 0F, 0F, 0F, 0F)
    private var progressBGRectF = newProgressBGRectF
    private var progressRectF = RectF()

    private val newBackgroundRectF
        get() = RectF(0F, 0F, width.toFloat(), height.toFloat())

    private val newProgressBGRectF
        get() = with(progressBGRectParam) { RectF(left, top, right, bottom) }

    private fun RectF.resetProgress() {
        progressBGRectF.apply {
            val width = right - left
            this@resetProgress.set(
                left,
                top,
                left + progress.toFloat() / maxProgress * width,
                bottom
            )
        }
    }

    private fun resetRectF() {
        backgroundRectF = newBackgroundRectF
        progressBGRectParam.apply {
            left = bitmapLeft + currentBitmap.width + width * progressMarginHorizontalPercent
            top = this@AdjustPromptView.height / 2 - height
            right = width - width * progressMarginHorizontalPercent
            bottom = this@AdjustPromptView.height / 2 + height
        }
        progressBGRectF = newProgressBGRectF
        progressRectF.resetProgress()
    }

    private val blackAlphaPaint = Paint().apply {
        color = Color.parseColor("#55000000")
    }

    private val whitePaint = Paint().apply {
        color = Color.WHITE
    }

    private val greyAlphaPaint = Paint().apply { color = Color.parseColor("#99CCCCCC") }
    private val progressColorPaint = Paint().apply {
        color = ResourcesCompat.getColor(context.resources, R.color.colorPrimary, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        resetRectF()
        resetBitmap()
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            drawRoundRect(backgroundRectF, 15f, 15f, blackAlphaPaint)
            drawRoundRect(progressBGRectF, 15f, 15f, greyAlphaPaint)
            drawRoundRect(progressRectF.apply { resetProgress() }, 15f, 15f, progressColorPaint)
            when (type) {
                AdjustType.VOLUME -> {
                    if (progress > 0)
                        drawBitmap(volumeBitmap, bitmapLeft, bitmapTop, whitePaint)
                    else
                        drawBitmap(volumeMute, bitmapLeft, bitmapTop, whitePaint)
                }
                AdjustType.BRIGHTNESS -> {
                    drawBitmap(brightnessBitmap, bitmapLeft, bitmapTop, whitePaint)
                }
            }
        }

    }

    fun showProgress(type: AdjustType, progress: Int) {
        visibility = VISIBLE
        this.type = type
        this.progress = progress
        invalidate()
    }

    fun hide() = run { visibility = GONE }

    data class ProgressRectParam(
        var height: Float,
        var left: Float,
        var top: Float,
        var right: Float,
        var bottom: Float,
    )
}