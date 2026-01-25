package com.zetarapower.monitor.ui.fragment

/**
 * Created by Evgenii Doronin
 * LinkedIn: https://www.linkedin.com/in/evgeniydoronin
 *
 * Современный фрагмент настроек с Big Battery дизайном
 */

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.ListPopupWindow.POSITION_PROMPT_BELOW
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.clj.fastble.BleManager
import com.zetarapower.monitor.MainActivity
import com.zetarapower.monitor.R
import com.zetarapower.monitor.logic.SettingsProtocolData
import com.zetarapower.monitor.ui.viewmodel.MainViewModel
import com.zetarapower.monitor.utils.dp2px
import com.zetarapower.monitor.utils.getAppVersionName

/**
 * Современный фрагмент настроек с карточным дизайном
 */
class SettingsFragmentNew : Fragment() {

    private var mainViewModel: MainViewModel? = null

    // UI Elements
    private lateinit var versionNameText: TextView

    // Connection Status Banner
    private lateinit var connectionStatusBanner: LinearLayout
    private lateinit var connectionStatusIcon: ImageView
    private lateinit var connectionStatusText: TextView

    // Module ID
    private lateinit var idsContainer: CardView
    private lateinit var selectedIdText: TextView
    private lateinit var moduleIdStatusLabel: TextView
    private var mIdsPopWindow: ListPopupWindow? = null

    // CAN Protocol
    private lateinit var canContainer: CardView
    private lateinit var selectedCANText: TextView
    private lateinit var canStatusLabel: TextView
    private var mCANPopWindow: ListPopupWindow? = null

    // RS485 Protocol
    private lateinit var rs485Container: CardView
    private lateinit var selectedRS485Text: TextView
    private lateinit var rs485StatusLabel: TextView
    private var mRS485PopWindow: ListPopupWindow? = null

    // Save Button and Information Banner
    private lateinit var saveButton: Button
    private lateinit var informationBanner: LinearLayout

    // Pending changes
    private var pendingModuleIdIndex: Int? = null
    private var pendingCANIndex: Int? = null
    private var pendingRS485Index: Int? = null
    private var isLoadingData = false

    // Current values from battery (null = not loaded yet)
    private var currentModuleId: Int? = null
    private var currentCANIndex: Int? = null
    private var currentRS485Index: Int? = null

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = activity?.let { ViewModelProviders.of(it)[MainViewModel::class.java] }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings_new, container, false)
        
        initViews(root)
        setupClickListeners()
        observeViewModel()
        loadInitialData()
        
        return root
    }

    /**
     * Инициализация view элементов
     */
    private fun initViews(root: View) {
        versionNameText = root.findViewById(R.id.versionName)

        // Connection Status Banner
        connectionStatusBanner = root.findViewById(R.id.connectionStatusBanner)
        connectionStatusIcon = root.findViewById(R.id.connectionStatusIcon)
        connectionStatusText = root.findViewById(R.id.connectionStatusText)

        // Module ID
        idsContainer = root.findViewById(R.id.IdsContainer)
        selectedIdText = root.findViewById(R.id.selectedId)
        moduleIdStatusLabel = root.findViewById(R.id.moduleIdStatusLabel)

        // CAN Protocol
        canContainer = root.findViewById(R.id.CANContainer)
        selectedCANText = root.findViewById(R.id.canId)
        canStatusLabel = root.findViewById(R.id.canStatusLabel)

        // RS485 Protocol
        rs485Container = root.findViewById(R.id.RS485Container)
        selectedRS485Text = root.findViewById(R.id.RS485Id)
        rs485StatusLabel = root.findViewById(R.id.rs485StatusLabel)

        // Save Button and Information Banner
        saveButton = root.findViewById(R.id.saveButton)
        informationBanner = root.findViewById(R.id.informationBanner)

        // Установка версии приложения
        versionNameText.text = getAppVersionName(requireActivity())

        // Hidden access to Diagnostics screen (long press on version)
        versionNameText.setOnLongClickListener {
            findNavController().navigate(R.id.action_settings_to_diagnostics)
            true
        }

        // Setup Save Button click listener
        saveButton.setOnClickListener {
            showSaveConfirmationDialog()
        }

        // Update connection status
        updateConnectionStatus()
    }

    /**
     * Настройка обработчиков кликов
     */
    private fun setupClickListeners() {
        // Module ID click listener
        if (getString(R.string.settings_module_ID) == "true") {
            selectedIdText.setOnClickListener {
                if (mIdsPopWindow == null) {
                    initIdListPop()
                }
                mIdsPopWindow?.show()
            }
        } else {
            idsContainer.visibility = View.GONE
        }

        // CAN and RS485 click listeners
        if (getString(R.string.settings_module_CAN) == "true") {
            selectedCANText.setOnClickListener {
                // Check if data is loaded
                if (mainViewModel?.canData?.value == null) {
                    Toast.makeText(requireContext(), R.string.waiting_for_data, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (mCANPopWindow == null) {
                    initCANListPop()
                }
                mCANPopWindow?.show()
            }

            selectedRS485Text.setOnClickListener {
                // Check if data is loaded
                if (mainViewModel?.rs485Protocol?.value == null) {
                    Toast.makeText(requireContext(), R.string.waiting_for_data, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (mRS485PopWindow == null) {
                    initRS485ListPop()
                }
                mRS485PopWindow?.show()
            }
        } else {
            canContainer.visibility = View.GONE
            rs485Container.visibility = View.GONE
        }
    }

    /**
     * Подписка на изменения в ViewModel
     */
    private fun observeViewModel() {
        // Selected ID observer
        mainViewModel?.selectedId?.observe(viewLifecycleOwner, Observer<Int> { id ->
            // Only update display if loading data (not from pending change)
            if (isLoadingData || pendingModuleIdIndex == null) {
                if (id == -1) {
                    // No data loaded yet - show placeholder like iOS
                    selectedIdText.text = "--"
                } else {
                    selectedIdText.text = "ID$id"
                    currentModuleId = id
                }
            }

            // Disable CAN and RS485 for non-ID1 modules
            // Use pending value if exists, otherwise use actual value (default to 1 for availability check)
            val effectiveModuleId = pendingModuleIdIndex?.let { it + 1 } ?: if (id == -1) 1 else id
            updateCANRS485Availability(effectiveModuleId)
        })

        // CAN Protocol observer
        mainViewModel?.canData?.observe(viewLifecycleOwner, Observer<SettingsProtocolData?> { data ->
            data?.let {
                val index = it.selectedIndex
                if (it.protocolArray != null && it.protocolArray.size > index) {
                    // Only update display if loading data (not from pending change)
                    if (isLoadingData || pendingCANIndex == null) {
                        selectedCANText.text = it.protocolArray[index]
                        currentCANIndex = index
                    }
                }
            }
        })

        // RS485 Protocol observer
        mainViewModel?.rs485Protocol?.observe(viewLifecycleOwner, Observer<SettingsProtocolData?> { data ->
            data?.let {
                val index = it.selectedIndex
                if (it.protocolArray != null && it.protocolArray.size > index) {
                    // Only update display if loading data (not from pending change)
                    if (isLoadingData || pendingRS485Index == null) {
                        selectedRS485Text.text = it.protocolArray[index]
                        currentRS485Index = index
                    }
                }
            }
        })

        // Mark loading as complete after initial data load
        handler.postDelayed({
            isLoadingData = false
        }, 2000)
    }

    /**
     * Обновление доступности CAN и RS485 настроек
     */
    private fun updateCANRS485Availability(moduleId: Int) {
        if (moduleId != 1) {
            selectedCANText.isEnabled = false
            selectedRS485Text.isEnabled = false
            selectedCANText.alpha = 0.5f
            selectedRS485Text.alpha = 0.5f
            canContainer.alpha = 0.6f
            rs485Container.alpha = 0.6f
        } else {
            selectedCANText.isEnabled = true
            selectedRS485Text.isEnabled = true
            selectedCANText.alpha = 1.0f
            selectedRS485Text.alpha = 1.0f
            canContainer.alpha = 1.0f
            rs485Container.alpha = 1.0f
        }
    }

    /**
     * Загрузка начальных данных
     */
    private fun loadInitialData() {
        isLoadingData = true
        if (BleManager.getInstance().allConnectedDevice?.isNotEmpty() == true) {
            if (activity != null) {
                (activity as MainActivity).stopTimer()
                (activity as MainActivity).getSettingData()
            }
        }
    }

    /**
     * Инициализация popup для выбора Module ID
     */
    private fun initIdListPop() {
        val lists: List<String> = listOf(
            "ID1", "ID2", "ID3", "ID4", "ID5", "ID6", "ID7", "ID8",
            "ID9", "ID10", "ID11", "ID12", "ID13", "ID14", "ID15", "ID16"
        )

        mIdsPopWindow = ListPopupWindow(requireActivity()).apply {
            setListSelector(context?.getDrawable(R.drawable.list_item_selector))
            setBackgroundDrawable(context?.getDrawable(R.drawable.item_bg))
            setAdapter(
                ArrayAdapter<String>(
                    requireContext(),
                    R.layout.popup_list_item,
                    lists
                )
            )
            width = dp2px(100)
            height = dp2px(350)
            anchorView = selectedIdText
            isModal = true
            promptPosition = POSITION_PROMPT_BELOW
            setOnItemClickListener { _, _, position, _ ->
                // Store pending change (like iOS - always track selection)
                val newModuleId = position + 1
                pendingModuleIdIndex = position
                selectedIdText.text = "ID$newModuleId"
                showStatusLabel(moduleIdStatusLabel, "ID$newModuleId")

                // Update CAN/RS485 availability based on new pending value
                updateCANRS485Availability(newModuleId)

                // If changing to non-ID1, clear CAN/RS485 pending changes
                if (newModuleId != 1) {
                    pendingCANIndex = null
                    pendingRS485Index = null
                    hideStatusLabel(canStatusLabel)
                    hideStatusLabel(rs485StatusLabel)
                }

                activateSaveButton()
                mIdsPopWindow?.dismiss()
            }
        }
    }

    /**
     * Инициализация popup для выбора CAN Protocol
     */
    private fun initCANListPop() {
        if (mainViewModel?.canData?.value == null) return

        mCANPopWindow = ListPopupWindow(requireActivity()).apply {
            setListSelector(context?.getDrawable(R.drawable.list_item_selector))
            setBackgroundDrawable(context?.getDrawable(R.drawable.item_bg))
            setAdapter(
                ArrayAdapter<String>(
                    requireContext(),
                    R.layout.popup_list_item,
                    mainViewModel?.canData?.value!!.protocolArray
                )
            )
            width = dp2px(150)
            height = dp2px(350)
            anchorView = selectedCANText
            isModal = true
            promptPosition = POSITION_PROMPT_BELOW
            setOnItemClickListener { _, _, position, _ ->
                // Store pending change (like iOS - always track selection)
                pendingCANIndex = position
                val protocolName = mainViewModel?.canData?.value?.protocolArray?.getOrNull(position) ?: "CAN $position"
                selectedCANText.text = protocolName
                showStatusLabel(canStatusLabel, protocolName)
                activateSaveButton()
                mCANPopWindow?.dismiss()
            }
        }
    }

    /**
     * Инициализация popup для выбора RS485 Protocol
     */
    private fun initRS485ListPop() {
        if (mainViewModel?.rs485Protocol?.value == null) return

        mRS485PopWindow = ListPopupWindow(requireActivity()).apply {
            setListSelector(context?.getDrawable(R.drawable.list_item_selector))
            setBackgroundDrawable(context?.getDrawable(R.drawable.item_bg))
            setAdapter(
                ArrayAdapter<String>(
                    requireContext(),
                    R.layout.popup_list_item,
                    mainViewModel?.rs485Protocol?.value!!.protocolArray
                )
            )
            width = dp2px(150)
            height = dp2px(250)
            anchorView = selectedRS485Text
            isModal = true
            promptPosition = POSITION_PROMPT_BELOW
            setOnItemClickListener { _, _, position, _ ->
                // Store pending change (like iOS - always track selection)
                pendingRS485Index = position
                val protocolName = mainViewModel?.rs485Protocol?.value?.protocolArray?.getOrNull(position) ?: "RS485 $position"
                selectedRS485Text.text = protocolName
                showStatusLabel(rs485StatusLabel, protocolName)
                activateSaveButton()
                mRS485PopWindow?.dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        if (BleManager.getInstance().allConnectedDevice?.isNotEmpty() == true) {
            if (activity != null) {
                (activity as MainActivity).getMainBMSData()
                (activity as MainActivity).startTimer()
            }
        }
    }

    // ==================== Connection Status Methods ====================

    /**
     * Обновление статуса подключения
     */
    private fun updateConnectionStatus() {
        val isConnected = BleManager.getInstance().allConnectedDevice?.isNotEmpty() == true
        if (isConnected) {
            showConnectedStatus()
        } else {
            showDisconnectedStatus()
        }
    }

    /**
     * Показать статус "Подключено"
     */
    private fun showConnectedStatus() {
        connectionStatusBanner.setBackgroundResource(R.drawable.connection_status_connected)
        connectionStatusIcon.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.connection_connected_text)
        )
        connectionStatusText.text = getString(R.string.connection_connected)
        connectionStatusText.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.connection_connected_text)
        )
    }

    /**
     * Показать статус "Отключено"
     */
    private fun showDisconnectedStatus() {
        connectionStatusBanner.setBackgroundResource(R.drawable.connection_status_disconnected)
        connectionStatusIcon.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.connection_disconnected_text)
        )
        connectionStatusText.text = getString(R.string.connection_disconnected)
        connectionStatusText.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.connection_disconnected_text)
        )
    }

    // ==================== Save Button Methods ====================

    /**
     * Активировать кнопку Save
     */
    private fun activateSaveButton() {
        saveButton.isEnabled = true
        saveButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        informationBanner.visibility = View.VISIBLE
    }

    /**
     * Деактивировать кнопку Save
     */
    private fun deactivateSaveButton() {
        saveButton.isEnabled = false
        saveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.save_button_text_disabled))
        informationBanner.visibility = View.GONE
    }

    /**
     * Проверить и деактивировать кнопку Save если нет pending изменений
     */
    private fun checkAndDeactivateSaveButton() {
        if (pendingModuleIdIndex == null && pendingCANIndex == null && pendingRS485Index == null) {
            deactivateSaveButton()
        }
    }

    // ==================== Status Label Methods ====================

    /**
     * Показать status label с выбранным значением
     */
    private fun showStatusLabel(label: TextView, value: String) {
        label.text = getString(R.string.status_label_selected, value)
        label.visibility = View.VISIBLE
    }

    /**
     * Скрыть status label
     */
    private fun hideStatusLabel(label: TextView) {
        label.visibility = View.GONE
    }

    /**
     * Скрыть все status labels
     */
    private fun hideAllStatusLabels() {
        hideStatusLabel(moduleIdStatusLabel)
        hideStatusLabel(canStatusLabel)
        hideStatusLabel(rs485StatusLabel)
    }

    // ==================== Save Confirmation Dialog ====================

    /**
     * Показать диалог подтверждения сохранения
     */
    private fun showSaveConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.save_dialog_title)
            .setMessage(R.string.save_dialog_message)
            .setPositiveButton(R.string.save_dialog_confirm) { _, _ ->
                performSave()
            }
            .setNegativeButton(R.string.save_dialog_cancel, null)
            .show()
    }

    /**
     * Выполнить сохранение pending изменений
     */
    private fun performSave() {
        val device = BleManager.getInstance().allConnectedDevice?.firstOrNull()
        if (device == null) {
            Toast.makeText(requireContext(), R.string.connection_disconnected, Toast.LENGTH_SHORT).show()
            return
        }

        // Hide all status labels
        hideAllStatusLabels()

        // Show saving message
        Toast.makeText(requireContext(), R.string.saving_settings, Toast.LENGTH_SHORT).show()

        // Send pending changes
        pendingModuleIdIndex?.let { index ->
            mainViewModel?.setBMSModuleIdData(device, null, index + 1)
        }

        pendingCANIndex?.let { index ->
            mainViewModel?.setCanData(device, null, index)
        }

        pendingRS485Index?.let { index ->
            mainViewModel?.setRS485Data(device, null, index)
        }

        // Clear pending changes
        clearPendingChanges()

        // Deactivate Save button
        deactivateSaveButton()

        // Show battery restarting message
        showBatteryRestartingMessage()
    }

    /**
     * Очистить все pending изменения
     */
    private fun clearPendingChanges() {
        pendingModuleIdIndex = null
        pendingCANIndex = null
        pendingRS485Index = null
    }

    /**
     * Показать сообщение о перезагрузке батареи
     */
    private fun showBatteryRestartingMessage() {
        Toast.makeText(requireContext(), R.string.battery_restarting, Toast.LENGTH_LONG).show()

        // Update connection status after a delay (battery will disconnect)
        handler.postDelayed({
            if (isAdded) {
                updateConnectionStatus()
            }
        }, 3000)
    }

    companion object {
        @JvmStatic
        fun newInstance(): SettingsFragmentNew {
            return SettingsFragmentNew()
        }
    }
}
