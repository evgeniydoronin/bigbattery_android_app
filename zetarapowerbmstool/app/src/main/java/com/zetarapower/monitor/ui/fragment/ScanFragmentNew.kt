package com.zetarapower.monitor.ui.fragment

/**
 * Created by Evgenii Doronin
 * LinkedIn: https://www.linkedin.com/in/evgeniydoronin
 * 
 * Современный фрагмент сканирования устройств с Big Battery дизайном
 */

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bosphere.filelogger.FL
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.zetarapower.monitor.R
import com.zetarapower.monitor.app.PowerMonitorApp
import com.zetarapower.monitor.bluetooth.BleUUIDs
import com.zetarapower.monitor.bluetooth.ZetaraBleUUID
import com.zetarapower.monitor.logic.PowerMonitorBlueTooth
import com.zetarapower.monitor.utils.showToast
import java.util.*

/**
 * Современный фрагмент сканирования устройств
 */
class ScanFragmentNew : DialogFragment() {

    companion object {
        private const val TAG = "ScanFragmentNew"
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 1001
    }

    private val mDeviceAdapter = DeviceAdapter()
    
    // Разрешения для Bluetooth
    private val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var imgLoading: ImageView
    private lateinit var rotateAnim: Animation
    private lateinit var btnScan: TextView
    private lateinit var emptyState: LinearLayout

    private var isConnecting = false
    private var connectCallback: ConnectCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan_new, container, false)
    }

    fun setConnectCallback(callback: ConnectCallback) {
        connectCallback = callback
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupClickListeners()
        setupRecyclerView()
        startScan()
    }

    /**
     * Инициализация view элементов
     */
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.scanDeviceListView)
        imgLoading = view.findViewById(R.id.img_loading)
        btnScan = view.findViewById(R.id.btn_scan)
        emptyState = view.findViewById(R.id.empty_state)
        
        rotateAnim = AnimationUtils.loadAnimation(context, R.anim.rotate)
    }

    /**
     * Настройка обработчиков кликов
     */
    private fun setupClickListeners() {
        btnScan.setOnClickListener {
            if (btnScan.text == getString(R.string.start_scan)) {
                startScan()
            } else if (btnScan.text == getString(R.string.stop_scan)) {
                BleManager.getInstance().cancelScan()
            }
        }
    }

    /**
     * Настройка RecyclerView
     */
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = mDeviceAdapter
    }

    /**
     * Проверка разрешений Bluetooth
     */
    private fun checkBluetoothPermissions(): Boolean {
        return bluetoothPermissions.all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Запрос разрешений Bluetooth
     */
    private fun requestBluetoothPermissions() {
        requestPermissions(bluetoothPermissions, REQUEST_BLUETOOTH_PERMISSIONS)
    }

    /**
     * Обработка результата запроса разрешений
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Все разрешения получены, запускаем сканирование
                    performScan()
                } else {
                    // Разрешения не получены
                    showToast("Bluetooth permissions are required for scanning devices")
                    btnScan.text = getString(R.string.start_scan)
                    btnScan.setBackgroundColor(resources.getColor(R.color.bb_primary, null))
                    imgLoading.clearAnimation()
                    imgLoading.visibility = View.INVISIBLE
                }
            }
        }
    }

    /**
     * Запуск сканирования устройств с проверкой разрешений
     */
    private fun startScan() {
        if (checkBluetoothPermissions()) {
            performScan()
        } else {
            requestBluetoothPermissions()
        }
    }

    /**
     * Выполнение сканирования (после получения разрешений)
     */
    private fun performScan() {
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                mDeviceAdapter.clear()
                mDeviceAdapter.addDevices(BleManager.getInstance().allConnectedDevice)
                mDeviceAdapter.notifyDataSetChanged()
                
                // Обновление UI
                imgLoading.startAnimation(rotateAnim)
                imgLoading.visibility = View.VISIBLE
                btnScan.text = getString(R.string.stop_scan)
                btnScan.setBackgroundColor(resources.getColor(R.color.colorInfoTitle, null))
                
                updateEmptyState()
            }

            override fun onLeScan(bleDevice: BleDevice) {
                super.onLeScan(bleDevice)
            }

            override fun onScanning(bleDevice: BleDevice) {
                mDeviceAdapter.addDevice(bleDevice)
                mDeviceAdapter.notifyDataSetChanged()
                updateEmptyState()
            }

            override fun onScanFinished(scanResultList: List<BleDevice>) {
                imgLoading.clearAnimation()
                if (activity != null) {
                    imgLoading.visibility = View.INVISIBLE
                    btnScan.text = getString(R.string.start_scan)
                    btnScan.setBackgroundColor(resources.getColor(R.color.bb_primary, null))
                }
                updateEmptyState()
                Log.i("Scan", "onScanFinished")
            }
        })
    }

    /**
     * Обновление состояния пустого списка
     */
    private fun updateEmptyState() {
        if (mDeviceAdapter.deviceList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * Подключение к устройству
     */
    private fun connect(bleDevice: BleDevice) {
        // Устройство уже подключено
        if (BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().cancelScan()
            dismiss()
            connectCallback?.onConnected(bleDevice, null)
            return
        }
        
        if (isConnecting) {
            return
        }
        
        isConnecting = true
        
        // Отключение предыдущих устройств
        if (BleManager.getInstance().allConnectedDevice != null
            && BleManager.getInstance().allConnectedDevice.isNotEmpty()
        ) {
            val device = BleManager.getInstance().allConnectedDevice[0]
            BleManager.getInstance().disconnectAllDevice()
            connectCallback?.onDisconnected(device)
        }

        // Подключение к новому устройству
        BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {
                // Показать индикатор подключения
            }
            
            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                isConnecting = false
                imgLoading.clearAnimation()
                imgLoading.visibility = View.INVISIBLE
                btnScan.text = PowerMonitorApp.inst.getString(R.string.start_scan)
                btnScan.setBackgroundColor(resources.getColor(R.color.bb_primary, null))
                showToast(R.string.connect_fail)
            }

            override fun onConnectSuccess(
                bleDevice: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {
                isConnecting = false
                showToast(R.string.connected)
                checkZetaraBleDevice(bleDevice, gatt)
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                bleDevice: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {
                isConnecting = false
            }
        })
    }

    /**
     * Проверка совместимости BLE устройства
     */
    private fun checkZetaraBleDevice(
        bleDevice: BleDevice,
        gatt: BluetoothGatt
    ) {
        var connectedService: BluetoothGattService? = null
        var connectedBleUUID: ZetaraBleUUID? = null
        
        for (bleUUID in BleUUIDs.serviceUUIDArray) {
            connectedService = gatt.getService(bleUUID.primaryServiceUUID)
            if (connectedService != null) {
                connectedBleUUID = bleUUID
                break
            }
        }

        if (connectedService == null || connectedBleUUID == null) {
            PowerMonitorBlueTooth.INSTANCE.disconnect(bleDevice)
            showToast(R.string.disconnect)
        } else {
            val writeCharacteristic: BluetoothGattCharacteristic? =
                connectedService.characteristics.find {
                    it.uuid == connectedBleUUID?.writeUUID
                }
            val notifyCharacteristic: BluetoothGattCharacteristic? =
                connectedService.characteristics.find {
                    it.uuid == connectedBleUUID?.notifyUUID
                }

            if (writeCharacteristic != null && notifyCharacteristic != null) {
                BleManager.getInstance()
                    .setMtu(bleDevice, 510, object : BleMtuChangedCallback() {
                        override fun onSetMTUFailure(exception: BleException) {
                            Log.e(TAG, "SetMTUFailure:$exception")
                            BleManager.getInstance().disconnect(bleDevice)
                            showToast(R.string.disconnect)
                        }

                        override fun onMtuChanged(mtu: Int) {
                            Log.i(TAG, "onMtuChanged: $mtu")
                            FL.i(TAG, "onMtuChanged: $mtu")
                            BleManager.getInstance().cancelScan()
                            dismiss()
                            connectCallback?.onConnected(bleDevice, connectedBleUUID)
                        }
                    })
            } else {
                PowerMonitorBlueTooth.INSTANCE.disconnect(bleDevice)
                showToast(R.string.disconnect)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        dialog?.window?.setLayout(
            LayoutParams.MATCH_PARENT,
            (resources.displayMetrics.heightPixels * 0.75f).toInt()
        )
    }

    /**
     * Адаптер для списка устройств
     */
    private inner class DeviceAdapter : RecyclerView.Adapter<DeviceHolder>() {

        val deviceList = ArrayList<BleDevice>()

        fun addDevices(devices: List<BleDevice>) {
            deviceList.addAll(devices)
        }

        fun addDevice(device: BleDevice) {
            if (!deviceList.contains(device)) {
                deviceList.add(device)
                notifyDataSetChanged()
            }
        }

        fun clear() {
            deviceList.clear()
        }

        override fun getItemCount(): Int {
            return deviceList.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
            val view = layoutInflater.inflate(R.layout.item_device_list_new, parent, false)
            return DeviceHolder(view)
        }

        override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
            val device = deviceList[position]
            
            // Имя устройства
            val deviceName = device.name
            if (!TextUtils.isEmpty(deviceName)) {
                holder.deviceName.text = deviceName
            } else {
                holder.deviceName.setText(R.string.unknown_device)
            }
            
            // MAC адрес
            holder.deviceAddress.text = device.mac
            
            // Уровень сигнала
            holder.rssi.text = "${device.rssi} dBm"
            
            // Статус подключения
            val isConnected = BleManager.getInstance().isConnected(device)
            holder.connect.text = if (isConnected) {
                getString(R.string.connected)
            } else {
                getString(R.string.connect)
            }
            
            // Цвет кнопки в зависимости от статуса
            holder.connect.setBackgroundColor(
                if (isConnected) {
                    resources.getColor(R.color.status_charging, null)
                } else {
                    resources.getColor(R.color.bb_primary, null)
                }
            )

            // Обработчик клика
            holder.itemView.setOnClickListener {
                it.isEnabled = false
                it.postDelayed({ it.isEnabled = true }, 2000)
                connect(device)
            }
        }
    }

    /**
     * ViewHolder для элемента списка устройств
     */
    private class DeviceHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.device_name)
        val deviceAddress: TextView = view.findViewById(R.id.device_address)
        val connect: TextView = view.findViewById(R.id.btn_connect)
        val rssi: TextView = view.findViewById(R.id.txt_rssi)
    }
}
