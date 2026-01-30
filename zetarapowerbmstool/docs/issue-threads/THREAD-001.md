# THREAD-001: CAN Protocol не отображается на Settings

## Status: 🟡 IN PROGRESS

## Симптомы

- На экране Settings карточка CAN Protocol не показывает название протокола
- Тестировщик Kunj: "I'm not able to see the can protocol name in 2nd test case"
- Возможно показывает "--" или пустое значение

## Связь с iOS

Аналогичная проблема была на iOS (builds 45-47):
- Race condition: подписка отменялась до получения ответа от батареи
- Запросы протоколов отправлялись последовательно с delay 600ms
- Если пользователь уходил с экрана до завершения — данные терялись

## Root Cause (текущее понимание)

**Гипотеза:** Та же timing-проблема что на iOS.

В Android запросы отправляются:
- Module ID → 0ms (первый)
- CAN → +600ms
- RS485 → +600ms

Файл: `MainActivity.kt:162-189` — метод `getSettingData()`

Если что-то прерывает процесс между запросами (переход на другой экран, потеря BLE) — CAN/RS485 не загрузятся.

## Timeline

### Test 2026-01-30 (Build 3.0.5) — INV Battery

**Tester:** Manual test
**Device:** Google Pixel 6a, Android 16
**Battery:** BB-51.2V100Ah-0855 (INV type)
**Result:** ✅ PASS (Test 1: Connect + Open Settings)

**Logs:** `docs/logs/bigbattery_logs_android_20260130_113932.json`

**What worked:**
- Module ID: **ID1** ✅
- CAN Protocol: **P01-GRW** ✅
- RS485 Protocol: **P01-GRW** ✅
- SET операции (смена протоколов): ✅
- BLE connection stable
- No errors (errors: 0, successes: 39)

**Protocol commands verified:**
| Command | Code | GET | SET |
|---------|------|-----|-----|
| Module ID | 0x02/0x05 | ✅ | ✅ |
| RS485 | 0x03/0x07 | ✅ | ✅ |
| CAN | 0x04/0x06 | ✅ | ✅ |

**Next steps:**
- Continue with Tests 2-8
- Test with PWR battery (no CAN/RS485 — expected "--")

---

### Report #1 (2025-01-29)

**Tester:** Kunj
**Build:** 3.0.2 (versionCode 14)
**Scenario:** Test 2 — Settings Screen UI

**Сообщение от тестировщика:**
> "I'm testing the app but I'm not able to see the can protocol name in 2nd test case."

**Логи:** Ожидаем от Kunj (запрошены)

**Next steps:**
1. Получить логи от Kunj
2. Проверить `protocolInfo.canProtocol` в JSON
3. Посмотреть events — был ли запрос CAN отправлен и получен ответ
4. Определить точную причину

---

## Metrics

| Метрика | Build 3.0.2 | Build 3.0.5 (INV) |
|---------|-------------|-------------------|
| Module ID загружается | ? | ✅ ID1 |
| CAN Protocol загружается | ❌ | ✅ P01-GRW |
| RS485 Protocol загружается | ? | ✅ P01-GRW |

---

## Файлы для анализа

- `MainActivity.kt:162-189` — `getSettingData()` последовательная отправка запросов
- `SettingsFragmentNew.kt` — отображение данных на экране
- `MainViewModel.kt:334-351` — `handleSettingProtocol()` парсинг ответов

---

## Логи

| Дата | Build | Battery | Файл |
|------|-------|---------|------|
| 2026-01-30 | 3.0.5 | INV (BB-51.2V100Ah-0855) | `docs/logs/bigbattery_logs_android_20260130_113932.json` |
