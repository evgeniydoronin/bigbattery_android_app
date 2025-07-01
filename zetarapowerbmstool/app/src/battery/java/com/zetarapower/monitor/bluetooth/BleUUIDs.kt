package com.zetarapower.monitor.bluetooth

import java.util.*

/**
 *
 */
object BleUUIDs {
    val serviceUUIDArray = arrayOf(
      ZetaraBleUUID(
            UUID.fromString("00001000-0000-1000-8000-00805f9b34fb"),
            UUID.fromString("00001001-0000-1000-8000-00805f9b34fb"),
            UUID.fromString("00001002-0000-1000-8000-00805f9b34fb")),
        ZetaraBleUUID(
            UUID.fromString("00001006-0000-1000-8000-00805f9b34fb"),
            UUID.fromString("00001008-0000-1000-8000-00805f9b34fb"),
            UUID.fromString("00001007-0000-1000-8000-00805f9b34fb"))
    )
}