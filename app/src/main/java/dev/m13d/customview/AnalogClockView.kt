package dev.m13d.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class AnalogClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var mBlackPaint: Paint
    private lateinit var mRedPaint: Paint
    private lateinit var mBlackPaint2: Paint

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

    companion object {
        private const val DEFAULT_WIDTH = 200 
    }
}
