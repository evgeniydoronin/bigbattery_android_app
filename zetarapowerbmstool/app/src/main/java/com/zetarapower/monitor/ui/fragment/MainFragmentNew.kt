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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zetarapower.monitor.ui.widgets.ArcProgressView
import com.clj.fastble.data.BleDevice
import com.zetarapower.monitor.MainActivity
import com.zetarapower.monitor.R
import com.zetarapower.monitor.bluetooth.ZetaraBleUUID
import com.zetarapower.monitor.logic.BMSData
import com.zetarapower.monitor.ui.adapter.CellVoltageAdapter
import com.zetarapower.monitor.ui.adapter.CellVoltageItemDecoration
import com.zetarapower.monitor.ui.adapter.TemperatureSensorAdapter
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

    // Battery Progress
    private lateinit var circleView: ArcProgressView
    private lateinit var chargingImage: ImageView
    private lateinit var batteryPercentage: TextView

    // Parameters Cards - основные значения теперь статичны в layout

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

    // Cell Voltage Tab
    private lateinit var cellVoltageRecyclerView: RecyclerView
    private lateinit var cellVoltageAdapter: CellVoltageAdapter

    // Temperature Tab
    private lateinit var temperatureRecyclerView: RecyclerView
    private lateinit var temperatureAdapter: TemperatureSensorAdapter

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

        // Battery Progress
        circleView = root.findViewById(R.id.circleView)
        chargingImage = root.findViewById(R.id.charging_img)
        batteryPercentage = root.findViewById(R.id.battery_percentage)

        // Parameters - основные значения теперь статичны в layout

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

        // Cell Voltage Tab - настройка RecyclerView
        setupCellVoltageRecyclerView(root)
        
        // Temperature Tab - настройка RecyclerView
        setupTemperatureRecyclerView(root)
    }

    /**
     * Настройка RecyclerView для Cell Voltage таба
     */
    private fun setupCellVoltageRecyclerView(root: View) {
        cellVoltageRecyclerView = root.findViewById(R.id.cell_voltage_recycler_view)
        cellVoltageAdapter = CellVoltageAdapter()
        
        // Настройка GridLayoutManager с 4 колонками для компактного отображения
        val gridLayoutManager = GridLayoutManager(context, 4)
        cellVoltageRecyclerView.layoutManager = gridLayoutManager
        cellVoltageRecyclerView.adapter = cellVoltageAdapter
        
        // Добавляем ItemDecoration для отступов между рядами
        val itemDecoration = CellVoltageItemDecoration(spanCount = 4, rowSpacing = 6)
        cellVoltageRecyclerView.addItemDecoration(itemDecoration)
        
        // Инициализация с 16 ячейками с прочерками по умолчанию
        cellVoltageAdapter.updateCellVoltages(FloatArray(0), 0)
    }

    /**
     * Настройка RecyclerView для Temperature таба
     */
    private fun setupTemperatureRecyclerView(root: View) {
        temperatureRecyclerView = root.findViewById(R.id.temperature_recycler_view)
        temperatureAdapter = TemperatureSensorAdapter()
        
        // Настройка LinearLayoutManager для вертикального списка (как в iOS)
        val linearLayoutManager = LinearLayoutManager(context)
        temperatureRecyclerView.layoutManager = linearLayoutManager
        temperatureRecyclerView.adapter = temperatureAdapter
        
        // Инициализация без данных - будут загружены при подключении к батарее
        temperatureAdapter.updateTemperatureSensors(0, ByteArray(0))
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

        tabSummary.setTextColor(inactiveColor)
        tabSummary.setBackgroundResource(R.drawable.tab_unselected_bg)

        tabCellVoltage.setTextColor(inactiveColor)
        tabCellVoltage.setBackgroundResource(R.drawable.tab_unselected_bg)

        tabTemperature.setTextColor(inactiveColor)
        tabTemperature.setBackgroundResource(R.drawable.tab_unselected_bg)
    }

    /**
     * Активация выбранного таба
     */
    private fun activateTab(tab: TextView) {
        val activeColor = resources.getColor(R.color.bb_green, null)

        tab.setTextColor(activeColor)
        tab.setBackgroundResource(R.drawable.tab_selected_bg)
    }

    /**
     * Подписка на изменения в ViewModel
     */
    private fun observeViewModel() {
        // Данные BMS
        mainViewModel?.data?.observe(viewLifecycleOwner, Observer<BMSData> { bmsData ->
            updateBMSData(bmsData)
        })

        // Статус подключения - больше не отображается в UI
        // mainViewModel?.updateStatus?.observe(viewLifecycleOwner, Observer<String> { status ->
        //     // Статус больше не отображается в Bluetooth панели
        // })

        // Имя подключенного устройства
        mainViewModel?.connectedDeviceName?.observe(viewLifecycleOwner, Observer<String> { name ->
            bluetoothDeviceName.text = name?.toString() ?: "Tap to Connect"
        })
    }

    /**
     * Обновление данных BMS на UI
     */
    private fun updateBMSData(data: BMSData) {
        // Обновление дуги прогресса
        circleView.setProgress(data.soc)
        batteryPercentage.text = "${data.soc}%"

        // Основные параметры теперь статичны в layout
        // Реальные значения отображаются в Summary табе

        // Обновление иконки зарядки
        if (data.status == 1) {
            chargingImage.visibility = View.VISIBLE
        } else {
            chargingImage.visibility = View.GONE
        }

        // Обновление Summary таба (если есть дополнительные данные)
        updateSummaryTab(data)
        
        // Обновление Cell Voltage таба
        updateCellVoltageTab(data)
        
        // Обновление Temperature таба
        updateTemperatureTab(data)
    }

    /**
     * Обновление данных Cell Voltage таба
     */
    private fun updateCellVoltageTab(data: BMSData) {
        // Обновляем адаптер с реальным количеством ячеек из BMSData
        // Адаптер сам покажет 16 ячеек с прочерками если нет данных
        cellVoltageAdapter.updateCellVoltages(data.cellVoltages, data.cellCount)
    }

    /**
     * Обновление данных Summary таба с реальными расчетами
     */
    private fun updateSummaryTab(data: BMSData) {
        // Получаем валидные напряжения ячеек
        val validVoltages = data.cellVoltages.take(data.cellCount).filter { it > 0 }
        
        if (validVoltages.isNotEmpty()) {
            // Реальные расчеты на основе ячеек
            val maxVoltage = validVoltages.maxOrNull() ?: 0f
            val minVoltage = validVoltages.minOrNull() ?: 0f
            val avgVoltage = validVoltages.average().toFloat()
            val voltageDiff = maxVoltage - minVoltage
            
            maxVoltageValue.text = "${String.format("%.2f", maxVoltage)}V"
            minVoltageValue.text = "${String.format("%.2f", minVoltage)}V"
            voltageDiffValue.text = "${String.format("%.3f", voltageDiff)}V"
            avgVoltageValue.text = "${String.format("%.2f", avgVoltage)}V"
        } else {
            // Fallback к общему напряжению если нет данных ячеек
            if (data.voltage > 0) {
                maxVoltageValue.text = "${String.format("%.2f", data.voltage)}V"
                minVoltageValue.text = "${String.format("%.2f", data.voltage)}V"
                avgVoltageValue.text = "${String.format("%.2f", data.voltage)}V"
            } else {
                maxVoltageValue.text = "-- V"
                minVoltageValue.text = "-- V"
                avgVoltageValue.text = "-- V"
            }
            voltageDiffValue.text = "-- V"
        }
        
        // Расчет мощности
        val power = data.voltage * data.current
        if (power != 0f) {
            powerValue.text = "${String.format("%.1f", power)}W"
        } else {
            powerValue.text = "-- W"
        }
        
        // Внутренняя температура
        if (data.tempEnv.toInt() != 0) {
            internalTempValue.text = "${data.tempEnv}°F"
        } else {
            internalTempValue.text = "-- °F"
        }
    }

    /**
     * Обновление данных Temperature таба
     */
    private fun updateTemperatureTab(data: BMSData) {
        // Обновляем адаптер с новыми температурными данными
        // Адаптер сам покажет датчики с прочерками если нет данных
        temperatureAdapter.updateTemperatureSensors(data.tempPCB, data.cellTempArray)
    }

    companion object {
        @JvmStatic
        fun newInstance(): MainFragmentNew {
            return MainFragmentNew()
        }
    }
}
