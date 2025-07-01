package com.zetarapower.monitor.logic


/**
 *
 */
data class BMSData (
    val voltage: Float,           // 电压
    val current: Float,           // 电流
    var cellVoltages: FloatArray = FloatArray(32), // 电池单元电压 （32）,
    val cellCount: Int = 8,
    val tempPCB : Byte = 0,            // pcb 温度
    val tempEnv : Short = 0,
    val cellTempArray: ByteArray = ByteArray(4),
    val soc: Int = 0,                   // 剩余电量
    val soh: Int = 100,
    val status: Int = 0,
    val warningStatus: Int = 0
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BMSData

        if (voltage != other.voltage) return false
        if (current != other.current) return false
        if (!cellVoltages.contentEquals(other.cellVoltages)) return false
        if (cellCount != other.cellCount) return false
        if (tempPCB != other.tempPCB) return false
        if (tempEnv != other.tempEnv) return false
        if (!cellTempArray.contentEquals(other.cellTempArray)) return false
        if (soc != other.soc) return false
        if (soh != other.soh) return false
        if (status != other.status) return false
        if (warningStatus != other.warningStatus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = voltage.hashCode()
        result = 31 * result + current.hashCode()
        result = 31 * result + cellVoltages.contentHashCode()
        result = 31 * result + cellCount
        result = 31 * result + tempPCB
        result = 31 * result + tempEnv
        result = 31 * result + cellTempArray.contentHashCode()
        result = 31 * result + soc
        result = 31 * result + soh
        result = 31 * result + status
        result = 31 * result + warningStatus
        return result
    }
}


