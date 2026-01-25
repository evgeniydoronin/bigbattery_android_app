package com.zetarapower.monitor.ui.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.zetarapower.monitor.R

/**
 * Кастомный View для отображения дуги прогресса 240° (как в iOS)
 * Created by Evgenii Doronin
 */
class ArcProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 30f // Толщина дуги
        color = context.getColor(R.color.battery_background)
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 30f // Толщина дуги
        color = context.getColor(R.color.bb_green) // Зеленый цвет
    }

    private val rectF = RectF()
    private var progress = 0 // Начальное значение
    private val maxProgress = 100

    // Углы дуги (как в iOS)
    private val startAngle = 150f // Начало слева внизу
    private val sweepAngle = 240f // Дуга на 240°

    fun setProgress(progress: Int) {
        this.progress = progress.coerceIn(0, maxProgress)
        invalidate()
    }

    fun getProgress(): Int = progress

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (minOf(width, height) / 2f) - progressPaint.strokeWidth / 2f

        // Настройка прямоугольника для дуги
        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // Рисуем фоновую дугу (серую)
        canvas.drawArc(rectF, startAngle, sweepAngle, false, backgroundPaint)

        // Рисуем дугу прогресса (зеленую)
        val progressSweep = (progress.toFloat() / maxProgress.toFloat()) * sweepAngle
        canvas.drawArc(rectF, startAngle, progressSweep, false, progressPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = 320 * resources.displayMetrics.density.toInt() // 320dp - увеличенный размер
        setMeasuredDimension(size, size)
    }
}
