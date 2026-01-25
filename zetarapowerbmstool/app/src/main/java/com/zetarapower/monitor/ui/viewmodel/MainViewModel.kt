package com.zetarapower.monitor.ui.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bosphere.filelogger.FL
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import com.zetarapower.monitor.R
import com.zetarapower.monitor.app.PowerMonitorApp
import com.zetarapower.monitor.bluetooth.ZetaraBleUUID
import com.zetarapower.monitor.logic.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 *
 */
class MainViewModel : ViewModel() {

    private var notifyStatus: MutableMap<BleDevice, Boolean> = mutableMapOf()

    private var uuidDeviceMap: MutableMap<BleDevice, ZetaraBleUUID> = mutableMapOf()

    private var timeFormat = SimpleDateFormat(PowerMonitorApp.inst.getString(R.string.time_format))

    private var needUpdateNotifyStatus = true

    private var bmsSplitter: BMSSplitter = BMSSplitter(object : BMSDataReadyCallback {
        override fun bmsDataReady(bmsData: BMSData?) {
            if (bmsData != null) {
                _data.value = bmsData
                _updateStatus.value = "Last update: ${getCurrentTime()}"
            }
        }
    })

    //
    val updateStatus: LiveData<String>
        get() = _updateStatus

    private val _updateStatus: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            it.value = "Device Not Connected"
        }
    }


    //
    val connectedDeviceName: LiveData<String>
        get() = _connectedDeviceName

    private val _connectedDeviceName: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            it.value = "Tap to Connect"
        }
    }

    // Settings
    private var lastSetId: Int = -1
    val selectedId: LiveData<Int>
        get() = _selectedId

    private val _selectedId: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().also {
            it.value = -1
        }
    }

    // CAN
    private var lastSetCanId: Int = -1
    val canData: LiveData<SettingsProtocolData>
        get() = _canData
    private val _canData: MutableLiveData<SettingsProtocolData> by lazy {
        MutableLiveData<SettingsProtocolData>().also {
            it.value = null
        }
    }

    // rs485
    private var lastSetRS485Id: Int = -1
    val rs485Protocol: LiveData<SettingsProtocolData>
        get() = _rs485Protocol

    private val _rs485Protocol: MutableLiveData<SettingsProtocolData> by lazy {
        MutableLiveData<SettingsProtocolData>().also {
            it.value = null
        }
    }


    //
    val data: LiveData<BMSData>
        //使number获取的值是_number
        get() = _data

    private val _data: MutableLiveData<BMSData> by lazy {
        MutableLiveData<BMSData>().also {
            it.value = BMSData(0.0f, 0.0f)
        }
    }

    /**
     *
     */
    fun disConnectDevice(device: BleDevice) {
        notifyStatus.remove(device)
        uuidDeviceMap.remove(device)
        _updateStatus.value = "Device Not Connected"
        _connectedDeviceName.value = "Tap to Connect"
        _selectedId.value = -1
        _rs485Protocol.value = null
        _canData.value = null
    }

    /**
     *
     */

    fun removeNotifyStatus(device: BleDevice) {
        needUpdateNotifyStatus = false
        notifyStatus.remove(device)
    }

    /**
     * for test
     */
    fun mockBLEData(bleData: ByteArray) {
        bmsSplitter.addBLEData(bleData)
    }

    /**
     *  获取BMS data
     */
    fun getBMSData(device: BleDevice, uuid: ZetaraBleUUID?) {
        sendControlData(device, uuid, BMSProtocalV2.OPERATION_GETBMS_HEX)
    }

    /**
     *  获取getBMSModuleIdData
     */
    fun getBMSModuleIdData(device: BleDevice, uuid: ZetaraBleUUID?) {
        sendControlData(device, uuid, BMSProtocalV2.OPERATION_GETIDS)
    }

    /**
     *  获取getBMSModuleIdData
     */
    fun setBMSModuleIdData(device: BleDevice, uuid: ZetaraBleUUID?, idNo: Int) {
        val opBytes =
            byteArrayOf(0x10, BMSProtocalV2.FUN_CODE_IDS_SET.toByte(), 0x01, idNo.toByte())
        val opHex = opBytes.generateCRC16().toHexString()
        sendControlData(device, uuid, opHex)
        lastSetId = idNo
    }


    /**
     *  获取getBMSModuleIdData
     */
    fun getRS485Data(device: BleDevice, uuid: ZetaraBleUUID?) {
        sendControlData(device, uuid, BMSProtocalV2.OPERATION_GETRS485)
    }

    /**
     *  获取getBMSModuleIdData
     */
    fun setRS485Data(device: BleDevice, uuid: ZetaraBleUUID?, idNo: Int) {
        val opBytes =
            byteArrayOf(0x10, BMSProtocalV2.FUN_CODE_RS485_SET.toByte(), 0x01, idNo.toByte())
        val opHex = opBytes.generateCRC16().toHexString()
        sendControlData(device, uuid, opHex)
        lastSetRS485Id = idNo
    }

    /**
     *  获取Can protocal
     */
    fun getCanData(device: BleDevice, uuid: ZetaraBleUUID?) {
        sendControlData(device, uuid, BMSProtocalV2.OPERATION_GETCAN)
    }

    /**
     *
     */
    fun setCanData(device: BleDevice, uuid: ZetaraBleUUID?, idNo: Int) {
        val opBytes =
            byteArrayOf(0x10, BMSProtocalV2.FUN_CODE_CAN_SET.toByte(), 0x01, idNo.toByte())
        val opHex = opBytes.generateCRC16().toHexString()
        sendControlData(device, uuid, opHex)
        lastSetCanId = idNo
    }


    private fun sendControlData(device: BleDevice, uuid: ZetaraBleUUID?, hexString: String) {
        sendBMSControlData(device, uuid) { _device, _uuid ->
            sendBLEControlData(_device, _uuid, hexString)
        }
    }

    /**
     *
     */
    private fun sendBMSControlData(
        device: BleDevice,
        uuid: ZetaraBleUUID?,
        op: (device: BleDevice, uuid: ZetaraBleUUID) -> Unit
    ) {
        var deviceUUID = uuid
        if (deviceUUID == null) {
            deviceUUID = uuidDeviceMap[device]
            _connectedDeviceName.value = device.name
        } else {
            uuidDeviceMap[device] = deviceUUID
        }
        if (deviceUUID == null) {
            return
        }
        if (notifyStatus[device] == true) {
            op(device, deviceUUID)
        } else {
            BleManager.getInstance().notify(device, deviceUUID.primaryServiceUUID.toString(),
                deviceUUID.notifyUUID.toString(), object : BleNotifyCallback() {
                    override fun onCharacteristicChanged(data: ByteArray?) {
                        if (data != null) {
                            handleResponseData(data)
                        }
                    }

                    override fun onNotifyFailure(exception: BleException?) {

                    }

                    override fun onNotifySuccess() {
                        FL.i("BLEData", "onNotifySuccess")
                        notifyStatus[device] = true
                        if (needUpdateNotifyStatus) {
                            _updateStatus.value = "Device Connected"
                        } else {
                            needUpdateNotifyStatus = true
                        }
                        op(device, deviceUUID)
                    }
                })
        }
    }


    /**
     *   "100470080B5030312D4752570000005030322D534C4B0000005030332D4459000000005030342D4D47520000005030352D5643540000005030362D4C55580000005030372D534D410000005030382D494E480000005030392D534F4C0000005031302D41464F0000005031312D535455000000350B"
     *
     *   "10033400055030312D4752570000005030322D4C55580000005030332D5343480000005030342D494E480000005030352D564F4C000000BCD8"
     */
    fun handleResponseData(data: ByteArray) {
        FL.i("BLEData", HexUtil.encodeHexStr(data))
        when (data[0]) {
            BMSProtocalV2.ADDRESS_CODE_BMS.toByte() -> { // ble data
                bmsSplitter.addBLEData(data)
            }
            BMSProtocalV2.ADDRESS_CODE_SETTINGS.toByte() -> { // settings data
                handleSettingsData(data)
            }
        }
    }

    /**
     *  处理setting数据
     */
    private fun handleSettingsData(data: ByteArray) {
        if (!data.crc16Verify()) {
            return
        }
        when (data[1]) {
            BMSProtocalV2.FUN_CODE_IDS_GET.toByte() -> {
                _selectedId.value = data[3].toInt()
            }
            BMSProtocalV2.FUN_CODE_IDS_SET.toByte() -> {
                if (data[3].toInt() == 0) {
                    if (lastSetId != -1) {
                        _selectedId.value = lastSetId
                        lastSetId = -1
                    }
                } else if (data[3].toInt() == 1) { //

                }
            }
            BMSProtocalV2.FUN_CODE_RS485_GET.toByte() -> {  // 获取 RS485
                handleSettingProtocol(data, BMSProtocalV2.FUN_CODE_RS485_GET)
            }
            BMSProtocalV2.FUN_CODE_RS485_SET.toByte() -> {  // Set RS485
                if (data[3].toInt() == 0) {
                    if (lastSetRS485Id != -1) {
                        if (_rs485Protocol.value != null) {
                            var protocolData = SettingsProtocolData(
                                lastSetRS485Id,
                                _rs485Protocol.value!!.protocolArray
                            )
                            _rs485Protocol.value = protocolData
                        }
                        lastSetRS485Id = -1
                    }
                } else if (data[3].toInt() == 1) { //

                }
            }
            BMSProtocalV2.FUN_CODE_CAN_GET.toByte() -> {     // 获取CAN
                handleSettingProtocol(data, BMSProtocalV2.FUN_CODE_CAN_GET)
            }
            BMSProtocalV2.FUN_CODE_CAN_SET.toByte() -> {     // Set CAN
                if (data[3].toInt() == 0) {
                    if (lastSetCanId != -1) {
                        if (_canData.value != null) {
                            var protocolData =
                                SettingsProtocolData(lastSetCanId, _canData.value!!.protocolArray)
                            _canData.value = protocolData
                        }
                        lastSetCanId = -1
                    }
                } else if (data[3].toInt() == 1) { //

                }
            }
        }
    }


    private fun handleSettingProtocol(data: ByteArray, type: Int) {
        var selectedIndex = data[3].toInt()
        var protocolDataSize = data[4].toInt()
        var protocolArray = ArrayList<String>(protocolDataSize)
        for (i in 0 until protocolDataSize) {
            // 50 30 31 2D 47 52 57 00 00 00
            var length = 0
            while(data[5 + i*10 + length].toInt() != 0 && length <= 10) {
                length++
            }
            protocolArray.add(i, String(data, 5 + i * 10, length))
        }
        if (type == BMSProtocalV2.FUN_CODE_RS485_GET) {
            _rs485Protocol.value = SettingsProtocolData(selectedIndex, protocolArray)
        } else if (type == BMSProtocalV2.FUN_CODE_CAN_GET) {
            _canData.value = SettingsProtocolData(selectedIndex, protocolArray)
        }
    }


    /**
     *
     */
    private fun sendBLEControlData(device: BleDevice, uuid: ZetaraBleUUID, opHex: String) {
        BleManager.getInstance()
            .write(device, uuid.primaryServiceUUID.toString(), uuid.writeUUID.toString(),
                HexUtil.hexStringToBytes(opHex),
                object : BleWriteCallback() {
                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                        FL.i("MainViewModel", "write $opHex success")
                    }

                    override fun onWriteFailure(exception: BleException?) {
                        FL.i("MainViewModel", "write $opHex error")
                    }
                })
    }


    private fun getCurrentTime(): String {
        val date = Date(System.currentTimeMillis())
        return timeFormat.format(date)
    }
}
