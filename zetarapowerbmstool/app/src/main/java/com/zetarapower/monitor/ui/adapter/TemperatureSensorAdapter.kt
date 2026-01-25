package com.zetarapower.monitor.ui.adapter

/**
 * Created by Evgenii Doronin
 * LinkedIn: https://www.linkedin.com/in/evgeniydoronin
 * 
 * Адаптер для отображения температурных датчиков (копия iOS реализации)
 */

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zetarapower.monitor.R

/**
 * Модель данных для температурного датчика
 */
data class TemperatureSensorItem(
    val sensorType: SensorType,
    val sensorNumber: Int,
    val temperatureCelsius: Int,
    val temperatureFahrenheit: Int
) {
    enum class SensorType {
        PCB,        // PCB Temperature
        CELL        // Cell Temperature
    }
}

/**
 * Адаптер для отображения температурных датчиков в RecyclerView
 */
class TemperatureSensorAdapter : RecyclerView.Adapter<TemperatureSensorAdapter.TemperatureSensorViewHolder>() {

    private var temperatureSensors: List<TemperatureSensorItem> = emptyList()

    /**
     * Обновление данных температурных датчиков - показывает датчики с прочерками по умолчанию
     */
    fun updateTemperatureSensors(pcbTemp: Byte, cellTemps: ByteArray) {
        val items = mutableListOf<TemperatureSensorItem>()
        
        // Конвертация температуры из Celsius в Fahrenheit
        fun celsiusToFahrenheit(celsius: Int): Int {
            return (celsius * 9 / 5) + 32
        }
        
        // Проверяем, есть ли реальные данные
        val pcbTempInt = pcbTemp.toInt()
        val hasRealPcbData = pcbTempInt != 0
        val hasRealCellData = cellTemps.any { it.toInt() != 0 }
        val hasRealData = hasRealPcbData || hasRealCellData
        
        if (hasRealData) {
            // Есть реальные данные - показываем только валидные датчики
            if (hasRealPcbData) {
                items.add(
                    TemperatureSensorItem(
                        sensorType = TemperatureSensorItem.SensorType.PCB,
                        sensorNumber = 0,
                        temperatureCelsius = pcbTempInt,
                        temperatureFahrenheit = celsiusToFahrenheit(pcbTempInt)
                    )
                )
            }
            
            for (i in cellTemps.indices) {
                val cellTempInt = cellTemps[i].toInt()
                if (cellTempInt != 0) {
                    items.add(
                        TemperatureSensorItem(
                            sensorType = TemperatureSensorItem.SensorType.CELL,
                            sensorNumber = i + 1,
                            temperatureCelsius = cellTempInt,
                            temperatureFahrenheit = celsiusToFahrenheit(cellTempInt)
                        )
                    )
                }
            }
        } else {
            // Нет реальных данных - показываем датчики с прочерками
            // PCB Temperature
            items.add(
                TemperatureSensorItem(
                    sensorType = TemperatureSensorItem.SensorType.PCB,
                    sensorNumber = 0,
                    temperatureCelsius = -1, // Placeholder для прочерков
                    temperatureFahrenheit = -1
                )
            )
            
            // 4 Cell Temperature датчика (как в iOS)
            for (i in 1..4) {
                items.add(
                    TemperatureSensorItem(
                        sensorType = TemperatureSensorItem.SensorType.CELL,
                        sensorNumber = i,
                        temperatureCelsius = -1, // Placeholder для прочерков
                        temperatureFahrenheit = -1
                    )
                )
            }
        }
        
        temperatureSensors = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemperatureSensorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_temperature_sensor, parent, false)
        return TemperatureSensorViewHolder(view)
    }

    override fun onBindViewHolder(holder: TemperatureSensorViewHolder, position: Int) {
        holder.bind(temperatureSensors[position])
    }

    override fun getItemCount(): Int = temperatureSensors.size

    /**
     * ViewHolder для температурного датчика
     */
    class TemperatureSensorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val temperatureIcon: ImageView = itemView.findViewById(R.id.temperature_icon)
        private val sensorTitle: TextView = itemView.findViewById(R.id.sensor_title)
        private val temperatureValue: TextView = itemView.findViewById(R.id.temperature_value)

        /**
         * Привязка данных к view элементам
         */
        fun bind(item: TemperatureSensorItem) {
            when (item.sensorType) {
                TemperatureSensorItem.SensorType.PCB -> {
                    // PCB Temperature - используем иконку internal_temperature
                    temperatureIcon.setImageResource(R.drawable.internal_temperature)
                    sensorTitle.text = "PCB Temperature"
                }
                TemperatureSensorItem.SensorType.CELL -> {
                    // Cell Temperature - используем обычную иконку temperature
                    temperatureIcon.setImageResource(R.drawable.temperature)
                    sensorTitle.text = "Temp. Sensor #${item.sensorNumber}"
                }
            }
            
            // Форматируем температуру - прочерки для placeholder'ов
            if (item.temperatureCelsius < 0 || item.temperatureFahrenheit < 0) {
                // Placeholder - показываем прочерки
                temperatureValue.text = "-- °F / -- °C"
            } else {
                // Реальные данные - форматируем как в iOS: "75°F / 24°C"
                temperatureValue.text = "${item.temperatureFahrenheit}°F / ${item.temperatureCelsius}°C"
            }
        }
    }
}
