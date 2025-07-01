package com.zetarapower.monitor.ui.fragment

/**
 * Created by Evgenii Doronin
 * LinkedIn: https://www.linkedin.com/in/evgeniydoronin
 * 
 * Новый Main Fragment с современным дизайном Big Battery
 * Включает табы Summary, Cell Voltage, Temperature
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import at.grabner.circleprogress.CircleProgressView
import com.clj.fastble.data.BleDevice
import com.zetarapower.monitor.MainActivity
import com.zetarapower.monitor.R
import com.zetarapower.monitor.bluetooth.ZetaraBleUUID
import com.zetarapower.monitor.logic.BMSData
import com.zetarapower.monitor.ui.viewmodel.MainViewModel

/**
 * Новый Main Fragment с современным дизайном Big Battery
 * Включает табы Summary, Cell Voltage, Temperature
 */
class MainFragmentNew : Fragment() {

    private var mainViewModel: MainViewModel? = null

    // Header и Bluetooth панель
    private lateinit var bluetoothCard: CardView
    private lateinit var bluetoothDeviceName: TextView
    private lateinit var deviceStatusText: TextView

    // Battery Progress
    private lateinit var circleView: CircleProgressView
    private lateinit var chargingImage: ImageView
    private lateinit var batteryPercentage: TextView

    // Parameters Cards
    private lateinit var voltageValue: TextView
    private lateinit var currentValue: TextView
    private lateinit var tempValue: TextView

    // Tabs
    private lateinit var tabSummary: TextView
    private lateinit var tabCellVoltage: TextView
    private lateinit var tabTemperature: TextView

    // Tab Content
    private lateinit var summaryTabContent: View
    private lateinit var cellVoltageTabContent: View
    private lateinit var temperatureTabContent: View

    // Summary Tab Views
    private lateinit var maxVoltageValue: TextView
    private lateinit var minVoltageValue: TextView
    private lateinit var voltageDiffValue: TextView
    private lateinit var powerValue: TextView
    private lateinit var internalTempValue: TextView
    private lateinit var avgVoltageValue: TextView

    private var currentTab = 0 // 0 = Summary, 1 = Cell Voltage, 2 = Temperature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = activity?.let { ViewModelProviders.of(it)[MainViewModel::class.java] }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main_new, container, false)
        
        initViews(root)
        setupTabClickListeners()
        setupBluetoothClickListener()
        observeViewModel()
        
        return root
    }

    /**
     * Инициализация всех view элементов
     */
    private fun initViews(root: View) {
        // Bluetooth панель
        bluetoothCard = root.findViewById(R.id.bluetooth_card)
        bluetoothDeviceName = root.findViewById(R.id.bluetooth_device_name)
        deviceStatusText = root.findViewById(R.id.device_status_text)

        // Battery Progress
        circleView = root.findViewById(R.id.circleView)
        chargingImage = root.findViewById(R.id.charging_img)
        batteryPercentage = root.findViewById(R.id.battery_percentage)

        // Parameters
        voltageValue = root.findViewById(R.id.voltageValue)
        currentValue = root.findViewById(R.id.currentValue)
        tempValue = root.findViewById(R.id.tempValue)

        // Tabs
        tabSummary = root.findViewById(R.id.tab_summary)
        tabCellVoltage = root.findViewById(R.id.tab_cell_voltage)
        tabTemperature = root.findViewById(R.id.tab_temperature)

        // Tab Content
        summaryTabContent = root.findViewById(R.id.summary_tab_content)
        cellVoltageTabContent = root.findViewById(R.id.cell_voltage_tab_content)
        temperatureTabContent = root.findViewById(R.id.temperature_tab_content)

        // Summary Tab Views
        maxVoltageValue = root.findViewById(R.id.max_voltage_value)
        minVoltageValue = root.findViewById(R.id.min_voltage_value)
        voltageDiffValue = root.findViewById(R.id.voltage_diff_value)
        powerValue = root.findViewById(R.id.power_value)
        internalTempValue = root.findViewById(R.id.internal_temp_value)
        avgVoltageValue = root.findViewById(R.id.avg_voltage_value)
    }

    /**
     * Настройка обработчиков кликов для табов
     */
    private fun setupTabClickListeners() {
        tabSummary.setOnClickListener { switchToTab(0) }
        tabCellVoltage.setOnClickListener { switchToTab(1) }
        tabTemperature.setOnClickListener { switchToTab(2) }
    }

    /**
     * Настройка обработчика клика для Bluetooth панели
     */
    private fun setupBluetoothClickListener() {
        bluetoothCard.setOnClickListener {
            if (activity != null) {
                showScanDialogNew()
            }
        }
    }

    /**
     * Показать новый диалог сканирования
     */
    private fun showScanDialogNew() {
        val scanDialog = ScanFragmentNew().apply {
            setConnectCallback(object : ConnectCallback {
                override fun onConnected(bleDevice: BleDevice, uuid: ZetaraBleUUID?) {
                    mainViewModel?.getBMSData(bleDevice, uuid)
                }

                override fun onDisconnected(bleDevice: BleDevice) {
                    mainViewModel?.disConnectDevice(bleDevice)
                }
            })
        }
        scanDialog.show(parentFragmentManager, "scanDialogNew")
    }

    /**
     * Переключение между табами
     */
    private fun switchToTab(tabIndex: Int) {
        currentTab = tabIndex

        // Сброс стилей всех табов
        resetTabStyles()

        // Скрытие всего контента
        summaryTabContent.visibility = View.GONE
        cellVoltageTabContent.visibility = View.GONE
        temperatureTabContent.visibility = View.GONE

        // Активация выбранного таба
        when (tabIndex) {
            0 -> {
                activateTab(tabSummary)
                summaryTabContent.visibility = View.VISIBLE
            }
            1 -> {
                activateTab(tabCellVoltage)
                cellVoltageTabContent.visibility = View.VISIBLE
            }
            2 -> {
                activateTab(tabTemperature)
                temperatureTabContent.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Сброс стилей всех табов
     */
    private fun resetTabStyles() {
        val inactiveColor = resources.getColor(R.color.bb_on_surface, null)
        val inactiveBackground = resources.getColor(R.color.bb_surface, null)

        tabSummary.setTextColor(inactiveColor)
        tabSummary.setBackgroundColor(inactiveBackground)

        tabCellVoltage.setTextColor(inactiveColor)
        tabCellVoltage.setBackgroundColor(inactiveBackground)

        tabTemperature.setTextColor(inactiveColor)
        tabTemperature.setBackgroundColor(inactiveBackground)
    }

    /**
     * Активация выбранного таба
     */
    private fun activateTab(tab: TextView) {
        val activeColor = resources.getColor(R.color.bb_surface, null)
        val activeBackground = resources.getColor(R.color.bb_primary, null)

        tab.setTextColor(activeColor)
        tab.setBackgroundColor(activeBackground)
    }

    /**
     * Подписка на изменения в ViewModel
     */
    private fun observeViewModel() {
        // Данные BMS
        mainViewModel?.data?.observe(viewLifecycleOwner, Observer<BMSData> { bmsData ->
            updateBMSData(bmsData)
        })

        // Статус подключения
        mainViewModel?.updateStatus?.observe(viewLifecycleOwner, Observer<String> { status ->
            deviceStatusText.text = status?.toString() ?: "Device Not Connected"
        })

        // Имя подключенного устройства
        mainViewModel?.connectedDeviceName?.observe(viewLifecycleOwner, Observer<String> { name ->
            bluetoothDeviceName.text = name?.toString() ?: "Tap to Connect"
        })
    }

    /**
     * Обновление данных BMS на UI
     */
    private fun updateBMSData(data: BMSData) {
        // Обновление круговой диаграммы
        circleView.setValue(data.soc.toFloat())
        batteryPercentage.text = "${data.soc}%"

        // Обновление основных параметров
        voltageValue.text = "${data.voltage}V"
        currentValue.text = "${data.current}A"
        
        val tempF = (32 + data.tempEnv * 1.8).toInt()
        tempValue.text = "${data.tempEnv}°C/${tempF}℉"

        // Обновление иконки зарядки
        if (data.status == 1) {
            chargingImage.visibility = View.VISIBLE
        } else {
            chargingImage.visibility = View.GONE
        }

        // Обновление Summary таба (если есть дополнительные данные)
        updateSummaryTab(data)
    }

    /**
     * Обновление данных Summary таба
     */
    private fun updateSummaryTab(data: BMSData) {
        // Пока используем базовые данные, позже добавим расчеты
        maxVoltageValue.text = "${data.voltage}V" // Заглушка
        minVoltageValue.text = "${data.voltage}V" // Заглушка
        voltageDiffValue.text = "0.0V" // Заглушка
        
        // Расчет мощности
        val power = data.voltage * data.current
        powerValue.text = "${String.format("%.1f", power)}W"
        
        internalTempValue.text = "${data.tempEnv}°F"
        avgVoltageValue.text = "${data.voltage}V" // Заглушка
    }

    companion object {
        @JvmStatic
        fun newInstance(): MainFragmentNew {
            return MainFragmentNew()
        }
    }
}
