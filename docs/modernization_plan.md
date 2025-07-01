# План модернизации Android приложения Zetara Power BMS Tool

## 🔗 Ссылки на исходные материалы

- **iOS приложение (эталон):** `/Users/evgeniydoronin/Projects/Customers/bigBattery/apps/src/Re-Branded App/`
- **Android приложение (текущее):** `zetarapowerbmstool/`
- **Дизайн-ресурсы:** `/Users/evgeniydoronin/Projects/Customers/bigBattery/apps/src/BB-Branded App Assets/`

## 📋 Анализ текущего состояния

### iOS приложение (эталон)
- Современный дизайн с градиентным фоном и компонентным подходом
- 4 основных экрана: Home, Connectivity, Details, Settings
- Богатый функционал с табами на главном экране (Summary, Cell Voltage, Temperature)
- Продвинутая визуализация данных батареи
- Настройки протоколов (Module ID, CAN, RS485)

### Android приложение (текущее состояние)
- Устаревший дизайн
- Базовый функционал работает, но упрощенный
- Проблемы с StatusBarUtil (закомментирован)
- Отсутствуют табы на главном экране
- Упрощенный экран деталей
- Ограниченные настройки

## 🎯 Цели модернизации

1. **Визуальное соответствие iOS версии** - современный дизайн, цветовая схема, компоненты
2. **Функциональное соответствие** - все возможности iOS версии
3. **Улучшенная архитектура** - компонентный подход, как в iOS
4. **Исправление технических проблем** - StatusBarUtil и другие

## 🏗️ Архитектурный план

### 1. Обновление дизайн-системы
- **Новая цветовая палитра** на основе iOS версии (Big Battery брендинг)
- **Градиентные фоны** вместо статичных изображений
- **Современные компоненты** с Material Design 3
- **Адаптивная типографика** и иконки

### 2. Реструктуризация главного экрана (HomeFragment)

**Текущая структура:** простой список элементов

**Новая структура (как в iOS):**
```
┌─ Header (логотип Big Battery)
├─ Bluetooth Connection Panel
├─ Battery Progress (круговая диаграмма)
├─ Battery Status Indicator
├─ Parameters Row (Voltage, Current, Temperature)
├─ Tabs Container
│  ├─ Summary Tab
│  ├─ Cell Voltage Tab
│  └─ Temperature Tab
└─ Timer (последнее обновление)
```

### 3. Создание новых компонентов

#### Основные компоненты:
- `BluetoothConnectionView` - панель подключения устройства
- `BatteryProgressView` - круговая диаграмма заряда
- `BatteryStatusView` - индикатор статуса (Charging, Standby, etc.)
- `BatteryParametersView` - параметры батареи
- `TabsContainerView` - контейнер с табами
- `SummaryTabView` - таб с общей информацией
- `CellVoltageTabView` - таб с напряжениями ячеек
- `TemperatureTabView` - таб с температурами
- `TimerView` - время последнего обновления

### 4. Модернизация экрана подключения
- **Современный дизайн** списка устройств
- **Улучшенная индикация** статуса подключения
- **Автоматическое переподключение** при потере связи

### 5. Полная переработка экрана деталей

**Текущий:** простая сетка с напряжениями ячеек

**Новый (как в iOS):**
- Скрытие секции Cell Voltage (как в iOS)
- Фокус на температурах с красивыми карточками
- Логотип внизу экрана
- Современные карточки с иконками

### 6. Расширение экрана настроек
- **Module ID** с выпадающим списком
- **CAN Protocol** настройки
- **RS485 Protocol** настройки
- **Версия приложения**
- Современный дизайн элементов настроек

## 🔧 Технические улучшения

### 1. Решение проблемы StatusBarUtil
```kotlin
// Замена на нативные Android API
window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
window.statusBarColor = Color.TRANSPARENT
```

### 2. Улучшение архитектуры данных
- **Единый источник данных** через ViewModel
- **Reactive подход** с LiveData/Flow
- **Кэширование данных** для офлайн режима

### 3. Оптимизация производительности
- **Ленивая загрузка** компонентов
- **Переиспользование** view holders
- **Оптимизация** Bluetooth операций

## 📱 Детальный план экранов

### Главный экран (Home)
1. **Header с логотипом** - белый фон, логотип Big Battery
2. **Bluetooth панель** - статус подключения, имя устройства, кнопка подключения
3. **Круговая диаграмма** - уровень заряда с анимацией
4. **Статус батареи** - Charging/Standby/Protecting с иконками
5. **Параметры** - Voltage, Current, Temperature в ряд
6. **Табы** - Summary, Cell Voltage, Temperature с переключением
7. **Таймер** - время последнего обновления

### Экран подключения (Connectivity)
1. **Список устройств** - современные карточки
2. **Индикация сигнала** - RSSI с иконкой
3. **Статус подключения** - Connected/Available
4. **Автопереподключение** - при потере связи

### Экран деталей (Details)
1. **Температуры** - PCB и Cell температуры в карточках
2. **Логотип** - внизу экрана
3. **Скрытие** Cell Voltage секции (как в iOS)

### Экран настроек (Settings)
1. **Module ID** - выпадающий список ID1-ID16
2. **CAN Protocol** - настройки протокола
3. **RS485 Protocol** - настройки протокола
4. **Версия** - информация о версии приложения

## 🎨 Дизайн-система

### Цветовая палитра (Big Battery)
```xml
<color name="primary">#FF6B35</color>        <!-- Оранжевый Big Battery -->
<color name="primaryDark">#E55A2B</color>
<color name="secondary">#2C3E50</color>      <!-- Темно-синий -->
<color name="background">#F8F9FA</color>     <!-- Светлый фон -->
<color name="surface">#FFFFFF</color>        <!-- Белый для карточек -->
<color name="onPrimary">#FFFFFF</color>      <!-- Белый текст на оранжевом -->
<color name="onSurface">#2C3E50</color>      <!-- Темный текст на светлом -->
```

### Типографика
- **Заголовки:** Roboto Bold 20sp
- **Подзаголовки:** Roboto Medium 16sp
- **Основной текст:** Roboto Regular 14sp
- **Мелкий текст:** Roboto Regular 12sp

### Компоненты
- **Карточки:** скругление 12dp, тень 4dp
- **Кнопки:** скругление 8dp, высота 48dp
- **Отступы:** 16dp стандартный, 8dp малый

## 📋 План реализации (поэтапно)

### Этап 1: Подготовка инфраструктуры
1. ✅ Обновление зависимостей
2. ✅ Создание новой дизайн-системы (Big Battery цвета)
3. ✅ Решение проблемы StatusBarUtil (нативные Android API)
4. ✅ Настройка архитектуры компонентов (папка ui/components)

### Этап 2: Главный экран
1. ⏳ Создание header с логотипом
2. ⏳ Bluetooth connection panel
3. ⏳ Battery progress view
4. ⏳ Battery status view
5. ⏳ Parameters view
6. ⏳ Tabs container с тремя табами

### Этап 3: Остальные экраны
1. ⏳ Модернизация экрана подключения
2. ⏳ Переработка экрана деталей
3. ⏳ Расширение экрана настроек

### Этап 4: Тестирование и полировка
1. ⏳ Тестирование на разных устройствах
2. ⏳ Оптимизация производительности
3. ⏳ Исправление багов
4. ⏳ Финальная полировка UI/UX

## ⚡ Ожидаемые результаты

После реализации плана Android приложение будет:
- **100% функционально соответствовать** iOS версии
- **Современный дизайн** в стиле Big Battery
- **Улучшенная производительность** и стабильность
- **Лучший пользовательский опыт**
- **Готовность к публикации** в Google Play Store

## 📝 Технические детали реализации

### Структура новых компонентов

#### BluetoothConnectionView
```kotlin
class BluetoothConnectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    private lateinit var deviceNameText: TextView
    private lateinit var statusText: TextView
    private lateinit var bluetoothIcon: ImageView
    
    var onTap: (() -> Unit)? = null
    
    fun updateDeviceName(name: String?) {
        // Обновление имени устройства или "Tap to Connect"
    }
    
    fun updateConnectionStatus(connected: Boolean) {
        // Обновление статуса подключения
    }
}
```

#### BatteryProgressView
```kotlin
class BatteryProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private lateinit var circleProgressView: CircleProgressView
    private lateinit var chargingIcon: ImageView
    
    var level: Float = 0f
        set(value) {
            field = value
            circleProgressView.setValue(value * 100)
        }
    
    fun updateChargingStatus(isCharging: Boolean) {
        chargingIcon.visibility = if (isCharging) View.VISIBLE else View.GONE
    }
}
```

#### TabsContainerView
```kotlin
class TabsContainerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: TabsAdapter
    
    fun getSummaryTabView(): SummaryTabView = adapter.summaryTab
    fun getCellVoltageTabView(): CellVoltageTabView = adapter.cellVoltageTab
    fun getTemperatureTabView(): TemperatureTabView = adapter.temperatureTab
}
```

### Обновление MainActivity.kt

```kotlin
// Замена StatusBarUtil на нативные API
private fun setupStatusBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.decorView.systemUiVisibility = 
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or 
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Для светлого контента используем темные иконки
            window.decorView.systemUiVisibility = 
                window.decorView.systemUiVisibility or 
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}
```

### Обновление цветовой схемы

```xml
<!-- colors.xml -->
<resources>
    <!-- Big Battery Brand Colors -->
    <color name="bb_primary">#FF6B35</color>
    <color name="bb_primary_dark">#E55A2B</color>
    <color name="bb_secondary">#2C3E50</color>
    <color name="bb_background">#F8F9FA</color>
    <color name="bb_surface">#FFFFFF</color>
    <color name="bb_on_primary">#FFFFFF</color>
    <color name="bb_on_surface">#2C3E50</color>
    <color name="bb_on_background">#2C3E50</color>
    
    <!-- Status Colors -->
    <color name="status_charging">#4CAF50</color>
    <color name="status_standby">#FFC107</color>
    <color name="status_protecting">#F44336</color>
    
    <!-- Component Colors -->
    <color name="battery_progress">#FF6B35</color>
    <color name="battery_background">#E0E0E0</color>
    <color name="tab_selected">#FF6B35</color>
    <color name="tab_unselected">#9E9E9E</color>
</resources>
```

---

**Дата создания:** 01.07.2025  
**Статус:** В разработке  
**Версия:** 1.0
