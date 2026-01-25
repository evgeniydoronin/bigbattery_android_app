# Исправление отображения данных "total" на главном экране

## Проблема
На главном экране приложения есть 3 карточки с данными "total" (Total Voltage, Total Current, Total Temperature), которые не отображали информацию при подключении к реальному устройству, хотя все табы (Summary, Cell Voltage, Temperature) работали корректно.

## Причина проблемы
В коде MainFragmentNew.kt отсутствовали:
1. Переменные для TextView элементов карточек
2. Инициализация этих переменных
3. Код обновления значений при получении данных от BMS

В layout файле TextView элементы не имели ID для доступа из кода.

## Решение

### 1. Добавлены ID в layout файл (fragment_main_new.xml)
```xml
<!-- Total Voltage Card -->
<TextView
    android:id="@+id/total_voltage_value"
    ...
    android:text="-- V" />

<!-- Total Current Card -->
<TextView
    android:id="@+id/total_current_value"
    ...
    android:text="-- A" />

<!-- Total Temperature Card -->
<TextView
    android:id="@+id/total_temperature_value"
    ...
    android:text="-- °F/-- °C" />
```

### 2. Добавлены переменные в MainFragmentNew.kt
```kotlin
// Parameters Cards - основные значения
private lateinit var totalVoltageValue: TextView
private lateinit var totalCurrentValue: TextView
private lateinit var totalTemperatureValue: TextView
```

### 3. Добавлена инициализация в методе initViews()
```kotlin
// Parameters Cards - основные значения
totalVoltageValue = root.findViewById(R.id.total_voltage_value)
totalCurrentValue = root.findViewById(R.id.total_current_value)
totalTemperatureValue = root.findViewById(R.id.total_temperature_value)
```

### 4. Добавлен метод обновления данных
```kotlin
/**
 * Обновление основных параметров в карточках на главном экране
 */
private fun updateTotalParametersCards(data: BMSData) {
    // Total Voltage
    if (data.voltage > 0) {
        totalVoltageValue.text = "${String.format("%.2f", data.voltage)}V"
    } else {
        totalVoltageValue.text = "-- V"
    }

    // Total Current
    if (data.current != 0f) {
        totalCurrentValue.text = "${String.format("%.2f", data.current)}A"
    } else {
        totalCurrentValue.text = "-- A"
    }

    // Total Temperature - используем tempEnv (температура окружающей среды)
    if (data.tempEnv.toInt() != 0) {
        // Конвертируем из Fahrenheit в Celsius для отображения обеих единиц
        val tempF = data.tempEnv.toInt()
        val tempC = ((tempF - 32) * 5 / 9)
        totalTemperatureValue.text = "${tempF}°F/${tempC}°C"
    } else {
        totalTemperatureValue.text = "-- °F/-- °C"
    }
}
```

### 5. Добавлен вызов метода в updateBMSData()
```kotlin
private fun updateBMSData(data: BMSData) {
    // ...
    // Обновление основных параметров в карточках
    updateTotalParametersCards(data)
    // ...
}
```

## Результат
Теперь при подключении к реальному устройству 3 карточки на главном экране будут отображать:
- **Total Voltage**: реальное напряжение батареи в вольтах (например, "12.34V")
- **Total Current**: реальный ток батареи в амперах (например, "5.67A")
- **Total Temperature**: температура в Fahrenheit и Celsius (например, "75°F/24°C")

## Файлы изменены
1. `zetarapowerbmstool/app/src/main/res/layout/fragment_main_new.xml`
2. `zetarapowerbmstool/app/src/main/java/com/zetarapower/monitor/ui/fragment/MainFragmentNew.kt`

## Дата исправления
16 июля 2025 г.
