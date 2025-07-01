package com.zetarapower.monitor.ui.fragment

import com.clj.fastble.data.BleDevice
import com.zetarapower.monitor.bluetooth.ZetaraBleUUID


/**
 *
 */

/**
 *
 */
interface ConnectCallback {
    fun onConnected(bleDevice: BleDevice,  uuid: ZetaraBleUUID?)
    fun onDisconnected(bleDevice: BleDevice)
}