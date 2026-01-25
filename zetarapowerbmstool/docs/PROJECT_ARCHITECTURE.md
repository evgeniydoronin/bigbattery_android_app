# Архитектура Android проекта "Zetara Power BMS Tool"

## 1. Общая информация

- **Package**: `com.zetarapower.monitor.bl`
- **Version**: 3.0.2 (versionCode 14)
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 35 (Android 15)
- **Язык**: Kotlin 1.8.10

---

## 2. Архитектура приложения

### Паттерн: MVVM (Model-View-ViewModel)

```
┌─────────────────────────────────┐
│      MainActivity (Host)         │
│  - BottomNavigationView         │
│  - NavController                │
│  - BLE Lifecycle Management     │
└──────────────┬──────────────────┘
               │
      ┌────────┴───────────┬─────────────┬──────────────┐
      │                    │             │              │
┌─────▼─────┐  ┌──────────▼──┐  ┌──────▼───┐  ┌──────▼───┐
│  MainFrag │  │  ScanFrag   │  │SettingFrag│ │InfoFrag   │
│  (Tabs)   │  │  (Dialog)   │  │          │  │          │
└─────┬─────┘  └──────┬──────┘  └──────┬───┘  └──────┬───┘
      │               │                │            │
      └───────────────┴────────────────┴────────────┘
                      │
            ┌─────────▼──────────┐
            │  MainViewModel     │
            │  - LiveData<BMSData>│
            │  - LiveData<Status>│
            │  - BLE Methods     │
            └─────────┬──────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
   ┌────▼────┐  ┌────▼─────┐  ┌───▼──────┐
   │BMSSplitter│ │BMSProtocol│ │PowerMonitor│
   │(Parsing)  │ │(Parsing)  │ │BlueTooth  │
   └────┬────┘  └──────────┘  └───────────┘
        │
   ┌────▼────────────┐
   │   FastBleLib    │
   │  (BLE Library)  │
   └─────────────────┘
```

### Компоненты:

- **Model**: `BMSData`, `SettingsProtocolData`
- **View**: Fragment'ы (`MainFragmentNew`, `ScanFragmentNew`, `SettingsFragmentNew`, `InfoFragmentNew`)
- **ViewModel**: `MainViewModel` (управляет состоянием и логикой)
- **LiveData**: для реактивного обновления UI

---

## 3. Структура файлов

```
com.zetarapower.monitor/
├── MainActivity.kt                    # Main Activity, навигация, BLE lifecycle
├── app/
│   └── PowerMonitorApp.kt            # Application класс
├── bluetooth/
│   ├── ZetaraBleUUID.kt              # Data class для UUID
│   └── BleUUIDs.kt (battery flavor)  # Массив поддерживаемых UUID
├── logic/
│   ├── BMSData.kt                    # Model: данные батареи
│   ├── BMSDataReadyCallback.kt       # Callback интерфейс
│   ├── BMSProtocalV2.kt              # Протокол парсинга BMS
│   ├── BMSSplitter.kt                # Разделитель мультифреймов
│   ├── CRC.kt                        # CRC16 верификация
│   ├── PowerMonitorBlueTooth.kt      # BLE менеджер (singleton)
│   └── SettingsProtocolData.kt       # Данные протокола настроек
├── ui/
│   ├── adapter/
│   │   ├── CellVoltageAdapter.kt     # Адаптер сетки ячеек
│   │   ├── CellVoltageItemDecoration.kt
│   │   └── TemperatureSensorAdapter.kt
│   ├── fragment/
│   │   ├── MainFragmentNew.kt        # Главный экран (3 таба)
│   │   ├── ScanFragmentNew.kt        # Сканирование BLE
│   │   ├── SettingsFragmentNew.kt    # Настройки
│   │   ├── InfoFragmentNew.kt        # Информация
│   │   └── ConnectCallback.kt        # Callback подключения
│   ├── viewmodel/
│   │   └── MainViewModel.kt          # MVVM ViewModel
│   └── widgets/
│       ├── ArcProgressView.kt        # Дуга прогресса SOC
│       ├── BatteryView.kt            # Визуализация батареи
│       └── DashboardView.kt          # Приборная панель
└── utils/
    └── Extensions.kt                 # Extension функции
```

---

## 4. BLE (Bluetooth Low Energy)

### UUID конфигурация

Два набора UUID для разных типов устройств:

```kotlin
// Набор 1
Primary: 00001000-0000-1000-8000-00805f9b34fb
Write:   00001001-0000-1000-8000-00805f9b34fb
Notify:  00001002-0000-1000-8000-00805f9b34fb

// Набор 2
Primary: 00001006-0000-1000-8000-00805f9b34fb
Write:   00001008-0000-1000-8000-00805f9b34fb
Notify:  00001007-0000-1000-8000-00805f9b34fb
```

### Протокол запросов

```kotlin
OPERATION_GETBMS_HEX = "01030000002705d0"  // Запрос BMS данных
OPERATION_GETIDS = "1002007165"             // Запрос Module ID
OPERATION_GETCAN = "10040072C5"             // Запрос CAN протокола
OPERATION_GETRS485 = "10030070F5"           // Запрос RS485 протокола
```

### Периодический опрос

- Интервал: **1500ms** (1.5 секунды)
- MTU: **510 байт** (максимум для BLE)

### Поток данных

```
BLE Device → Notify Callback → handleResponseData() → BMSSplitter → BMSData → LiveData → UI
```

---

## 5. Парсинг данных BMS

### Структура ответа (минимум 78 байт)

```
Индекс  | Описание              | Размер
--------|----------------------|--------
0       | Адрес BMS            | 1 байт
1       | Function Code        | 1 байт
2       | Data Length          | 1 байт
3-75    | Данные батареи       | 73 байта
76-77   | CRC16 checksum       | 2 байта
```

### Распределение данных

```
0-1     : Суммарное напряжение (÷100 = V)
2-3     : Ток (÷10 = A)
4-35    : Напряжения 16 ячеек (÷1000 = V)
36-37   : Temperature PCB
40-41   : Max Temperature
46-47   : SOH (State of Health)
48-49   : SOC (State of Charge) %
51      : Status
66-69   : 4 температуры ячеек
72-73   : Количество ячеек
```

### Мультифреймовая поддержка

- **Function Code 0x03**: Первый фрейм (до 16 ячеек)
- **Function Code 0x04**: Дополнительные фреймы (для >16 ячеек)

---

## 6. Модели данных

### BMSData

```kotlin
data class BMSData (
    val voltage: Float,           // Суммарное напряжение (V)
    val current: Float,           // Ток (A)
    var cellVoltages: FloatArray, // Напряжения ячеек (до 32)
    val cellCount: Int,           // Количество ячеек (8-16)
    val tempPCB: Byte,            // Температура PCB (°C)
    val tempEnv: Short,           // Температура окружающей среды (°F)
    val cellTempArray: ByteArray, // 4 температуры ячеек (°C)
    val soc: Int,                 // State of Charge (0-100%)
    val soh: Int,                 // State of Health (0-100%)
    val status: Int,              // Статус батареи
    val warningStatus: Int        // Статус предупреждений
)
```

### Статусы батареи

```kotlin
STATUS_STANDBY = 0        // Standby
STATUS_CHARGING = 1       // Charging
STATUS_DISCHARGING = 2    // Discharging
STATUS_PROTECTING = 4     // Protecting
STATUS_CHARGING_LIMIT = 5 // Charging Limit
```

---

## 7. UI экраны

### Main Screen (MainFragmentNew)

3 таба:
- **Summary**: SOC% дуга, статус, Voltage/Current/Temp, детали
- **Cell Voltage**: GridLayout 4x4 для 16 ячеек
- **Temperature**: PCB + 4 Cell Temperature датчика

### Scan Screen (ScanFragmentNew)

- DialogFragment для сканирования BLE
- RecyclerView со списком устройств
- Проверка совместимости UUID

### Settings Screen (SettingsFragmentNew)

- Module ID (выпадающий список)
- CAN Protocol
- RS485 Protocol
- Версия приложения

### Info Screen (InfoFragmentNew)

- Детальные температуры
- Конвертация C/F

---

## 8. Ключевые зависимости

```gradle
// AndroidX
androidx.appcompat:appcompat:1.6.1
androidx.core:core-ktx:1.10.1
com.google.android.material:material:1.9.0
androidx.constraintlayout:constraintlayout:2.1.4

// Lifecycle & ViewModel
androidx.lifecycle:lifecycle-extensions:2.2.0
androidx.lifecycle:lifecycle-livedata-ktx:2.6.1
androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1

// Navigation
androidx.navigation:navigation-fragment-ktx:2.5.2
androidx.navigation:navigation-ui-ktx:2.5.2

// BLE
com.clj.fastble (FastBleLib) - локальный модуль

// UI
com.github.jakob-grabner:Circle-Progress-View:1.4
com.sothree.slidinguppanel:library:3.4.0
```

---

## 9. Flavors

```gradle
productFlavors {
    battery {
        applicationId "com.zetarapower.monitor.bl"
        dimension "company"
        versionName "3.0.2"
        versionCode 14
    }
}
```

Ресурсы flavor: `app/src/battery/`

---

## 10. Permissions

```xml
<!-- Android 12+ -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Android < 12 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Общие -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 11. Файлы проекта (ключевые)

| Файл | Строк | Описание |
|------|-------|----------|
| MainActivity.kt | 486 | Главная Activity, BLE lifecycle |
| MainFragmentNew.kt | 488 | Главный экран с табами |
| ScanFragmentNew.kt | 459 | Сканирование BLE |
| MainViewModel.kt | 377 | MVVM ViewModel |
| BMSProtocalV2.kt | 169 | Парсинг BMS данных |
| SettingsFragmentNew.kt | 317 | Настройки |
| InfoFragmentNew.kt | 125 | Информация |
| CellVoltageAdapter.kt | 98 | Адаптер ячеек |
| TemperatureSensorAdapter.kt | 161 | Адаптер температур |
| ArcProgressView.kt | 76 | Дуга прогресса |
