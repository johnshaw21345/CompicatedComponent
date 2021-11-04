package com.bytedance.compicatedcomponent.homework

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

/**
 *  author : neo
 *  time   : 2021/10/25
 *  desc   :
 */
class ClockViewTouchable @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val FULL_ANGLE = 360

        private const val CUSTOM_ALPHA = 140
        private const val FULL_ALPHA = 255

        private const val POINTER_TYPE_SECOND = 2
        private const val POINTER_TYPE_MINUTES = 1
        private const val POINTER_TYPE_HOURS = 0

        private const val DEFAULT_PRIMARY_COLOR: Int = Color.WHITE
        private const val DEFAULT_SECONDARY_COLOR: Int = Color.LTGRAY

        private const val DEFAULT_DEGREE_STROKE_WIDTH = 0.010f
        private const val DEFAULT_HOURS_STROKE_WIDTH = 0.2f

        private const val RIGHT_ANGLE = 90

        private const val UNIT_DEGREE = (6 * Math.PI / 180).toFloat() // 一个小格的度数
    }

    private var panelRadius = 200.0f // 表盘半径

    private var hourPointerLength = 0f // 指针长度

    private var minutePointerLength = 0f
    private var secondPointerLength = 0f

    private var resultWidth = 0
    private  var centerX: Int = 0
    private  var centerY: Int = 0
    private  var radius: Int = 0

    private var degreesColor = 0
    private var hoursValueColor = 0

    var nowSeconds = 0
    var nowMinutes = 0
    var nowHours = 0

    private var touchedNeedle = 0

    private val needlePaint: Paint

    init {
        degreesColor = DEFAULT_PRIMARY_COLOR
        hoursValueColor = DEFAULT_PRIMARY_COLOR
        needlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        needlePaint.style = Paint.Style.FILL_AND_STROKE
        needlePaint.strokeCap = Paint.Cap.ROUND

        val calendar: Calendar = Calendar.getInstance()
        val now: Date = calendar.time
        nowHours = now.hours
        nowMinutes = now.minutes
        nowSeconds = now.seconds


        class RefreshTimer : TimerTask(){
            override fun run(){
                nowSeconds++

                if (nowSeconds==60) {
                    nowSeconds = 0
                    nowMinutes++
                    if (nowMinutes == 60){
                        nowMinutes = 0
                        nowHours++
                        if(nowHours == 24)
                            nowHours=0
                    }
                }
                invalidate()
            }
        }

        val refresh = RefreshTimer()

        Timer().schedule(refresh,Date(),1000)


    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size: Int
        val width = measuredWidth
        val height = measuredHeight
        val widthWithoutPadding = width - paddingLeft - paddingRight
        val heightWithoutPadding = height - paddingTop - paddingBottom
        size = if (widthWithoutPadding > heightWithoutPadding) {
            heightWithoutPadding
        } else {
            widthWithoutPadding
        }
        setMeasuredDimension(size + paddingLeft + paddingRight, size + paddingTop + paddingBottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        resultWidth = if (height > width) width else height
        val halfWidth = resultWidth / 2
        centerX = halfWidth
        centerY = halfWidth
        radius = halfWidth
        panelRadius = radius.toFloat()
        hourPointerLength = panelRadius - 350
        minutePointerLength = panelRadius - 250
        secondPointerLength = panelRadius - 150
        drawDegrees(canvas)
        drawHoursValues(canvas)
        drawNumbers(canvas)
        drawNeedles(canvas)
        

        // todo 1: 每一秒刷新一次，让指针动起来



    }

    private fun drawDegrees(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = resultWidth * DEFAULT_DEGREE_STROKE_WIDTH
            color = degreesColor
        }
        val rPadded: Int = centerX - (resultWidth * 0.01f).toInt()
        val rEnd: Int = centerX - (resultWidth * 0.05f).toInt()
        var i = 0
        while (i < FULL_ANGLE) {
            if (i % RIGHT_ANGLE != 0 && i % 15 != 0) {
                paint.alpha = CUSTOM_ALPHA
            } else {
                paint.alpha = FULL_ALPHA
            }
            val startX = (centerX + rPadded * cos(Math.toRadians(i.toDouble())))
            val startY = (centerX - rPadded * sin(Math.toRadians(i.toDouble())))
            val stopX = (centerX + rEnd * cos(Math.toRadians(i.toDouble())))
            val stopY = (centerX - rEnd * sin(Math.toRadians(i.toDouble())))
            canvas.drawLine(
                startX.toFloat(),
                startY.toFloat(),
                stopX.toFloat(),
                stopY.toFloat(),
                paint
            )
            i += 6
        }
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */
    private fun drawHoursValues(canvas: Canvas) {
        // Default Color:
        // - hoursValuesColor
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            strokeCap = Paint.Cap.ROUND
            strokeWidth = resultWidth * DEFAULT_HOURS_STROKE_WIDTH
            color = hoursValueColor
            textAlign = Paint.Align.CENTER
        }
        val rEnd: Int = centerX - (resultWidth * 0.12f).toInt()
        var i = 0
        var num = 3
        while (i < FULL_ANGLE) {
                paint.alpha = FULL_ALPHA
                paint.textSize = 100F
            val posX = (centerX + rEnd * cos(Math.toRadians(i.toDouble())))
            val posY = (centerX + paint.textSize*0.8/2 - rEnd * sin(Math.toRadians(i.toDouble())))
            canvas.drawText(
                num.toString(),
                posX.toFloat(),
                posY.toFloat(),
                paint
            )
            i += 30
            num --
            if (num==0){num = 12}
        }
    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     */
    private fun drawNeedles(canvas: Canvas) {

        // 画秒针
        drawPointer(canvas, POINTER_TYPE_SECOND, nowSeconds)
        // 画分针
        // todo 2: 画分针
        drawPointer(canvas, POINTER_TYPE_MINUTES, nowMinutes)

        // 画时针
        val part = nowMinutes / 12
        drawPointer(canvas, POINTER_TYPE_HOURS, 5 * nowHours + part)
    }


    private fun drawPointer(canvas: Canvas, pointerType: Int, value: Int) {
        val degree: Float
        var pointerHeadXY = FloatArray(2)
        needlePaint.strokeWidth = resultWidth * DEFAULT_DEGREE_STROKE_WIDTH
        when (pointerType) {
            POINTER_TYPE_HOURS -> {
                degree = value * UNIT_DEGREE
                needlePaint.color = Color.RED
                pointerHeadXY = getPointerHeadXY(hourPointerLength, degree)
            }
            POINTER_TYPE_MINUTES -> {
                degree = value * UNIT_DEGREE
                needlePaint.color = Color.YELLOW
                pointerHeadXY = getPointerHeadXY(minutePointerLength, degree)
            }
            POINTER_TYPE_SECOND -> {
                degree = value * UNIT_DEGREE
                needlePaint.color = Color.GREEN
                pointerHeadXY = getPointerHeadXY(secondPointerLength, degree)
            }
        }
        canvas.drawLine(
            centerX.toFloat(), centerY.toFloat(),
            pointerHeadXY[0], pointerHeadXY[1], needlePaint
        )
    }

    private fun getPointerHeadXY(pointerLength: Float, degree: Float): FloatArray {
        val xy = FloatArray(2)
        xy[0] = centerX + pointerLength * sin(degree)
        xy[1] = centerY - pointerLength * cos(degree)
        return xy
    }

    private fun drawNumbers(canvas: Canvas){

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            strokeCap = Paint.Cap.ROUND
            strokeWidth = resultWidth * DEFAULT_HOURS_STROKE_WIDTH
            color = DEFAULT_SECONDARY_COLOR
            textAlign = Paint.Align.CENTER
            textSize = 80F
        }

        val posX = (centerX)
        val posY = (centerX + (resultWidth * 0.15f).toInt())

        val secondsText: String = if (nowSeconds>=10) {
            "$nowSeconds"
        }
        else{
            "0${nowSeconds}"
        }

        val minutesText: String = if (nowMinutes>=10) {
            "$nowMinutes"
        }
        else{
            "0${nowMinutes}"
        }

        val hoursText: String = if (nowHours>=10) {
            "$nowHours"
        }
        else{
            "0${nowHours}"
        }

        val timeText = "${hoursText}:${minutesText}:${secondsText}"

        canvas.drawText(
            timeText,
            posX.toFloat(),
            posY.toFloat(),
            paint
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when(event?.action) {

            MotionEvent.ACTION_DOWN-> {

                val xm = getPointerHeadXY(minutePointerLength, nowMinutes * UNIT_DEGREE)[0]
                val ym = getPointerHeadXY(minutePointerLength, nowMinutes * UNIT_DEGREE)[1]

                val xs = getPointerHeadXY(secondPointerLength, nowSeconds * UNIT_DEGREE)[0]
                val ys = getPointerHeadXY(secondPointerLength, nowSeconds * UNIT_DEGREE)[1]

                val xh = getPointerHeadXY(
                    hourPointerLength,
                    (5 * nowHours + nowMinutes / 12) * UNIT_DEGREE
                )[0]
                val yh = getPointerHeadXY(
                    hourPointerLength,
                    (5 * nowHours + nowMinutes / 12) * UNIT_DEGREE
                )[1]

                val xtouch = event.x
                val ytouch = event.y

                    if (xtouch > xm - 40 && xtouch  < xm + 40 && ytouch > ym - 40 && ytouch < ym + 40) {
                        touchedNeedle = POINTER_TYPE_MINUTES
                        Log.i("->", "${xtouch},${ytouch},MINUTES")
                    }

                    if (xtouch > xs - 40 && xtouch < xs + 40 && ytouch > ys - 40 && ytouch < ys + 40) {
                        touchedNeedle = POINTER_TYPE_SECOND
                        Log.i("->", "${xtouch},${ytouch},SECONDS")
                    }

                    if (xtouch > xh - 40 && xtouch < xh + 40 && ytouch > yh - 40 && ytouch < yh + 40) {
                        touchedNeedle = POINTER_TYPE_HOURS
                        Log.i("->", "${xtouch},${ytouch},HOURS")
                    }




            }

            MotionEvent.ACTION_MOVE-> {
                val xtouch = event.x
                val ytouch = event.y

                val offsetX = xtouch-centerX
                val offsetY = ytouch-centerY
                val degreeD = Math.toDegrees(atan((offsetX)/(offsetY)).toDouble())
                var degreeT = 0.0

                if (offsetX > 0 && offsetY <= 0 ){
                    degreeT = -degreeD
                }

                if (offsetX > 0 && offsetY > 0 ){
                    degreeT = 180-degreeD
                }

                if (offsetX < 0 && offsetY > 0 ){
                    degreeT = 180-degreeD
                }

                if (offsetX < 0 && offsetY < 0 ){
                    degreeT = 360-degreeD
                }

                when(touchedNeedle){
                    POINTER_TYPE_MINUTES->{
                        nowMinutes = (degreeT/6).toInt()

                    }
                    POINTER_TYPE_SECOND->{
                        nowSeconds = (degreeT/6).toInt()

                    }
                    POINTER_TYPE_HOURS->{
                        nowHours = (degreeT/30).toInt()

                    }


                }

                invalidate()

                        Log.i("->", "${offsetX},${offsetY},${degreeD},${touchedNeedle}")




                }
                
                 MotionEvent.ACTION_UP-> {
                    touchedNeedle = 3
                }
            }


        return true

    }


}
