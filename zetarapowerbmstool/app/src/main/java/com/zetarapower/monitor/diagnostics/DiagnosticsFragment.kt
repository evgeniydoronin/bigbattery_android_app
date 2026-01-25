package com.zetarapower.monitor.diagnostics

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clj.fastble.BleManager
import com.zetarapower.monitor.R
import com.zetarapower.monitor.logic.BMSData
import com.zetarapower.monitor.ui.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Diagnostics screen for collecting and sending diagnostic data
 * Mirrors iOS DiagnosticsViewController
 */
class DiagnosticsFragment : Fragment() {

    private var mainViewModel: MainViewModel? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var sendLogsButton: Button
    private lateinit var backButton: ImageButton

    private val adapter = DiagnosticsAdapter()
    private val eventLogs = mutableListOf<DiagnosticsEvent>()
    private val dataCollector by lazy { DiagnosticsDataCollector(requireContext()) }

    private var currentBmsData: BMSData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = activity?.let { ViewModelProviders.of(it)[MainViewModel::class.java] }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_diagnostics, container, false)

        initViews(root)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Add initial event
        addEvent(DiagnosticsEvent.EventType.CONNECTION, "Diagnostics screen launched")

        return root
    }

    override fun onResume() {
        super.onResume()

        // Check connection status
        val connectedDevices = BleManager.getInstance().allConnectedDevice
        if (connectedDevices?.isNotEmpty() == true) {
            val deviceName = connectedDevices[0].name ?: "Unknown device"
            addEvent(DiagnosticsEvent.EventType.CONNECTION, "Device connected: $deviceName")
        } else {
            addEvent(DiagnosticsEvent.EventType.CONNECTION, "No device connected")
        }

        updateUI()
    }

    private fun initViews(root: View) {
        recyclerView = root.findViewById(R.id.recyclerView)
        sendLogsButton = root.findViewById(R.id.sendLogsButton)
        backButton = root.findViewById(R.id.backButton)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        sendLogsButton.setOnClickListener {
            sendLogs()
        }
    }

    private fun observeViewModel() {
        // BMS Data observer
        mainViewModel?.data?.observe(viewLifecycleOwner, Observer<BMSData> { data ->
            currentBmsData = data
            addEvent(DiagnosticsEvent.EventType.DATA_UPDATE, "New battery data received")
            updateUI()
        })
    }

    /**
     * Add event to log
     */
    private fun addEvent(type: DiagnosticsEvent.EventType, message: String) {
        val event = DiagnosticsEvent(Date(), type, message)
        eventLogs.add(0, event) // Add to beginning

        // Limit to 100 events
        if (eventLogs.size > 100) {
            eventLogs.removeAt(eventLogs.lastIndex)
        }
    }

    /**
     * Update UI with current data
     */
    private fun updateUI() {
        val items = mutableListOf<DiagnosticsAdapter.DiagnosticsItem>()

        // Device Information Section
        items.add(DiagnosticsAdapter.DiagnosticsItem.SectionHeader(
            getString(R.string.diagnostics_section_device)
        ))

        val connectedDevices = BleManager.getInstance().allConnectedDevice
        val deviceName = if (connectedDevices?.isNotEmpty() == true) {
            connectedDevices[0].name ?: "Unknown device"
        } else {
            "No device connected"
        }
        items.add(DiagnosticsAdapter.DiagnosticsItem.Parameter(
            getString(R.string.diagnostics_device_name),
            deviceName
        ))

        // Battery Information Section
        items.add(DiagnosticsAdapter.DiagnosticsItem.SectionHeader(
            getString(R.string.diagnostics_section_battery)
        ))

        val isConnected = connectedDevices?.isNotEmpty() == true
        val bmsData = currentBmsData

        items.add(DiagnosticsAdapter.DiagnosticsItem.Parameter(
            getString(R.string.diagnostics_voltage),
            if (isConnected && bmsData != null) String.format("%.2f V", bmsData.voltage) else "--"
        ))
        items.add(DiagnosticsAdapter.DiagnosticsItem.Parameter(
            getString(R.string.diagnostics_current),
            if (isConnected && bmsData != null) String.format("%.2f A", bmsData.current) else "--"
        ))
        items.add(DiagnosticsAdapter.DiagnosticsItem.Parameter(
            getString(R.string.diagnostics_soc),
            if (isConnected && bmsData != null) "${bmsData.soc}%" else "--"
        ))
        items.add(DiagnosticsAdapter.DiagnosticsItem.Parameter(
            getString(R.string.diagnostics_soh),
            if (isConnected && bmsData != null) "${bmsData.soh}%" else "--"
        ))
        items.add(DiagnosticsAdapter.DiagnosticsItem.Parameter(
            getString(R.string.diagnostics_status),
            if (isConnected && bmsData != null) bmsData.status.toString() else "--"
        ))
        items.add(DiagnosticsAdapter.DiagnosticsItem.Parameter(
            getString(R.string.diagnostics_cell_count),
            if (isConnected && bmsData != null) "${bmsData.cellCount}" else "--"
        ))

        // Cell Voltages Section
        items.add(DiagnosticsAdapter.DiagnosticsItem.SectionHeader(
            getString(R.string.diagnostics_section_cells)
        ))

        if (isConnected && bmsData != null && bmsData.cellCount > 0) {
            bmsData.cellVoltages.take(bmsData.cellCount).forEachIndexed { index, voltage ->
                items.add(DiagnosticsAdapter.DiagnosticsItem.Parameter(
                    getString(R.string.diagnostics_cell, index + 1),
                    String.format("%.3f V", voltage)
                ))
            }
        } else {
            items.add(DiagnosticsAdapter.DiagnosticsItem.Parameter(
                getString(R.string.diagnostics_cell, 1),
                "-- V"
            ))
        }

        // Temperatures Section
        items.add(DiagnosticsAdapter.DiagnosticsItem.SectionHeader(
            getString(R.string.diagnostics_section_temps)
        ))

        items.add(DiagnosticsAdapter.DiagnosticsItem.Parameter(
            getString(R.string.diagnostics_temp_pcb),
            if (isConnected && bmsData != null) {
                val tempC = bmsData.tempPCB.toInt()
                val tempF = (tempC * 9 / 5) + 32
                "$tempF°F / $tempC°C"
            } else "-- °F / -- °C"
        ))

        items.add(DiagnosticsAdapter.DiagnosticsItem.Parameter(
            getString(R.string.diagnostics_temp_env),
            if (isConnected && bmsData != null) {
                val tempC = bmsData.tempEnv.toInt()
                val tempF = (tempC * 9 / 5) + 32
                "$tempF°F / $tempC°C"
            } else "-- °F / -- °C"
        ))

        if (isConnected && bmsData != null && bmsData.cellTempArray.isNotEmpty()) {
            bmsData.cellTempArray.forEachIndexed { index, temp ->
                val tempC = temp.toInt()
                val tempF = (tempC * 9 / 5) + 32
                items.add(DiagnosticsAdapter.DiagnosticsItem.Parameter(
                    getString(R.string.diagnostics_temp_sensor, index + 1),
                    "$tempF°F / $tempC°C"
                ))
            }
        }

        // Event Logs Section
        items.add(DiagnosticsAdapter.DiagnosticsItem.SectionHeader(
            getString(R.string.diagnostics_section_events)
        ))

        eventLogs.forEach { event ->
            items.add(DiagnosticsAdapter.DiagnosticsItem.Event(event))
        }

        adapter.setItems(items)
    }

    /**
     * Send logs via email
     */
    private fun sendLogs() {
        try {
            // Collect all diagnostic data
            val logsData = dataCollector.createLogsData(
                bmsData = currentBmsData,
                selectedId = mainViewModel?.selectedId?.value,
                canData = mainViewModel?.canData?.value,
                rs485Data = mainViewModel?.rs485Protocol?.value,
                eventLogs = eventLogs
            )

            // Create JSON file
            val dateFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val dateString = dateFormatter.format(Date())
            val fileName = "bigbattery_logs_android_$dateString.json"

            val file = File(requireContext().cacheDir, fileName)
            file.writeText(logsData.toString(2)) // Pretty print with 2 space indent

            // Create content URI using FileProvider
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )

            // Create email intent
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("evgeniydoronin@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.diagnostics_email_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.diagnostics_email_body))
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Check if email app is available
            if (emailIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(Intent.createChooser(emailIntent, "Send logs via"))
                addEvent(DiagnosticsEvent.EventType.CONNECTION, "Logs sent successfully")
            } else {
                Toast.makeText(requireContext(), R.string.diagnostics_no_email_app, Toast.LENGTH_LONG).show()
                addEvent(DiagnosticsEvent.EventType.ERROR, "No email app found")
            }

        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.diagnostics_email_error, Toast.LENGTH_LONG).show()
            addEvent(DiagnosticsEvent.EventType.ERROR, "Error sending logs: ${e.message}")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): DiagnosticsFragment {
            return DiagnosticsFragment()
        }
    }
}
