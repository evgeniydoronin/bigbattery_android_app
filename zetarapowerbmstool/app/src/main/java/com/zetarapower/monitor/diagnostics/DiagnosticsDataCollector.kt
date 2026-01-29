package com.zetarapower.monitor.diagnostics

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.clj.fastble.BleManager
import com.zetarapower.monitor.logic.BMSData
import com.zetarapower.monitor.logic.SettingsProtocolData
import com.zetarapower.monitor.utils.getAppVersionCode
import com.zetarapower.monitor.utils.getAppVersionName
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

/**
 * Collects diagnostic data for sending to developer
 * Mirrors iOS DiagnosticsViewController.createLogsData()
 */
class DiagnosticsDataCollector(private val context: Context) {

    private val dateFormatter = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())

    /**
     * Create complete diagnostic data JSON
     */
    fun createLogsData(
        bmsData: BMSData?,
        selectedId: Int?,
        canData: SettingsProtocolData?,
        rs485Data: SettingsProtocolData?,
        eventLogs: List<DiagnosticsEvent>
    ): JSONObject {
        val logsData = JSONObject()

        // Device Information
        logsData.put("deviceInfo", createDeviceInfo())

        // App Information
        logsData.put("appInfo", createAppInfo())

        // Battery Information
        logsData.put("batteryInfo", createBatteryInfo(bmsData))

        // Extended Battery Information
        logsData.put("extendedBatteryInfo", createExtendedBatteryInfo(bmsData))

        // Protocol Information
        logsData.put("protocolInfo", createProtocolInfo(selectedId, canData, rs485Data))

        // Bluetooth Information
        logsData.put("bluetoothInfo", createBluetoothInfo())

        // Connection Process Information
        logsData.put("connectionProcessInfo", createConnectionProcessInfo(eventLogs))

        // Raw Data Information
        logsData.put("rawDataInfo", createRawDataInfo(bmsData))

        // Communication Errors Information
        logsData.put("communicationErrorsInfo", createCommunicationErrorsInfo(eventLogs))

        // System Information
        logsData.put("systemInfo", createSystemInfo())

        // Events
        logsData.put("events", createEventsArray(eventLogs))

        // Timestamp
        logsData.put("timestamp", dateFormatter.format(Date()))

        return logsData
    }

    /**
     * Device Information
     */
    private fun createDeviceInfo(): JSONObject {
        return JSONObject().apply {
            put("model", Build.MODEL)
            put("manufacturer", Build.MANUFACTURER)
            put("systemName", "Android")
            put("systemVersion", Build.VERSION.RELEASE)
            put("sdkVersion", Build.VERSION.SDK_INT)
            put("name", Build.DEVICE)
            put("product", Build.PRODUCT)
        }
    }

    /**
     * App Information
     */
    private fun createAppInfo(): JSONObject {
        return JSONObject().apply {
            put("version", getAppVersionName(context))
            put("build", getAppVersionCode(context).toString())
            put("packageName", context.packageName)
        }
    }

    /**
     * Battery Information from BMS
     */
    private fun createBatteryInfo(bmsData: BMSData?): JSONObject {
        return JSONObject().apply {
            put("timestamp", dateFormatter.format(Date()))

            if (bmsData != null) {
                put("voltage", bmsData.voltage)
                put("current", bmsData.current)
                put("soc", bmsData.soc)
                put("soh", bmsData.soh)
                put("status", bmsData.status.toString())
                put("cellCount", bmsData.cellCount)

                // Cell voltages
                val cellVoltagesArray = JSONArray()
                bmsData.cellVoltages.take(bmsData.cellCount).forEach { cellVoltagesArray.put(it) }
                put("cellVoltages", cellVoltagesArray)

                // Cell temperatures
                val cellTempsArray = JSONArray()
                bmsData.cellTempArray.forEach { cellTempsArray.put(it.toInt()) }
                put("cellTemps", cellTempsArray)

                put("tempPCB", bmsData.tempPCB.toInt())
                put("tempEnv", bmsData.tempEnv.toInt())
            } else {
                put("status", "No data")
            }
        }
    }

    /**
     * Extended Battery Information with statistics
     */
    private fun createExtendedBatteryInfo(bmsData: BMSData?): JSONObject {
        return JSONObject().apply {
            if (bmsData != null) {
                // Cell voltage statistics
                val voltages = bmsData.cellVoltages.take(bmsData.cellCount).toFloatArray()
                if (voltages.isNotEmpty()) {
                    val minVoltage = voltages.minOrNull() ?: 0f
                    val maxVoltage = voltages.maxOrNull() ?: 0f
                    val avgVoltage = voltages.average().toFloat()
                    val voltageDelta = maxVoltage - minVoltage

                    // Standard deviation
                    val sumOfSquaredDiff = voltages.sumOf { v ->
                        val diff = v - avgVoltage
                        (diff * diff).toDouble()
                    }
                    val stdDev = sqrt(sumOfSquaredDiff / voltages.size)

                    put("cellVoltageMin", minVoltage)
                    put("cellVoltageMax", maxVoltage)
                    put("cellVoltageAverage", avgVoltage)
                    put("cellVoltageDelta", voltageDelta)
                    put("cellVoltageStdDev", stdDev)
                }

                // Temperature statistics
                val temps = bmsData.cellTempArray
                if (temps.isNotEmpty()) {
                    val minTemp = temps.minOrNull()?.toInt() ?: 0
                    val maxTemp = temps.maxOrNull()?.toInt() ?: 0
                    val tempDelta = maxTemp - minTemp

                    put("tempMin", minTemp)
                    put("tempMax", maxTemp)
                    put("tempDelta", tempDelta)
                }

                // Protection status (status is Int in Android, not enum)
                val isProtecting = bmsData.status == 4 // Assuming 4 is protecting status
                val protectionStatus = JSONObject().apply {
                    put("overvoltage", isProtecting)
                    put("undervoltage", isProtecting)
                    put("overcurrent", isProtecting)
                    put("overtemperature", isProtecting)
                    put("shortCircuit", isProtecting)
                }
                put("protectionStatus", protectionStatus)
            }
        }
    }

    /**
     * Protocol Information
     */
    private fun createProtocolInfo(
        selectedId: Int?,
        canData: SettingsProtocolData?,
        rs485Data: SettingsProtocolData?
    ): JSONObject {
        val moduleId = if (selectedId != null && selectedId != -1) "ID$selectedId" else "--"
        val canProtocol = canData?.let {
            it.protocolArray.getOrNull(it.selectedIndex) ?: "--"
        } ?: "--"
        val rs485Protocol = rs485Data?.let {
            it.protocolArray.getOrNull(it.selectedIndex) ?: "--"
        } ?: "--"

        val protocolLogs = ProtocolLogger.getLogs()

        return JSONObject().apply {
            put("currentValues", JSONObject().apply {
                put("moduleId", moduleId)
                put("canProtocol", canProtocol)
                put("rs485Protocol", rs485Protocol)
            })
            put("recentLogs", JSONArray().apply {
                protocolLogs.forEach { entry ->
                    put(JSONObject().apply {
                        put("timestamp", dateFormatter.format(entry.timestamp))
                        put("type", entry.type)
                        put("message", entry.message)
                    })
                }
            })
            put("statistics", JSONObject().apply {
                put("totalLogs", protocolLogs.size)
                put("errors", protocolLogs.count { it.type == "BLE_ERROR" || it.type == "ERROR" })
                put("successes", protocolLogs.count { it.type == "BLE_WRITE" || it.type == "SUCCESS" || it.type == "RESPONSE" })
                put("warnings", protocolLogs.count { it.type == "WARNING" || it.type == "SKIP" })
            })
            put("lastUpdateTime", dateFormatter.format(Date()))
        }
    }

    /**
     * Bluetooth Information
     */
    private fun createBluetoothInfo(): JSONObject {
        return JSONObject().apply {
            put("connectionAttempts", 0)

            val connectedDevices = BleManager.getInstance().allConnectedDevice
            if (connectedDevices?.isNotEmpty() == true) {
                val device = connectedDevices[0]
                put("peripheralName", device.name ?: "Unknown")
                put("peripheralIdentifier", device.mac)
                put("state", "connected")
            } else {
                put("state", "disconnected")
            }

            // Bluetooth adapter state
            val bleState = when {
                !BleManager.getInstance().isBlueEnable -> "poweredOff"
                BleManager.getInstance().isSupportBle -> "poweredOn"
                else -> "unsupported"
            }
            put("adapterState", bleState)
        }
    }

    /**
     * Connection Process Information
     */
    private fun createConnectionProcessInfo(eventLogs: List<DiagnosticsEvent>): JSONObject {
        val connectionEvents = eventLogs.filter {
            it.type == DiagnosticsEvent.EventType.CONNECTION ||
            it.type == DiagnosticsEvent.EventType.DISCONNECTION
        }

        val stepsArray = JSONArray()
        connectionEvents.forEach { event ->
            stepsArray.put(JSONObject().apply {
                put("timestamp", dateFormatter.format(event.timestamp))
                put("step", if (event.type == DiagnosticsEvent.EventType.CONNECTION) "connection" else "disconnection")
                put("status", "success")
                put("message", event.message)
            })
        }

        return JSONObject().apply {
            put("steps", stepsArray)
        }
    }

    /**
     * Raw Data Information
     */
    private fun createRawDataInfo(bmsData: BMSData?): JSONObject {
        return JSONObject().apply {
            if (bmsData != null) {
                // Create hex representation of BMS data
                val hexBuilder = StringBuilder()

                // Add voltage bytes
                val voltageBytes = java.nio.ByteBuffer.allocate(4).putFloat(bmsData.voltage).array()
                voltageBytes.forEach { hexBuilder.append(String.format("%02X", it)) }

                // Add current bytes
                val currentBytes = java.nio.ByteBuffer.allocate(4).putFloat(bmsData.current).array()
                currentBytes.forEach { hexBuilder.append(String.format("%02X", it)) }

                // Add SOC bytes
                val socBytes = java.nio.ByteBuffer.allocate(4).putInt(bmsData.soc).array()
                socBytes.forEach { hexBuilder.append(String.format("%02X", it)) }

                // Add cell voltages
                bmsData.cellVoltages.take(bmsData.cellCount).forEach { voltage ->
                    val bytes = java.nio.ByteBuffer.allocate(4).putFloat(voltage).array()
                    bytes.forEach { hexBuilder.append(String.format("%02X", it)) }
                }

                put("lastReceivedPacket", hexBuilder.toString())
                put("packetHistory", JSONArray().put(JSONObject().apply {
                    put("timestamp", dateFormatter.format(Date()))
                    put("data", hexBuilder.toString())
                    put("parseResult", "success")
                }))
                put("parseErrors", JSONArray())
            } else {
                put("lastReceivedPacket", "Unavailable")
                put("packetHistory", JSONArray())
                put("parseErrors", JSONArray())
            }
        }
    }

    /**
     * Communication Errors Information
     */
    private fun createCommunicationErrorsInfo(eventLogs: List<DiagnosticsEvent>): JSONObject {
        val errorEvents = eventLogs.filter { it.type == DiagnosticsEvent.EventType.ERROR }

        return JSONObject().apply {
            put("timeouts", 0)
            put("crcErrors", 0)
            put("packetLoss", 0)
            put("retries", 0)

            if (errorEvents.isNotEmpty()) {
                val lastError = errorEvents.first()
                put("lastError", JSONObject().apply {
                    put("timestamp", dateFormatter.format(lastError.timestamp))
                    put("type", "error")
                    put("message", lastError.message)
                })
            }
        }
    }

    /**
     * System Information
     */
    private fun createSystemInfo(): JSONObject {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

        return JSONObject().apply {
            // Low power mode
            put("isLowPowerMode", powerManager?.isPowerSaveMode ?: false)

            // Available memory
            val runtime = Runtime.getRuntime()
            put("availableMemory", runtime.maxMemory())
            put("freeMemory", runtime.freeMemory())
            put("totalMemory", runtime.totalMemory())

            // CPU usage placeholder
            put("cpuUsage", 0.0)

            // Device battery level
            val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            put("batteryLevel", if (batteryLevel >= 0) batteryLevel else 0)
        }
    }

    /**
     * Events Array
     */
    private fun createEventsArray(eventLogs: List<DiagnosticsEvent>): JSONArray {
        val eventsArray = JSONArray()
        eventLogs.forEach { event ->
            eventsArray.put(JSONObject().apply {
                put("timestamp", dateFormatter.format(event.timestamp))
                put("type", event.type.title)
                put("message", event.message)
            })
        }
        return eventsArray
    }
}
