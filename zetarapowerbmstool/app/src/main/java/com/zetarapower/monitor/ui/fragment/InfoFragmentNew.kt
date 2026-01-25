package com.zetarapower.monitor.ui.fragment

/**
 * Created by Evgenii Doronin
 * LinkedIn: https://www.linkedin.com/in/evgeniydoronin
 * 
 * Современный фрагмент деталей с фокусом на температуры
 * Cell Voltage скрыт как в iOS версии
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.zetarapower.monitor.R
import com.zetarapower.monitor.logic.BMSData
import com.zetarapower.monitor.ui.viewmodel.MainViewModel

/**
 * Современный фрагмент деталей с Big Battery дизайном
 */
class InfoFragmentNew : Fragment() {

    private var infoViewModel: MainViewModel? = null

    // Temperature Views
    private lateinit var tempPcbText: TextView
    private lateinit var tempCell1Text: TextView
    private lateinit var tempCell2Text: TextView
    private lateinit var tempCell3Text: TextView
    private lateinit var tempCell4Text: TextView

    // Card Views для дополнительных температур
    private lateinit var cell3View: CardView
    private lateinit var cell4View: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        infoViewModel = activity?.let { ViewModelProviders.of(it)[MainViewModel::class.java] }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_info_new, container, false)
        
        initViews(root)
        observeViewModel()
        
        return root
    }

    /**
     * Инициализация view элементов
     */
    private fun initViews(root: View) {
        // Temperature TextViews
        tempPcbText = root.findViewById(R.id.pcb_temp)
        tempCell1Text = root.findViewById(R.id.cell1_temp)
        tempCell2Text = root.findViewById(R.id.cell2_temp)
        tempCell3Text = root.findViewById(R.id.cell3_temp)
        tempCell4Text = root.findViewById(R.id.cell4_temp)

        // Card Views для дополнительных температур
        cell3View = root.findViewById(R.id.cell3)
        cell4View = root.findViewById(R.id.cell4)
    }

    /**
     * Подписка на изменения в ViewModel
     */
    private fun observeViewModel() {
        infoViewModel?.data?.observe(viewLifecycleOwner, Observer<BMSData> { bmsData ->
            updateTemperatureData(bmsData)
        })
    }

    /**
     * Обновление данных температур
     */
    private fun updateTemperatureData(data: BMSData) {
        // PCB Temperature
        val tempPcbF = (32 + data.tempPCB * 1.8).toInt()
        tempPcbText.text = "${data.tempPCB}°C/${tempPcbF}℉"

        // Cell Temperature 1
        val cellTemp1F = (32 + data.cellTempArray[0] * 1.8).toInt()
        tempCell1Text.text = "${data.cellTempArray[0]}°C/${cellTemp1F}℉"

        // Cell Temperature 2
        val cellTemp2F = (32 + data.cellTempArray[1] * 1.8).toInt()
        tempCell2Text.text = "${data.cellTempArray[1]}°C/${cellTemp2F}℉"

        // Показать дополнительные температуры для 16-ячеечных батарей
        if (data.cellCount == 16) {
            cell3View.visibility = View.VISIBLE
            cell4View.visibility = View.VISIBLE

            // Cell Temperature 3
            val cellTemp3F = (32 + data.cellTempArray[2] * 1.8).toInt()
            tempCell3Text.text = "${data.cellTempArray[2]}°C/${cellTemp3F}℉"

            // Cell Temperature 4
            val cellTemp4F = (32 + data.cellTempArray[3] * 1.8).toInt()
            tempCell4Text.text = "${data.cellTempArray[3]}°C/${cellTemp4F}℉"
        } else {
            // Скрыть дополнительные температуры для меньших батарей
            cell3View.visibility = View.GONE
            cell4View.visibility = View.GONE
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): InfoFragmentNew {
            return InfoFragmentNew()
        }
    }
}
