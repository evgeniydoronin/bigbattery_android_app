package com.zetarapower.monitor.ui.adapter

/**
 * Created by Evgenii Doronin
 * LinkedIn: https://www.linkedin.com/in/evgeniydoronin
 * 
 * Адаптер для отображения напряжений ячеек батареи в сетке
 */

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zetarapower.monitor.R

/**
 * Модель данных для ячейки батареи
 */
data class CellVoltageItem(
    val cellNumber: Int,
    val voltage: Float
)

/**
 * Адаптер для отображения ячеек батареи в RecyclerView
 */
class CellVoltageAdapter : RecyclerView.Adapter<CellVoltageAdapter.CellVoltageViewHolder>() {

    private var cellVoltages: List<CellVoltageItem> = emptyList()

    /**
     * Обновление данных ячеек - показывает 16 ячеек с прочерками по умолчанию
     */
    fun updateCellVoltages(voltages: FloatArray, cellCount: Int) {
        val items = mutableListOf<CellVoltageItem>()
        
        // Проверяем, есть ли реальные данные в ячейках
        val hasRealData = voltages.any { it > 0 }
        
        // Всегда показываем минимум 16 ячеек для единообразия UI (4 ряда по 4)
        val displayCellCount = maxOf(cellCount, 16)
        
        // Создаем ячейки
        for (i in 0 until displayCellCount) {
            val voltage = if (i < voltages.size && voltages[i] > 0) {
                // Реальные данные
                voltages[i]
            } else {
                // Placeholder - отрицательное значение для обозначения прочерка
                -1f
            }
            items.add(CellVoltageItem(i + 1, voltage))
        }
        
        cellVoltages = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellVoltageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cell_voltage, parent, false)
        return CellVoltageViewHolder(view)
    }

    override fun onBindViewHolder(holder: CellVoltageViewHolder, position: Int) {
        holder.bind(cellVoltages[position])
    }

    override fun getItemCount(): Int = cellVoltages.size

    /**
     * ViewHolder для ячейки батареи
     */
    class CellVoltageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val cellVoltageValue: TextView = itemView.findViewById(R.id.cell_voltage_value)
        private val cellNumber: TextView = itemView.findViewById(R.id.cell_number)

        /**
         * Привязка данных к view элементам
         */
        fun bind(item: CellVoltageItem) {
            // Форматируем напряжение - прочерки для placeholder'ов
            if (item.voltage < 0) {
                // Placeholder - показываем прочерки
                cellVoltageValue.text = "-- V"
            } else {
                // Реальные данные - форматируем с 2 знаками после запятой
                cellVoltageValue.text = String.format("%.2f V", item.voltage)
            }
            
            // Устанавливаем номер ячейки
            cellNumber.text = "Cell ${item.cellNumber}"
        }
    }
}
