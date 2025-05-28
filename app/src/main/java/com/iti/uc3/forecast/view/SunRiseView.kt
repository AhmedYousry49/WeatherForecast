package com.iti.uc3.forecast.view

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.iti.uc3.forecast.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// Assuming R.drawable.ic_sunny exists in the project
// Assuming this data class structure is used to pass data
data class SunTimes(
    val sunriseEpoch: Long,
    val sunsetEpoch: Long,
    val timeZoneId: String
)

class SunRiseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var sunriseEpoch: Long = 0
    private var sunsetEpoch: Long = 0
    private var timeZoneId: String = TimeZone.getDefault().id
    private lateinit var timeFormatter: SimpleDateFormat

    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.GRAY // Color of the dashed line
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 40f // Adjust as needed
        textAlign = Paint.Align.CENTER
    }

    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
    }

    private val sunBitmap: Bitmap by lazy {
        getBitmapFromVector(context, R.drawable.ic_sunny) // Use a placeholder if R is not available
    }
    private val smallSunBitmap: Bitmap by lazy {
        // Create a smaller version for the sunset marker if needed, or reuse
        Bitmap.createScaledBitmap(sunBitmap, sunBitmap.width / 2, sunBitmap.height / 2, true)
    }

    private val arcRect = RectF()
    private var sunX: Float = 0f
    private var sunY: Float = 0f
    private var pathRadius: Float = 0f
    private var arcCenterX: Float = 0f
    private var arcCenterY: Float = 0f

    private var sunriseText: String = "--:--"
    private var sunsetText: String = "--:--"

    // Store the sun's progress (0.0 to 1.0)
    private var sunProgress: Float = 0f

    init {
        // Initialize timezone-specific formatter
        updateFormatter()
    }

    fun setSunTimes(times: SunTimes) {
        this.sunriseEpoch = times.sunriseEpoch
        this.sunsetEpoch = times.sunsetEpoch
        this.timeZoneId = times.timeZoneId
        updateFormatter()
        updateTimesText()
        calculateSunPosition()
        invalidate() // Request redraw
    }

    private fun updateFormatter() {
        timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone(timeZoneId)
        }
    }

    private fun updateTimesText() {
        if (sunriseEpoch > 0) {
            sunriseText = timeFormatter.format(Date(sunriseEpoch * 1000))
        }
        if (sunsetEpoch > 0) {
            sunsetText = timeFormatter.format(Date(sunsetEpoch * 1000))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Adjust height to accommodate text labels below the arc
        val desiredWidth = 600 // Increased default width
        val desiredHeight = 350 // Increased default height for text

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val padding = 60f // Padding around the arc + text
        val textHeight = textPaint.descent() - textPaint.ascent() + 20f // Approximate height needed for text
        val availableHeight = h - padding - textHeight
        val availableWidth = w - padding * 2

        pathRadius = min(availableWidth / 2f, availableHeight)
        arcCenterX = w / 2f
        // Center Y adjusted upwards to leave space for text below
        arcCenterY = h - textHeight - padding / 2f

        arcRect.set(
            arcCenterX - pathRadius,
            arcCenterY - pathRadius,
            arcCenterX + pathRadius,
            arcCenterY + pathRadius
        )

        // Create gradient based on final dimensions - applied to the partial path later
        gradientPaint.shader = LinearGradient(
            arcCenterX,
            arcCenterY - pathRadius, // Top of the arc
            arcCenterX,
            arcCenterY, // Baseline of the arc
            Color.parseColor("#FDB813"), // Lighter color near arc (adjust as needed)
            Color.parseColor("#403200"), // Darker color near base (adjust as needed)
            Shader.TileMode.CLAMP
        )

        calculateSunPosition() // Recalculate position when size changes
    }

    private fun calculateSunPosition() {
        if (sunriseEpoch <= 0 || sunsetEpoch <= 0 || sunsetEpoch <= sunriseEpoch) {
            // Default position and progress if times are invalid
            sunX = arcCenterX - pathRadius // Start at sunrise position
            sunY = arcCenterY
            sunProgress = 0f
            return
        }

        val currentTimeMillis = System.currentTimeMillis()
        val sunriseMillis = sunriseEpoch * 1000
        val sunsetMillis = sunsetEpoch * 1000

        // Clamp current time to be within sunrise and sunset for positioning on the arc
        val clampedTime = currentTimeMillis.coerceIn(sunriseMillis, sunsetMillis)

        val totalDuration = (sunsetMillis - sunriseMillis).toFloat()
        val elapsedDuration = (clampedTime - sunriseMillis).toFloat()

        // Calculate progress (0.0 to 1.0)
        sunProgress = if (totalDuration > 0) elapsedDuration / totalDuration else 0f

        // Angle: 180 degrees (left, sunrise) to 0 degrees (right, sunset)
        val angleRad = Math.toRadians((180 * (1 - sunProgress)).toDouble())

        // Calculate sun's X and Y coordinates
        sunX = (arcCenterX + pathRadius * cos(angleRad)).toFloat()
        sunY = (arcCenterY - pathRadius * sin(angleRad)).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw the PARTIAL gradient fill (under the arc up to sun position)
        val sweepAngle = 180 * sunProgress // Calculate sweep angle based on progress
        // Use a small threshold to avoid drawing tiny/invalid paths when progress is near 0
        if (sweepAngle > 0.1f) {
            val partialGradientPath = Path()
            // Start at the baseline below the sunrise marker
            partialGradientPath.moveTo(arcCenterX - pathRadius, arcCenterY)
            // Draw the arc segment from sunrise (180 deg) up to the current sun position
            partialGradientPath.addArc(arcRect, 180f, sweepAngle)
            // The arc ends at (sunX, sunY). Now draw a line down to the baseline at sunX.
            partialGradientPath.lineTo(sunX, arcCenterY)
            // Close the path by drawing a line back to the start along the baseline
            partialGradientPath.close() // This implicitly draws the line back to the start point

            canvas.drawPath(partialGradientPath, gradientPaint)
        }

        // 2. Draw the FULL dashed arc path (always visible)
        canvas.drawArc(arcRect, 180f, 180f, false, pathPaint)

        // 3. Draw Sunrise marker (small circle)
        val sunriseMarkerX = arcCenterX - pathRadius
        val sunriseMarkerY = arcCenterY
        canvas.drawCircle(sunriseMarkerX, sunriseMarkerY, 15f, markerPaint)

        // 4. Draw Sunset marker (small sun icon)
        val sunsetMarkerX = arcCenterX + pathRadius
        val sunsetMarkerY = arcCenterY
        // Adjust position to center the bitmap
        canvas.drawBitmap(
            smallSunBitmap,
            sunsetMarkerX - smallSunBitmap.width / 2f,
            sunsetMarkerY - smallSunBitmap.height / 2f,
            null
        )

        val scaledSunBitmap = Bitmap.createScaledBitmap(
            sunBitmap,
            (smallSunBitmap.width * 0.8f).toInt(),  // 60% width
            (smallSunBitmap.height * 0.8f).toInt(), // 60% height
            true
        )


        // 5. Draw the main Sun icon at the calculated position
        if (sunriseEpoch > 0 && sunsetEpoch > 0) { // Only draw if times are valid
            // Adjust position to center the bitmap
            canvas.drawBitmap(
                scaledSunBitmap,
                sunX - scaledSunBitmap.width / 2f,
                sunY - scaledSunBitmap.height / 2f,
                null
            )
        }

        // 6. Draw Time Labels
        val textY = arcCenterY + textPaint.descent() - textPaint.ascent() + 10f // Position below arc baseline
        canvas.drawText("Sunrise", sunriseMarkerX+5, textY + 7f, textPaint) // Label above time
        canvas.drawText(sunriseText, sunriseMarkerX+5, textY + 40f, textPaint)

        canvas.drawText("Sunset", sunsetMarkerX, textY + 7f, textPaint) // Label above time
        canvas.drawText(sunsetText, sunsetMarkerX, textY+ 40f, textPaint)
    }

    companion object {
        // Helper function to convert vector drawable to Bitmap safely
        fun getBitmapFromVector(context: Context, @DrawableRes drawableId: Int): Bitmap {
            // Placeholder for R.drawable.ic_sunny - Replace with actual ID
            val actualDrawableId = try {
                context.resources.getIdentifier("ic_sunny", "drawable", context.packageName)
                    .takeIf { it != 0 } ?: android.R.drawable.star_on // Fallback if not found
            } catch (e: Exception) {
                android.R.drawable.star_on // Fallback on error
            }

            val drawable = ContextCompat.getDrawable(context, actualDrawableId)
                ?: throw IllegalArgumentException("Drawable $actualDrawableId not found")

            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth.takeIf { it > 0 } ?: 100, // Provide default size
                drawable.intrinsicHeight.takeIf { it > 0 } ?: 100,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }
}

