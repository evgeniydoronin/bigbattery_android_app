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

### Test 2026-02-10 (Build 3.0.7) — Settings Banner Fix

**Tester:** Kunj
**Device:** Google Pixel 6a, Android 16
**Battery:** BB-51.2V100Ah-0855 (INV type)
**Result:** ✅ PASS (баннер disconnect)

**Logs:** `docs/logs/bigbattery_logs_android_20260210_115248.json`

**Что проверялось:**
- Баннер Settings обновляется в реальном времени при потере BLE (без переключения табов)

**Результат:**
- 11:51:00 — BLE disconnect
- 11:51:01 — баннер обновился на "Disconnected" (1 сек) ✅
- Протоколы загружены корректно: ID1, P02-SLK, P03-SCH ✅
- Errors: 0 ✅

**Что НЕ входило в scope:**
- Auto-reconnect на экране Settings — функционал отсутствует, ожидает решения от Marshal
- Два варианта: 1) реализовать auto-reconnect на Settings; 2) переключиться на доработку Home screen

---

### Test 2026-02-02 (Build 3.0.6) — Test 4 + Test 5

**Tester:** Kunj
**Device:** Google Pixel 6a, Android 16
**Battery:** BB-51.2V100Ah-0855 (INV type)

**Test 4 (Protocol Change + Save): ✅ PASS**
- Протокол сменён с P01-GRW на P02-SLK (CAN) и P03-SCH (RS485)
- Save + confirmation dialog + battery restart — всё работает

**Test 5 (Mid-Session Reconnect): 🔄 PARTIAL PASS**
- Disconnect detection: баннер не обновлялся без переключения табов ❌
- Auto-reconnect: отсутствует ❌
- После ручного reconnect: данные загружены корректно ✅

**Logs:**
- `docs/logs/bigbattery_logs_android_20260202_152723.json` (disconnect)
- `docs/logs/bigbattery_logs_android_20260202_152917.json` (manual reconnect)

**Root cause анализ:**
1. Баннер — `SettingsFragmentNew` не имел real-time observer на BLE статус (проверка только при создании фрагмента)
2. Auto-reconnect — существует только в `MainActivity.onResume()`, не работает пока пользователь на Settings

**Fix в 3.0.7:**
- Добавлена периодическая проверка BLE статуса каждые 2 сек в `SettingsFragmentNew`
- Баннер теперь обновляется в реальном времени

---

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

---

### Report #1 (2025-01-29)

**Tester:** Kunj
**Build:** 3.0.2 (versionCode 14)
**Scenario:** Test 2 — Settings Screen UI

**Сообщение от тестировщика:**
> "I'm testing the app but I'm not able to see the can protocol name in 2nd test case."

**Логи:** Ожидаем от Kunj (запрошены)

---

## Metrics

| Метрика | Build 3.0.2 | Build 3.0.5 | Build 3.0.6 | Build 3.0.7 |
|---------|-------------|-------------|-------------|-------------|
| Module ID загружается | ? | ✅ ID1 | ✅ ID1 | ✅ ID1 |
| CAN Protocol загружается | ❌ | ✅ P01-GRW | ✅ P02-SLK | ✅ P02-SLK |
| RS485 Protocol загружается | ? | ✅ P01-GRW | ✅ P03-SCH | ✅ P03-SCH |
| Баннер real-time update | — | — | ❌ | ✅ |
| Auto-reconnect на Settings | — | — | ❌ | ❌ (not in scope) |

---

## Файлы для анализа

- `MainActivity.kt:162-189` — `getSettingData()` последовательная отправка запросов
- `SettingsFragmentNew.kt` — отображение данных на экране + periodic connection check
- `MainViewModel.kt:334-351` — `handleSettingProtocol()` парсинг ответов

---

## Логи

| Дата | Build | Battery | Файл |
|------|-------|---------|------|
| 2026-02-10 | 3.0.7 | INV (BB-51.2V100Ah-0855) | `docs/logs/bigbattery_logs_android_20260210_115248.json` |
| 2026-02-02 | 3.0.6 | INV (BB-51.2V100Ah-0855) | `docs/logs/bigbattery_logs_android_20260202_152723.json` |
| 2026-02-02 | 3.0.6 | INV (BB-51.2V100Ah-0855) | `docs/logs/bigbattery_logs_android_20260202_152917.json` |
| 2026-01-30 | 3.0.5 | INV (BB-51.2V100Ah-0855) | `docs/logs/bigbattery_logs_android_20260130_113932.json` |
