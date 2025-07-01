package com.zetarapower.monitor.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.zetarapower.monitor.R
import com.zetarapower.monitor.app.PowerMonitorApp


/**
 *
 */
fun Activity.openFragment(
    fragment: Fragment,
    containerViewId: Int,
    addToBackStack: Boolean
) {
    this as AppCompatActivity
    supportFragmentManager.beginTransaction().apply {
        add(containerViewId, fragment)
        if (addToBackStack) addToBackStack(fragment::class.java.simpleName)
        commit()
    }
}

fun Fragment.dp2px(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}

/**
 *
 */
fun Fragment.getAppVersionName(context: Context?): String?{
    if (context == null){
        return null
    }
    val manager: PackageManager = context.packageManager
    return try {
        val info: PackageInfo = manager.getPackageInfo(context.packageName, 0)
        info.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }
}

fun Fragment.showToast(content: String){
    Toast.makeText(
        PowerMonitorApp.inst,
        content,
        Toast.LENGTH_LONG
    ).show()
}

fun Fragment.showToast(stringId: Int){
    Toast.makeText(
        PowerMonitorApp.inst,
        PowerMonitorApp.inst.getString(stringId),
        Toast.LENGTH_LONG
    ).show()
}

fun Activity.showAlertDialog(message: String, onOkayClicked: () -> Unit) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Device Not Connected")
    builder.setMessage(message)
        .setPositiveButton("Okay") { dialog, _ ->
            dialog.dismiss()
            onOkayClicked.invoke()
        }
    val alertDialog = builder.create()
    alertDialog.show()
}