package com.zetarapower.monitor.diagnostics

import java.util.*

/**
 * In-memory logger for protocol operations
 * Collects logs that will be included in DiagnosticsDataCollector output
 */
object ProtocolLogger {
    private val logs = mutableListOf<ProtocolLogEntry>()
    private const val MAX_LOGS = 50

    @Synchronized
    fun log(type: String, message: String) {
        logs.add(0, ProtocolLogEntry(Date(), type, message))
        if (logs.size > MAX_LOGS) {
            logs.removeAt(logs.lastIndex)
        }
    }

    @Synchronized
    fun getLogs(): List<ProtocolLogEntry> = logs.toList()

    @Synchronized
    fun clear() = logs.clear()
}

data class ProtocolLogEntry(
    val timestamp: Date,
    val type: String,
    val message: String
)
