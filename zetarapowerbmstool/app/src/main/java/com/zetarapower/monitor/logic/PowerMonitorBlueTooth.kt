package com.zetarapower.monitor.logic

import android.app.Application
import android.content.Context
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig
import com.zetarapower.monitor.app.PowerMonitorApp
import com.zetarapower.monitor.bluetooth.BleUUIDs
import java.util.*


/**
 * Created by juqiu.lt on 2020/7/12.
 */
class PowerMonitorBlueTooth private constructor(context: Context){


    private var connectedDev: BleDevice? = null


    /**
     *
     */
    fun disconnect(bleDevice: BleDevice) {
        BleManager.getInstance().disconnect(bleDevice)
    }


    /**
     *
     */
    fun connect(bleDevice: BleDevice, bleGattCallback: BleGattCallback){
        BleManager.getInstance().connect(bleDevice, bleGattCallback)
    }


    /**
     *
     */
    fun init(context: Application) {
        var scanRuleConfig = BleScanRuleConfig.Builder().setScanTimeOut(6000).build()
        BleManager.getInstance().init(context, scanRuleConfig)
        BleManager.getInstance().maxConnectCount = 2
    }



    companion object {
        const val TAG = "PowerMonitorBlueTooth"
        val INSTANCE : PowerMonitorBlueTooth by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            PowerMonitorBlueTooth(PowerMonitorApp.inst)
        }
    }
}