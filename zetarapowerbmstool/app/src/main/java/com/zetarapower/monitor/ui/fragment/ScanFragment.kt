package com.zetarapower.monitor.ui.fragment

import android.app.Dialog
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
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
import android.widget.TextView
import androidx.annotation.Nullable
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
 *
 * Info Fragment
 *
 */
class ScanFragment : DialogFragment() {


    companion object {
        private const val TAG = "ScanFragment"
    }

    private val mDeviceAdapter = DeviceAdapter()

    private lateinit var recyclerView: RecyclerView

    private lateinit var imgLoading: ImageView
    private lateinit var rotateAnim: Animation
    private lateinit var btnScan: TextView

    private var isConnecting = false

    private var connectCallback: ConnectCallback? = null

    /**
     *
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }


    /**
     *
     */
    fun setConnectCallback(callback: ConnectCallback) {
        connectCallback = callback
    }

    /**
     *
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.scanDeviceListView)
        imgLoading = view.findViewById(R.id.img_loading)
        rotateAnim = AnimationUtils.loadAnimation(context, R.anim.rotate)
        btnScan = view.findViewById(R.id.btn_scan)
        btnScan.setOnClickListener {
            if (btnScan.text == getString(R.string.start_scan)) {
                startScan()
            } else if (btnScan.text == getString(R.string.stop_scan)) {
                BleManager.getInstance().cancelScan()
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = mDeviceAdapter
        startScan()
    }


    /**
     *
     */
    private fun startScan() {
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                mDeviceAdapter.clear()
                mDeviceAdapter.addDevices(BleManager.getInstance().allConnectedDevice)
                mDeviceAdapter.notifyDataSetChanged()
                imgLoading.startAnimation(rotateAnim)
                imgLoading.visibility = View.VISIBLE
                btnScan.text = getString(R.string.stop_scan)
            }

            /**
             *
             */
            override fun onLeScan(bleDevice: BleDevice) {
                super.onLeScan(bleDevice)
            }

            /**
             *
             */
            override fun onScanning(bleDevice: BleDevice) {
                mDeviceAdapter.addDevice(bleDevice)
                mDeviceAdapter.notifyDataSetChanged()
            }

            /**
             *
             */
            override fun onScanFinished(scanResultList: List<BleDevice>) {
                imgLoading.clearAnimation()
                if (activity != null) {
                    imgLoading.visibility = View.INVISIBLE
                    btnScan.text = getString(R.string.start_scan)
                }
                Log.i("Scan", "onScanFinished")
            }
        })
    }

    /**
     *
     */
    private inner class DeviceAdapter : RecyclerView.Adapter<DeviceHolder>() {

        val deviceList = ArrayList<BleDevice>()

        //
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
            val view = layoutInflater.inflate(R.layout.item_device_list, null)
            return DeviceHolder(view)
        }


        override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
            val device = deviceList[position]
            val deviceName = device.name
            if (!TextUtils.isEmpty(deviceName)) {
                holder.deviceName.text = deviceName
            } else {
                holder.deviceName.setText(R.string.unknown_device)
            }
            holder.deviceAddress.text = device.mac

            holder.rssi.text = device.rssi.toString()

            holder.connect.text = if (BleManager.getInstance().isConnected(device)) {
                getString(R.string.connected)
            } else {
                getString(R.string.connect)
            }

            holder.itemView.setOnClickListener {
                it.isEnabled = false
                it.postDelayed( Runnable { it.isEnabled = true }, 2000)
                /**
                 * 点击连接设备
                 */
                connect(device)

            }
        }
    }


    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        dialog?.window?.setLayout(
            LayoutParams.MATCH_PARENT,
            (resources.displayMetrics.heightPixels * 0.65f).toInt()
        )
    }


    /**
     *
     */
    private fun connect(bleDevice: BleDevice) {
        // 当前设备已连接
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
        // 断开之前的连接
        if (BleManager.getInstance().allConnectedDevice != null
            && BleManager.getInstance().allConnectedDevice.isNotEmpty()
        ) {
            val device = BleManager.getInstance().allConnectedDevice[0]
            BleManager.getInstance().disconnectAllDevice()
            connectCallback?.onDisconnected(device)
        }

        // 连接新设备
        BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {

            }
            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                isConnecting = false
                imgLoading.clearAnimation()
                imgLoading.visibility = View.INVISIBLE
                btnScan.text = PowerMonitorApp.inst.getString(R.string.start_scan)
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
     *
     */
    private fun checkZetaraBleDevice(
        bleDevice: BleDevice,
        gatt: BluetoothGatt
    ) {

        // check if the BLE is our BLE device, or disconnect
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
            var writeCharacteristic: BluetoothGattCharacteristic? =
                connectedService.characteristics.find {
                    it.uuid == connectedBleUUID?.writeUUID
                }
            var notifyCharacteristic: BluetoothGattCharacteristic? =
                connectedService.characteristics.find {
                    it.uuid == connectedBleUUID?.notifyUUID
                }

            if (writeCharacteristic != null
                && notifyCharacteristic != null
            ) {
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


    private class DeviceHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.device_name)
        val deviceAddress: TextView = view.findViewById(R.id.device_address)
        val connect: TextView = view.findViewById(R.id.btn_connect)
        val rssi: TextView = view.findViewById(R.id.txt_rssi)

        init {
            connect.visibility = View.VISIBLE
        }
    }


}