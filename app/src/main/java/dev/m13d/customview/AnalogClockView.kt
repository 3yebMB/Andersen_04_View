package dev.m13d.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class AnalogClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val partsCount = 3
    private val pointerRatio = 2f / partsCount

    private val paint = Paint()
    var clockColor = Color.BLACK
    var secondColor = Color.RED
    var minuteColor = Color.BLUE
    var hourColor = Color.BLACK
    var clockStroke = 10f
    var hourStroke = 10f
    var minuteStroke = 15f
    var secondStroke = 5f

    var clockTagLength = 0f
    var hourLength = 0f
    var minuteLength = 0f
    var secondLength = 0f

    private var width = 0f
    private var height = 0f
    private var radius = 0f

    private var refreshThread: Thread? = null
    private var mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0) invalidate()
        }
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.AnalogClockView, 0, 0)
            .apply {
                try {
                    secondColor = getColor(R.styleable.AnalogClockView_secondColor, secondColor)
                    minuteColor = getColor(R.styleable.AnalogClockView_minuteColor, minuteColor)
                    hourColor = getColor(R.styleable.AnalogClockView_hourColor, hourColor)
                    clockColor = getColor(R.styleable.AnalogClockView_clockColor, clockColor)

                    hourStroke = getDimension(R.styleable.AnalogClockView_hourStroke, hourStroke)
                    minuteStroke =
                        getDimension(R.styleable.AnalogClockView_minuteStroke, minuteStroke)
                    secondStroke =
                        getDimension(R.styleable.AnalogClockView_secondStroke, secondStroke)
                    clockStroke = getDimension(R.styleable.AnalogClockView_clockStroke, clockStroke)
                } finally {
                    recycle()
                }
            }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        with(paint) {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeWidth = clockStroke
            color = clockColor
        }

        drawOuterCircle(canvas, paint)
        drawScale(canvas)
        getCurrentTime(canvas)
    }

    private fun drawOuterCircle(canvas: Canvas?, paint: Paint) {
        val centerX = width / 2
        val centerY = height / 2
        canvas?.drawCircle(centerX, centerY, radius - DEFAULT_OFFSET, paint)
    }

    private fun drawScale(canvas: Canvas?) {
        canvas?.save()

        val centerX = width / 2
        val centerY = height / 2

        paint.style = Paint.Style.FILL
        canvas?.translate(centerX, centerY)
        val clockMarkY = radius

        for (i in 0..59) {
            canvas?.drawLine(
                CENTER,
                clockMarkY,
                CENTER,
                clockMarkY - if (i % 5 == 0) clockTagLength else clockTagLength / 2,
                paint
            )
            canvas?.rotate(SECOND_STEP)
        }
    }

    private fun getCurrentTime(canvas: Canvas?) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        val hourAngle = hour * HOUR_STEP + HOUR_STEP / DIAL_POINTS * minute
        drawPointer(canvas, hourColor, hourStroke, hourLength, hourAngle)

        val minuteAngle = minute * SECOND_STEP + SECOND_STEP / DIAL_POINTS * second
        drawPointer(canvas, minuteColor, minuteStroke, minuteLength, minuteAngle)

        val secondAngle = second * SECOND_STEP
        drawPointer(canvas, secondColor, secondStroke, secondLength, secondAngle)
    }

    private fun drawPointer(
        canvas: Canvas?,
        color: Int,
        width: Float,
        length: Float,
        angle: Float
    ) {
        paint.color = color
        paint.strokeWidth = width
        drawTime(canvas, length, angle)
    }

    private fun drawTime(canvas: Canvas?, arrowLength: Float, angle: Float) {
        canvas?.rotate(angle)
        canvas?.drawLine(
            0f,
            arrowLength / partsCount, 0f,
            arrowLength / partsCount - arrowLength,
            paint
        )
        canvas?.rotate(CIRCLE_DEGREES - angle)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val result =
            if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
                DEFAULT_WIDTH
            } else {
                widthSpecSize.coerceAtMost(heightSpecSize)
            }

        width = result.toFloat()
        height = result.toFloat()

        radius = result / 2.0f
        clockTagLength = radius / 12
        secondLength = 2 * radius * pointerRatio
        minuteLength = secondLength * GOLDEN_RATIO / pointerRatio
        hourLength = minuteLength * GOLDEN_RATIO

        setMeasuredDimension(result, result)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        refreshThread = Thread {
            while (true) {
                try {
                    Thread.sleep(1000)
                    mHandler.sendEmptyMessage(0)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        refreshThread?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler.removeCallbacksAndMessages(null)
        refreshThread?.interrupt()
    }

    companion object {
        private const val DEFAULT_WIDTH = 200
        private const val HOUR_STEP = 30f
        private const val SECOND_STEP = 6f
        private const val DEFAULT_OFFSET = 5
        private const val CENTER = 0f
        private const val CIRCLE_DEGREES = 360f
        private const val GOLDEN_RATIO = 0.62f
        private const val DIAL_POINTS = 60
    }
}
