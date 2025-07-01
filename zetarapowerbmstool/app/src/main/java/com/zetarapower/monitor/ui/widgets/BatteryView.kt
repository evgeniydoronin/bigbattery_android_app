package com.zetarapower.monitor.ui.widgets

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.zetarapower.monitor.R


/**
 *
 */
class BatteryView @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mPower = 0
    private var isCharging = false

    private var battery_full: Drawable? = null
    private var battery_mask: Drawable? = null
    private var battery_low: Drawable? = null


    init {
        battery_mask =  resources.getDrawable(R.drawable.battery_bg)
        battery_full =  resources.getDrawable(R.drawable.battery_full)
        battery_low  = resources.getDrawable(R.drawable.battery_low)
    }


    /**
     *
     */
    fun setPower(power: Int)  {
        mPower = power
        if (mPower >= 100 || mPower < 0){
            mPower = 100
        }
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var marginTop = (height* 10.5/177).toInt()
        var marginBottom  = (height* 6.5/177).toInt()

        var batteryPowerHeight = height-marginBottom-marginTop
        var batterPowerTop = marginTop + ((100-mPower)/100.0*batteryPowerHeight).toInt()

        if (mPower <= 10){
            battery_low?.setBounds(0, batterPowerTop, width, height - marginBottom)
            battery_low?.draw(canvas)
        }else{
            battery_full?.setBounds(0, batterPowerTop, width, height - marginBottom)
            battery_full?.draw(canvas)
        }

        battery_mask?.setBounds(0, 0, width, height)
        battery_mask?.draw(canvas)
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
}