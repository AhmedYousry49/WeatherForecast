package com.iti.uc3.forecast.view


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.iti.uc3.LocaleHelper.formatTemperature
import com.iti.uc3.forecast.data.model.TempUnit
import kotlin.math.max

class HourlyChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var data: List<HourlyForecast> = emptyList()

    private val backgroundPaint = Paint().apply {
        color = Color.BLACK
    }

    private val chartPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f, 0f, 0f, 600f,
            Color.CYAN, Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        style = Paint.Style.FILL
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color =  Color.GRAY
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    private val pointRadius = 10f
    val paddingHorizontal = 120f
    private val verticalSpace = 400f
    private var maxTemp = 0
    private var minTemp = 0
    private var tempUnit: TempUnit = TempUnit.CELSIUS

    fun setData(forecastList: List<HourlyForecast>, toUnit: TempUnit) {
        // Convert temperatures to the desired unit

        tempUnit=toUnit
        data = forecastList
        maxTemp = forecastList.maxOfOrNull { it.temperature } ?: 0
        minTemp = forecastList.minOfOrNull { it.temperature } ?: 0
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val chartWidth = (data.size - 1) * paddingHorizontal + 2 * paddingHorizontal
        val chartHeight = 600
        setMeasuredDimension(chartWidth.toInt(), chartHeight)
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return

        // Draw background
//        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        val tempRange = max(maxTemp - minTemp, 1)
        val points = data.mapIndexed { index, item ->
            val x = paddingHorizontal + index * paddingHorizontal
            val y = height - ((item.temperature - minTemp) / tempRange.toFloat() * verticalSpace + 50f)
            x to y
        }

        // Build smooth path
        val path = Path()
        points.forEachIndexed { i, (x, y) ->
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                val (prevX, prevY) = points[i - 1]
                val midX = (prevX + x) / 2
                path.cubicTo(midX, prevY, midX, y, x, y)
            }
        }

        // Draw fill path under curve
        val fillPath = Path(path).apply {
            lineTo(points.last().first, height.toFloat())
            lineTo(points.first().first, height.toFloat())
            close()
        }
        canvas.drawPath(fillPath, fillPaint)

        // Draw line path
        canvas.drawPath(path, chartPaint)

        // Draw points and text
        data.forEachIndexed { index, item ->
            val (x, y) = points[index]
            canvas.drawCircle(x, y, pointRadius, pointPaint)


            canvas.drawText( formatTemperature(item.temperature,tempUnit) , x, y - 20f, textPaint)
            canvas.drawText(item.hour, x, height.toFloat() - 10f, textPaint)
        }
    }


    fun getScrollDistance(): Int {
        return ((data.size - 6).coerceAtLeast(0) * paddingHorizontal).toInt()
    }
}
