package com.zetarapower.monitor.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.zetarapower.monitor.R
import com.zetarapower.monitor.logic.BMSData
import com.zetarapower.monitor.ui.viewmodel.MainViewModel

/**
 *
 * Info Fragment
 *
 */
class InfoFragment : Fragment() {

    private  var infoViewModel: MainViewModel? = null

    private lateinit var tempPcbText: TextView

    private lateinit var tempCell1Text: TextView
    private lateinit var tempCell2Text: TextView
    private lateinit var tempCell3Text: TextView
    private lateinit var tempCell4Text: TextView

    private lateinit var cell3View: View
    private lateinit var cell4View: View


    /**
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        infoViewModel = activity?.let { ViewModelProviders.of(it)[MainViewModel::class.java] }
    }


    /**
     *
     */
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_info, container, false)
        val gridLayout: GridLayout = root.findViewById(R.id.grid_layout)
        tempPcbText = root.findViewById(R.id.pcb_temp)
        tempCell1Text = root.findViewById(R.id.cell1_temp)
        tempCell2Text = root.findViewById(R.id.cell2_temp)
        tempCell3Text = root.findViewById(R.id.cell3_temp)
        tempCell4Text = root.findViewById(R.id.cell4_temp)

        cell3View = root.findViewById(R.id.cell3)
        cell4View = root.findViewById(R.id.cell4)

        infoViewModel?.data?.observe(viewLifecycleOwner, Observer<BMSData> {
            //
            var cellCount = it.cellCount
            if (cellCount > gridLayout.childCount) cellCount = gridLayout.childCount

            for (i in 0 until cellCount) {
                (gridLayout[i] as TextView).visibility = View.VISIBLE
            }
            for (i in cellCount until gridLayout.childCount){
                (gridLayout[i] as TextView).visibility = View.GONE
            }
            for (i in 0 until it.cellCount) {
                (gridLayout[i] as TextView).text =
                    String.format("%.3f", it.cellVoltages[i]) + "V"
            }

            // temp
            var tempF = (32+it.tempPCB*1.8).toInt()
            tempPcbText?.text = "${it.tempPCB}°C/${tempF}℉"

            var cellTemp1F = (32+it.cellTempArray[0]*1.8).toInt()
            tempCell1Text?.text = "${it.cellTempArray[0]}°C/${cellTemp1F}℉"

            var cellTemp2F = (32+it.cellTempArray[1]*1.8).toInt()
            tempCell2Text?.text = "${it.cellTempArray[1]}°C/${cellTemp2F}℉"

            if (it.cellCount == 16) {
                cell3View.visibility = View.VISIBLE
                cell4View.visibility = View.VISIBLE

                var cellTemp3F = (32+it.cellTempArray[2]*1.8).toInt()
                tempCell3Text?.text = "${it.cellTempArray[2]}°C/${cellTemp3F}℉"

                var cellTemp4F = (32+it.cellTempArray[3]*1.8).toInt()
                tempCell4Text?.text = "${it.cellTempArray[3]}°C/${cellTemp4F}℉"
            }
        })
        return root
    }



    companion object {
        @JvmStatic
        fun newInstance(): InfoFragment {
            return InfoFragment()
        }
    }
}