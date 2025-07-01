package com.zetarapower.monitor

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.bosphere.filelogger.FL
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.google.android.material.bottomnavigation.BottomNavigationView
// Временно отключено из-за проблем с доступом к JitPack
// import com.jaeger.library.StatusBarUtil
import com.zetarapower.monitor.bluetooth.ZetaraBleUUID
import com.zetarapower.monitor.ui.fragment.ConnectCallback
import com.zetarapower.monitor.ui.fragment.MainFragment
import com.zetarapower.monitor.ui.fragment.ScanFragment
import com.zetarapower.monitor.ui.fragment.SplashFragment
import com.zetarapower.monitor.ui.viewmodel.MainViewModel
import com.zetarapower.monitor.utils.showAlertDialog
import com.zetarapower.monitor.utils.showToast

/**
 *
 */
class MainActivity : AppCompatActivity() {

    private var scanDialog: ScanFragment? = null
    private var splashFragment: SplashFragment? = null
    private var mainViewModel: MainViewModel? = null
    private var handler: Handler = Handler(Looper.getMainLooper())

    var previousMenuItem: MenuItem? = null

    private var getBmsRunnable = object : Runnable {
        override fun run() {
            getMainBMSData()
            handler.postDelayed(this, DELAY_TIME)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FL.i("MainActivity", "onCreate")
        setContentView(R.layout.activity_main)
        
        // Настройка статус-бара нативными Android API
        setupStatusBar()
        
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.itemIconTintList = null
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        previousMenuItem = navView.menu.findItem(R.id.navigation_home)
        navView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_shop -> {
                    // Открываем браузер с сайтом Big Battery вместо навигации к фрагменту
                    openBigBatteryWebsite()
                    false // Не выполняем навигацию
                }
                else -> {
                    // Обычная навигация для других пунктов меню
                    previousMenuItem = menuItem
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                }
            }
        }

        if (getString(R.string.splash_screen) == "true") {
            addSplash()
        }
        mainViewModel = ViewModelProviders.of(this)[MainViewModel::class.java]

        mainViewModel?.updateStatus?.observe(this, Observer<String> {
            val deviceConnected = "Device Not Connected" != it?.toString()
            if (!deviceConnected) {
                resetTimerData()
            } else {
                handler.removeCallbacks(getBmsRunnable)
                getBMSDataInternal()
            }
        })
    }


    /**
     *
     */
    private fun addSplash() {
        splashFragment = SplashFragment.newInstance()
        var transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.splash, splashFragment!!).commitAllowingStateLoss()
        removeSplash()
    }


    /**
     *
     */
    private fun removeSplash() {
        if (splashFragment != null) {
            handler.postDelayed(Runnable {
                var transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.remove(splashFragment!!)
                transaction.commitAllowingStateLoss()
                splashFragment = null
            }, 1500)
        }
    }

    /**
     *
     */
    private fun getBMSDataInternal() {
        handler.postDelayed(getBmsRunnable, DELAY_TIME)
    }


    /**
     *
     */
    private fun resetTimerData() {
        handler.removeCallbacks(getBmsRunnable) //停止刷新
    }

    fun getMainBMSData() {
        if (BleManager.getInstance().allConnectedDevice != null
            && BleManager.getInstance().allConnectedDevice.isNotEmpty()
        ) {
            mainViewModel?.getBMSData(BleManager.getInstance().allConnectedDevice[0], null)
        }
    }

    fun getSettingData(delayTime: Long = 0L) {
        var delay = delayTime
        if (getString(R.string.settings_module_ID) == "true") {
            if (mainViewModel?.selectedId?.value == -1) {
                handler.postDelayed({
                    mainViewModel?.getBMSModuleIdData(
                        BleManager.getInstance().allConnectedDevice[0],
                        null
                    )
                }, delay)
                delay += 600L
            }
        }

        if (getString(R.string.settings_module_CAN) == "true") {
            if (mainViewModel?.canData?.value == null) {
                handler.postDelayed(Runnable {
                    mainViewModel?.getCanData(BleManager.getInstance().allConnectedDevice[0], null)
                }, delay)
                delay += 600L
            }
            if (mainViewModel?.rs485Protocol?.value == null) {
                handler.postDelayed(Runnable {
                    mainViewModel?.getRS485Data(BleManager.getInstance().allConnectedDevice[0], null)
                }, delay)
            }
        }
    }

    fun startTimer() {
        handler.postDelayed(getBmsRunnable, DELAY_TIME)
    }

    fun stopTimer() {
        handler.removeCallbacks(getBmsRunnable) //停止刷新
    }

    override fun onResume() {
        super.onResume()
        FL.i("MainActivity", "onResume")
        if (BleManager.getInstance().allConnectedDevice != null
            && BleManager.getInstance().allConnectedDevice.isNotEmpty()
        ) {
            FL.i("MainActivity", "onResume startTimer")
            mainViewModel?.removeNotifyStatus(BleManager.getInstance().allConnectedDevice[0])
            if (!BleManager.getInstance().isConnected(BleManager.getInstance().allConnectedDevice[0])) {
                FL.i("MainActivity", "onResume disConnected")
                BleManager.getInstance().connect(BleManager.getInstance().allConnectedDevice[0], object : BleGattCallback() {
                    override fun onStartConnect() {

                    }
                    override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {

                    }
                    override fun onConnectSuccess(
                        bleDevice: BleDevice?,
                        gatt: BluetoothGatt?,
                        status: Int
                    ) {
                        FL.i("MainActivity", "onResume connected success")
                        BleManager.getInstance()
                            .setMtu(bleDevice, 510, object : BleMtuChangedCallback() {
                                override fun onSetMTUFailure(exception: BleException) {
                                    FL.e(TAG, "SetMTUFailure:$exception")
                                    BleManager.getInstance().disconnect(bleDevice)
                                }
                                override fun onMtuChanged(mtu: Int) {
                                    FL.i(TAG, "onMtuChanged: $mtu")
                                }
                            })
                    }
                    override fun onDisConnected(
                        isActiveDisConnected: Boolean,
                        device: BleDevice?,
                        gatt: BluetoothGatt?,
                        status: Int
                    ) {

                    }
                })
            }
            startTimer()
        }
    }

    override fun onPause() {
        super.onPause()
        FL.i("MainActivity", "onPause stopTimer")
        stopTimer()
    }

    /**
     *
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String?>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_LOCATION -> if (grantResults.isNotEmpty()) {
                var permissionSize = permissions.size
                var permissionGrantedSize = 0
                var i = 0
                while (i < grantResults.size) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        permissionGrantedSize++
                    }
                    i++
                }
                if (permissionSize == permissionGrantedSize) {
                    realShowScanDialog()
                }
            }
        }
    }

    /**
     *  Step1: check blue permission
     *  Step2: check location permission
     */
    private fun checkPermissions() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show()
            return
        }
        //
        val permissions: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        val permissionDeniedList: MutableList<String> = ArrayList()
        for (permission in permissions) {
            val permissionCheck: Int = ContextCompat.checkSelfPermission(this, permission)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionDeniedList.add(permission)
            }
        }
        if (permissionDeniedList.isNotEmpty()) {
            val deniedPermissions = permissionDeniedList.toTypedArray()
            ActivityCompat.requestPermissions(
                this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION
            )
        } else {
            realShowScanDialog()
        }
    }

    /**
     *  Check open GPS
     */
    private fun onPermissionGranted(permission: String?) {
        when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.notifyTitle)
                    .setMessage(R.string.gpsNotifyMsg)
                    .setNegativeButton(
                        R.string.cancel
                    ) { _, _ -> finish() }
                    .setPositiveButton(
                        R.string.setting
                    ) { _, _ ->
                        val intent =
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
                    }
                    .setCancelable(false)
                    .show()
            } else {
                realShowScanDialog()
            }
        }
    }


    /**
     *
     */
    fun showScanDialog() {
        checkPermissions()
    }

    /**
     *
     */
    private fun realShowScanDialog() {
        //新增弹出列表,先初始化
        if (scanDialog == null) {
            scanDialog = ScanFragment().apply {
                setConnectCallback(object : ConnectCallback {
                    override fun onConnected(bleDevice: BleDevice, uuid: ZetaraBleUUID?) {
                        mainViewModel?.getBMSData(bleDevice, uuid)
                    }

                    override fun onDisconnected(bleDevice: BleDevice) {
                        mainViewModel?.disConnectDevice(bleDevice)
                    }
                })
            }
        }
        scanDialog?.show(supportFragmentManager, "scanDialog")
    }


    /**
     *
     */
    private fun checkGPSIsOpen(): Boolean {
        val locationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    /**
     *
     */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                realShowScanDialog()
            }
        }
    }


    /**
     *
     */
    override fun onBackPressed() {
        if (scanDialog?.dialog?.isShowing == true) {
            scanDialog?.dismiss()
            return
        }
        showExitAlertDialog()
    }


    override fun onDestroy() {
        super.onDestroy()
        BleManager.getInstance().disconnectAllDevice()
        BleManager.getInstance().destroy()
        FL.i("MainActivity", "onDestroy")
    }

    /**
     *
     */
    private fun showExitAlertDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage("Are you sure to exit？")
            .setPositiveButton("Yes") { _, _ ->
                finish()
            }
            .setNegativeButton("Cancel") { _, _ ->

            }.show()
    }

    /**
     * Открывает сайт Big Battery в браузере
     * Соответствует поведению iOS версии приложения
     */
    private fun openBigBatteryWebsite() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bigbattery.com"))
            startActivity(intent)
        } catch (e: Exception) {
            // Обработка ошибки открытия браузера
            Toast.makeText(this, "Unable to open website", Toast.LENGTH_SHORT).show()
            FL.e(TAG, "Error opening Big Battery website: $e")
        }
    }

    /**
     * Настройка статус-бара нативными Android API
     * Заменяет функциональность StatusBarUtil
     */
    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Делаем статус-бар прозрачным
            window.decorView.systemUiVisibility = 
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or 
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Проверяем настройку темного режима
                if (getString(R.string.dark_mode) == "true") {
                    // Темный режим - светлые иконки статус-бара
                    window.decorView.systemUiVisibility = 
                        window.decorView.systemUiVisibility and 
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                } else {
                    // Светлый режим - темные иконки статус-бара
                    window.decorView.systemUiVisibility = 
                        window.decorView.systemUiVisibility or 
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
        }
    }


    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSION_LOCATION = 0
        private const val REQUEST_CODE_OPEN_GPS = 1
        const val DELAY_TIME: Long = 1500
    }
}
