package com.horclotapp.momentum

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CircleProgressView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val backgroundPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 10f
        isAntiAlias = true
        alpha = 76 // 30% opacity
    }

    private val progressPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val emojiPaint = Paint().apply {
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var emoji: String = "😊"
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width.coerceAtMost(height) / 2f) - 10f

        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        // Draw progress arc
        progressPaint.color = getProgressColor()
        val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        canvas.drawArc(rect, 270f, 360f * progress, false, progressPaint)

        // Draw emoji - ИСПРАВЛЕННАЯ ЧАСТЬ:
        val textBounds = Rect()
        emojiPaint.getTextBounds(emoji, 0, emoji.length, textBounds)
        val textHeight = textBounds.height()
        val emojiY = centerY + textHeight / 2f
        canvas.drawText(emoji, centerX, emojiY, emojiPaint)
    }

    private fun getProgressColor(): Int {
        return when {
            progress < 0.3 -> Color.RED
            progress < 0.7 -> Color.YELLOW
            else -> Color.GREEN
        }
    }
}