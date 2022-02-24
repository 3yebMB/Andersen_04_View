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

    private lateinit var mBlackPaint: Paint
    private lateinit var mRedPaint: Paint
    private lateinit var mBlackPaint2: Paint

    private var hour: Int = 0
    private var minute: Int = 0
    private var second: Int = 0

    private var refreshThread: Thread? = null
    private var mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0) invalidate()
        }
    }

    init {
        initPaints()
    }

    private fun initPaints() {
        mBlackPaint = Paint()
        with(mBlackPaint) {
            color = Color.BLACK
            strokeWidth = 5f
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
        mBlackPaint2 = Paint()
        with(mBlackPaint2) {
            color = Color.BLACK
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        mRedPaint = Paint()
        with(mRedPaint) {
            color = Color.RED
            strokeWidth = 5f
            isAntiAlias = true
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawOuterCircle(canvas)
        drawScale(canvas)
        getCurrentTime()
        drawHand(canvas)
    }

    private fun drawOuterCircle(canvas: Canvas?) {
        mBlackPaint.strokeWidth = 5f
        canvas?.drawCircle(
            (measuredWidth / 2).toFloat(),
            (measuredHeight / 2).toFloat(),
            ((measuredWidth / 2) - 5).toFloat(),
            mBlackPaint
        )
    }

    private fun drawScale(canvas: Canvas?) {
        var scaleLength: Float?
        canvas?.save()

        for (i in 0..59) {
            if (i % 5 == 0) {
                mBlackPaint.strokeWidth = 5f
                scaleLength = 20f
            } else {
                mBlackPaint.strokeWidth = 3f
                scaleLength = 10f
            }
            canvas?.drawLine(
                (measuredWidth / 2).toFloat(),
                5f,
                (measuredWidth / 2).toFloat(),
                (5 + scaleLength),
                mBlackPaint
            )
            canvas?.rotate(
                360 / 60.toFloat(),
                (measuredWidth / 2).toFloat(),
                (measuredHeight / 2).toFloat()
            )
        }
        canvas?.restore()
    }

    private fun getCurrentTime() {
        val calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
        second = calendar.get(Calendar.SECOND)
    }

    private fun drawHand(canvas: Canvas?) {
        drawSecond(canvas, mRedPaint)
        mBlackPaint.strokeWidth = 10f
        drawMinute(canvas, mBlackPaint)
        mBlackPaint.strokeWidth = 15f
        drawHour(canvas, mBlackPaint)
    }

    private fun drawSecond(canvas: Canvas?, paint: Paint?) {
        val longR = measuredWidth / 2 - 60
        val shortR = 60
        val startX = (measuredWidth / 2 - shortR * sin(second.times(Math.PI / SECOND_DIVIDER))).toFloat()
        val startY = (measuredWidth / 2 + shortR * cos(second.times(Math.PI / SECOND_DIVIDER))).toFloat()
        val endX = (measuredWidth / 2 + longR * sin(second.times(Math.PI / SECOND_DIVIDER))).toFloat()
        val endY = (measuredWidth / 2 - longR * cos(second.times(Math.PI / SECOND_DIVIDER))).toFloat()
        paint?.let { canvas?.drawLine(startX, startY, endX, endY, it) }
    }

    private fun drawMinute(canvas: Canvas?, paint: Paint?) {
        val longR = measuredWidth / 2 - 90
        val shortR = 50
        val startX = (measuredWidth / 2 - shortR * sin((minute*60+second).times(Math.PI / MINUTE_DIVIDER))).toFloat()
        val startY = (measuredWidth / 2 + shortR * cos((minute*60+second).times(Math.PI / MINUTE_DIVIDER))).toFloat()
        val endX = (measuredWidth / 2 + longR * sin((minute*60+second).times(Math.PI / MINUTE_DIVIDER))).toFloat()
        val endY = (measuredWidth / 2 - longR * cos((minute*60+second).times(Math.PI / MINUTE_DIVIDER))).toFloat()
        paint?.let { canvas?.drawLine(startX, startY, endX, endY, it) }
    }

    private fun drawHour(canvas: Canvas?, paint: Paint?) {
        val longR = measuredWidth / 2 - 120
        val shortR = 40
        val startX = (measuredWidth / 2 - shortR * sin((hour*3600+minute*60+second).times(Math.PI / HOUR_DIVIDER))).toFloat()
        val startY = (measuredWidth / 2 + shortR * cos((hour*3600+minute*60+second).times(Math.PI / HOUR_DIVIDER))).toFloat()
        val endX = (measuredWidth / 2 + longR * sin((hour*3600+minute*60+second).times(Math.PI / HOUR_DIVIDER))).toFloat()
        val endY = (measuredWidth / 2 - longR * cos((hour*3600+minute*60+second).times(Math.PI / HOUR_DIVIDER))).toFloat()
        paint?.let { canvas?.drawLine(startX, startY, endX, endY, it) }
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
        private const val SECOND_DIVIDER = 30
        private const val MINUTE_DIVIDER = 1800
        private const val HOUR_DIVIDER = 21600
    }
}
