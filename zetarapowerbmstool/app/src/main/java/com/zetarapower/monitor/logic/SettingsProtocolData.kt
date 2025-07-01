package com.zetarapower.monitor.logic

data class SettingsProtocolData(
    val selectedIndex: Int,
    val protocolArray: ArrayList<String>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SettingsProtocolData

        if (selectedIndex != other.selectedIndex) return false
        if (protocolArray != other.protocolArray) return false

        return true
    }

    override fun hashCode(): Int {
        var result = selectedIndex
        result = 31 * result + protocolArray.hashCode()
        return result
    }
}