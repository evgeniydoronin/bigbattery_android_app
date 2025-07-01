package com.zetarapower.monitor.app

import android.app.Application
import com.bosphere.filelogger.FL
import com.bosphere.filelogger.FLConfig
import com.bosphere.filelogger.FLConst
import com.zetarapower.monitor.BuildConfig
import com.zetarapower.monitor.logic.PowerMonitorBlueTooth


/**
 * Created by juqiu.lt on 2020/7/12.
 */
class PowerMonitorApp : Application() {


    companion object {
        lateinit var  inst: PowerMonitorApp
            private set
    }

    /**
     *
     */
    override fun onCreate() {
        super.onCreate()
        inst = this
        init()
    }


    private fun init() {
        PowerMonitorBlueTooth.INSTANCE.init(this)
        initLogger()
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG){
            FL.init(
                FLConfig.Builder(this)
                    .minLevel(FLConst.Level.V)
                    .logToFile(true)
                    .dir(getExternalFilesDir("zetara_file_logger"))
                    .retentionPolicy(FLConst.RetentionPolicy.FILE_COUNT)
                    .build()
            )
            FL.setEnabled(true)
        }
    }
}