package com.zetarapower.monitor.ui.widgets

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.zetarapower.monitor.R
import kotlin.math.cos
import kotlin.math.sin


/**
 *
 */
class DashboardView @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mRadius  = 0
    private val mStartAngle = 180 // 起始角度
    private val mSweepAngle = 180 // 绘制角度

    // Set In Attribute
    private val mMin: Int      /* 最小值 */
    private val mMax: Int      // 最大值
    private val mSection: Int  // 值域（mMax-mMin）等分份数

    private var mPortion = 0
    //

    private val mHeaderText = "" // 表头
    private var mRealTimeValue = 0f// 实时读数

    private val isShowValue = true // 是否显示实时读数
    private var mStrokeWidth = 0 // 画笔宽度 = 0

    private var mLength1 = 0 // 长刻度的相对圆弧的长度 = 0
    private var mLength2 = 0 // 刻度读数顶部的相对圆弧的长度 = 0

    private var mPLRadius = 0 // 指针长半径 = 0
    private var mPSRadius = 0 // 指针短半径 = 0


    private var mPadding = 0
    private var mCenterX = 0f
    private var mCenterY = 0f // 圆心坐标 = 0f
    private var mPaint: Paint
    private var mRectFArc: RectF
    private var mPath: Path
    private var mRectFInnerArc: RectF
    private var mRectText: Rect
    private var mTexts: Array<String?>

    private var mColor:Int
    
    init {
        //
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DashboardView)
        mMin = typedArray.getInt(R.styleable.DashboardView_dash_board_min, 0)
        mMax = typedArray.getInt(R.styleable.DashboardView_dash_board_max, 100)
        mColor = typedArray.getColor(R.styleable.DashboardView_dash_board_line_color,
            Color.CYAN
        )

        typedArray.recycle()

        mSection = 10
        mPortion = (mMax - mMin)/mSection
        mRealTimeValue = (mMax-mMin)/2f


        mStrokeWidth = dp2px(1)
        mLength1 = dp2px(8) + mStrokeWidth
        mLength2 = mLength1 + dp2px(2)

        mPSRadius = dp2px(10)
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.strokeCap = Paint.Cap.ROUND

        mRectFArc = RectF()
        mPath = Path()

        mRectFInnerArc = RectF()
        mRectText = Rect()

        mTexts = arrayOfNulls(mSection + 1) // 需要显示mSection + 1个刻度读数
        for (i in mTexts.indices) {
            val n = (mMax - mMin) / mSection
            mTexts[i] = (mMin + i * n).toString()
        }
    }


    /**
     *
     */
    var realTimeValue: Float
        get() = mRealTimeValue
        set(realTimeValue) {
            if (mRealTimeValue == realTimeValue || realTimeValue < mMin || realTimeValue > mMax) {
                return
            }
            mRealTimeValue = realTimeValue
            postInvalidate()
        }
    
    

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mPadding = paddingLeft.coerceAtLeast(paddingTop)
            .coerceAtLeast(paddingRight.coerceAtLeast(paddingBottom))
        
        setPadding(mPadding, mPadding, mPadding, mPadding)
        
        val width = resolveSize(dp2px(200), widthMeasureSpec)
        mRadius = (width - mPadding * 2 - mStrokeWidth * 2) / 2

        mPaint.textSize = sp2px(16).toFloat()
        if (isShowValue) { // 显示实时读数，View高度增加字体高度3倍
            mPaint.getTextBounds("0", 0, "0".length, mRectText)
        } else {
            mPaint.getTextBounds("0", 0, 0, mRectText)
        }
        // 由半径+指针短半径+实时读数文字高度确定的高度
        val height1 = mRadius + mStrokeWidth * 2 + mPSRadius + mRectText.height() * 3
        // 由起始角度确定的高度
        val point1 = getCoordinatePoint(mRadius, mStartAngle.toFloat())
        
        // 由结束角度确定的高度
        val point2 =
            getCoordinatePoint(mRadius, mStartAngle + mSweepAngle.toFloat())
        
        // 取最大值
        val max = height1.toFloat()
            .coerceAtLeast((point1[1] + mRadius + mStrokeWidth * 2).coerceAtLeast(point2[1] + mRadius + mStrokeWidth * 2))
            .toInt()
        
        setMeasuredDimension(width, max + paddingTop + paddingBottom)
        mCenterY = measuredWidth / 2f
        mCenterX = mCenterY
        mRectFArc[paddingLeft + mStrokeWidth.toFloat(), paddingTop + mStrokeWidth.toFloat(), measuredWidth - paddingRight - mStrokeWidth.toFloat()] =
            measuredWidth - paddingBottom - mStrokeWidth
                .toFloat()
        mPaint.textSize = sp2px(10).toFloat()
        mPaint.getTextBounds("0", 0, "0".length, mRectText)
        mRectFInnerArc[paddingLeft + mLength2 + mRectText.height().toFloat(), paddingTop + mLength2 + mRectText.height().toFloat(), measuredWidth - paddingRight - mLength2 - mRectText.height().toFloat()] =
            measuredWidth - paddingBottom - mLength2 - mRectText.height()
                .toFloat()
        mPLRadius = mRadius - (mLength2 + mRectText.height() + dp2px(5))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        /**
         * 画圆弧
         */
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mStrokeWidth.toFloat()
        mPaint.color = ContextCompat.getColor(context, android.R.color.holo_orange_dark)
        canvas.drawArc(mRectFArc, mStartAngle.toFloat(), mSweepAngle.toFloat(), false, mPaint)


        /**
         * 画长刻度
         * 画好起始角度的一条刻度后通过canvas绕着原点旋转来画剩下的长刻度
         */
        val cos =
            cos(Math.toRadians(mStartAngle - 180.toDouble()))
        val sin =
            sin(Math.toRadians(mStartAngle - 180.toDouble()))
        val x0 = (mPadding + mStrokeWidth + mRadius * (1 - cos)).toFloat()
        val y0 = (mPadding + mStrokeWidth + mRadius * (1 - sin)).toFloat()
        val x1 =
            (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1) * cos).toFloat()
        val y1 =
            (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1) * sin).toFloat()
        canvas.save()
        canvas.drawLine(x0, y0, x1, y1, mPaint)
        var angle = mSweepAngle * 1f / mSection
        for (i in 0 until mSection) {
            canvas.rotate(angle, mCenterX, mCenterY)
            canvas.drawLine(x0, y0, x1, y1, mPaint)
        }
        canvas.restore()



        /**
         * 画短刻度
         * 同样采用canvas的旋转原理
         */
        canvas.save()
        mPaint.strokeWidth = 1f
        val x2 =
            (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1 / 2f) * cos).toFloat()
        val y2 =
            (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1 / 2f) * sin).toFloat()
        canvas.drawLine(x0, y0, x2, y2, mPaint)
        angle = mSweepAngle * 1f / (mSection * mPortion)
        for (i in 1 until mSection * mPortion) {
            canvas.rotate(angle, mCenterX, mCenterY)
            if (i % mPortion == 0) { // 避免与长刻度画重合
                continue
            }
            canvas.drawLine(x0, y0, x2, y2, mPaint)
        }
        canvas.restore()
        /**
         * 画长刻度读数
         * 添加一个圆弧path，文字沿着path绘制
         */
        mPaint.textSize = sp2px(10).toFloat()
        mPaint.textAlign = Paint.Align.LEFT
        mPaint.style = Paint.Style.FILL
        for (i in mTexts.indices) {
            mPaint.getTextBounds(mTexts[i], 0, mTexts[i]!!.length, mRectText)
            // 粗略把文字的宽度视为圆心角2*θ对应的弧长，利用弧长公式得到θ，下面用于修正角度
            val θ = (180 * mRectText.width() / 2 /
                    (Math.PI * (mRadius - mLength2 - mRectText.height()))).toFloat()
            mPath.reset()
            mPath.addArc(
                mRectFInnerArc,
                mStartAngle + i * (mSweepAngle / mSection) - θ,  // 正起始角度减去θ使文字居中对准长刻度
                mSweepAngle
                    .toFloat()
            )
            canvas.drawTextOnPath(mTexts[i]!!, mPath, 0f, 0f, mPaint)
        }

        /**
         * 画表头
         * 没有表头就不画
         */
        if (!TextUtils.isEmpty(mHeaderText)) {
            mPaint.textSize = sp2px(14).toFloat()
            mPaint.textAlign = Paint.Align.CENTER
            mPaint.getTextBounds(mHeaderText, 0, mHeaderText.length, mRectText)
            canvas.drawText(mHeaderText, mCenterX, mCenterY / 2f + mRectText.height(), mPaint)
        }
        /**
         * 画指针
         */
        val θ =
            mStartAngle + mSweepAngle * (mRealTimeValue - mMin) / (mMax - mMin).toFloat() // 指针与水平线夹角

        val d = dp2px(5) // 指针由两个等腰三角形构成，d为共底边长的一半
        mPath.reset()

        val p1 = getCoordinatePoint(d, θ - 90)
        mPath.moveTo(p1[0], p1[1])
        val p2 = getCoordinatePoint(mPLRadius, θ)
        mPath.lineTo(p2[0], p2[1])
        val p3 = getCoordinatePoint(d, θ + 90)
        mPath.lineTo(p3[0], p3[1])
        val p4 = getCoordinatePoint(mPSRadius, θ - 180)
        mPath.lineTo(p4[0], p4[1])
        mPath.close()
        canvas.drawPath(mPath, mPaint)

        /**
         * 画指针围绕的镂空圆心
         */
        mPaint.color = Color.WHITE
        canvas.drawCircle(mCenterX, mCenterY, dp2px(2).toFloat(), mPaint)
        /**
         * 画实时度数值
         */
        if (isShowValue) {
            mPaint.textSize = sp2px(16).toFloat()
            mPaint.textAlign = Paint.Align.CENTER
            mPaint.color = mColor
            val value = "$mRealTimeValue ℃"
            mPaint.getTextBounds(value, 0, value.length, mRectText)
            canvas.drawText(
                value,
                mCenterX,
                mCenterY + mPSRadius + mRectText.height() * 2,
                mPaint
            )
        }
    }

    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    private fun sp2px(sp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }


    private fun getCoordinatePoint(radius: Int, angle: Float): FloatArray {
        val point = FloatArray(2)
        var arcAngle = Math.toRadians(angle.toDouble()) //将角度转换为弧度
        if (angle < 90) {
            point[0] = (mCenterX + cos(arcAngle) * radius).toFloat()
            point[1] = (mCenterY + sin(arcAngle) * radius).toFloat()
        } else if (angle == 90f) {
            point[0] = mCenterX
            point[1] = mCenterY + radius
        } else if (angle > 90 && angle < 180) {
            arcAngle = Math.PI * (180 - angle) / 180.0
            point[0] = (mCenterX - cos(arcAngle) * radius).toFloat()
            point[1] = (mCenterY + sin(arcAngle) * radius).toFloat()
        } else if (angle == 180f) {
            point[0] = mCenterX - radius
            point[1] = mCenterY
        } else if (angle > 180 && angle < 270) {
            arcAngle = Math.PI * (angle - 180) / 180.0
            point[0] = (mCenterX - cos(arcAngle) * radius).toFloat()
            point[1] = (mCenterY - sin(arcAngle) * radius).toFloat()
        } else if (angle == 270f) {
            point[0] = mCenterX
            point[1] = mCenterY - radius
        } else {
            arcAngle = Math.PI * (360 - angle) / 180.0
            point[0] = (mCenterX + cos(arcAngle) * radius).toFloat()
            point[1] = (mCenterY - sin(arcAngle) * radius).toFloat()
        }
        return point
    }
}