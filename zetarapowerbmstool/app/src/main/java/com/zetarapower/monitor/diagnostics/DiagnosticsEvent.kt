package com.zetarapower.monitor.diagnostics

import java.util.*

/**
 * Model for diagnostic event log entry
 * Mirrors iOS DiagnosticsEvent
 */
data class DiagnosticsEvent(
    val timestamp: Date,
    val type: EventType,
    val message: String
) {
    enum class EventType(val title: String) {
        CONNECTION("Connection"),
        DISCONNECTION("Disconnection"),
        DATA_UPDATE("Data Update"),
        ERROR("Error")
    }
}
