package com.zetarapower.monitor.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import at.grabner.circleprogress.CircleProgressView
import com.zetarapower.monitor.MainActivity
import com.zetarapower.monitor.R
import com.zetarapower.monitor.logic.BMSData
import com.zetarapower.monitor.ui.viewmodel.MainViewModel
import com.zetarapower.monitor.ui.widgets.BatteryView

/**
 *
 * Main Fragments
 */
class MainFragment : Fragment() {

    private var mainViewModel: MainViewModel? = null

    private lateinit var deviceLogo: ImageView
    private lateinit var chargingImage: ImageView
    private lateinit var circleView: CircleProgressView
    private lateinit var batteryView: BatteryView

    private lateinit var voltageView: TextView
    private lateinit var currentView: TextView
    private lateinit var tempView: TextView

    private lateinit var deviceStatusView: TextView
    private lateinit var bluetoothStatusView: TextView
    private lateinit var titleView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = activity?.let { ViewModelProviders.of(it)[MainViewModel::class.java] }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)
        titleView = root.findViewById(R.id.bluetooth_device_name)
        bluetoothStatusView = root.findViewById(R.id.device_status_text)
        chargingImage = root.findViewById(R.id.charging_img)
        circleView = root.findViewById(R.id.circleView)
        voltageView = root.findViewById(R.id.voltageValue)
        currentView = root.findViewById(R.id.currentValue)
        deviceStatusView = root.findViewById(R.id.statusValue)
        tempView = root.findViewById(R.id.tempValue)
        batteryView = root.findViewById(R.id.batteryView)
        deviceLogo = root.findViewById(R.id.device_logo)
        deviceLogo.setOnClickListener {
            if (activity != null) {
                (activity as MainActivity).showScanDialog()
            }
        }

        mainViewModel?.data?.observe(viewLifecycleOwner, Observer<BMSData> {
            circleView?.setValue(it.soc.toFloat())
            voltageView?.text = "${it.voltage}V"
            currentView?.text = "${it.current}A"
            var tempF = (32 + it.tempEnv * 1.8).toInt()
            tempView?.text = "${it.tempEnv}°C/${tempF}℉"
            deviceStatusView?.text = when (it.status) {
                0 -> "Stand by"
                1 -> "Charging..."
                2 -> "DisCharging"
                4 -> "Protect"
                8 -> "Charging Lmt"
                else -> "Stand by"
            }
            if (it.status == 1) {
                chargingImage?.visibility = View.VISIBLE
            } else {
                chargingImage?.visibility = View.GONE
            }
            batteryView.setPower(it.soc)
        })

        mainViewModel?.updateStatus?.observe(viewLifecycleOwner, Observer<String> {
            bluetoothStatusView.text = it?.toString()
        })

        mainViewModel?.connectedDeviceName?.observe(viewLifecycleOwner, Observer<String> {
            titleView.text = it?.toString() ?: ""
        })
        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }

}