# Android App Test Instructions — Settings Screen & Home Screen

**App:** Big Battery BMS Tool (Android)
**Package:** `com.zetarapower.monitor.bl`
**Scope:** Home screen cards, Settings screen, BLE connection, Diagnostics export
**Date:** February 2026

---

## Before You Start

### Equipment needed
- Android phone with Bluetooth enabled
- Big Battery module (powered ON, within BLE range ~3-5 meters)
- Email app configured on the phone (for sending logs)

### App version
- Open app → Settings → scroll to bottom → note the **Version** number
- Write it in the results table

### Key screens for this test
- **Home** — main screen, shows battery info + Selected ID / CAN / RS485 cards
- **Settings** — gear icon, shows Module ID / CAN / RS485 cards (editable)
- **Diagnostics** — hidden screen, has "Send Logs" button

### How to open Diagnostics (important!)
The Diagnostics screen is hidden. To open it:
1. Go to **Settings** screen
2. Scroll to the bottom — find the app **version number**
3. **Long press** (hold 2 seconds) on the version text
4. Diagnostics screen will open

---

## Results Table

Fill in after each test. Mark PASS or FAIL.

| # | Test | PASS / FAIL | Module ID | CAN | RS485 | Notes |
|---|------|-------------|-----------|-----|-------|-------|
| 1 | Connect + Open Settings | ✅ PASS | ID1 | P01-GRW | P01-GRW | Build 3.0.5, INV battery (BB-51.2V100Ah-0855) |
| 2 | Settings Screen UI | ✅ PASS | — | — | — | Build 3.0.5, visual check |
| 3 | Protocol Data Loading | ✅ PASS | ID1 | P01-GRW | P01-GRW | Build 3.0.5, no "--" |
| 4 | Protocol Change + Save | ✅ PASS | ID1 | P02-SLK | P03-SCH | Build 3.0.6, save + restart OK |
| 5 | Mid-Session Reconnect → Settings | 🔄 PARTIAL | ID1 | P02-SLK | P03-SCH | Build 3.0.7: banner ✅, auto-reconnect ❌ (not in scope) |
| 6 | Home Screen Cards | ✅ PASS | ID1 | P06-LUX | P02-LUX | Build 3.0.7, battery BB-51.2V100Ah-0000 |

**App Version:** _______________
**Phone Model:** _______________
**Android Version:** _______________
**Module Serial / Name:** _______________
**Test Date:** _______________
**Tester:** _______________

---

## Test 1: Connect + Open Settings (CRITICAL)

**Goal:** Connect to the module and verify Settings screen opens with data.

### Steps

1. Close the app completely (swipe it away from Recent Apps)
2. Turn OFF Bluetooth on the phone
3. Wait 5 seconds
4. Turn ON Bluetooth
5. Open the app
6. Connect to the battery module (tap Bluetooth card → scan → tap module name)
7. Wait for connection (up to 5 seconds)
8. Tap the **Settings** icon (gear)
9. Wait 3 seconds for protocol data to load

### Expected Result

- Settings screen opens
- **Connection Status Banner** at top shows "Connected" with green icon
- **Module ID** card shows a value (number 1-16)
- **CAN Protocol** card shows a protocol name
- **RS485 Protocol** card shows a protocol name
- No values show "--" or are blank

### If FAILED

1. Screenshot Settings screen
2. Diagnostics → **Send Logs**
3. Notes — short: `connected / not connected` and which fields show "--"

> Logs will show connection events and protocol data, but they won't tell us whether the screen itself loaded or got stuck visually.

---

## Test 2: Settings Screen UI (CRITICAL)

**Goal:** Verify all UI elements on the Settings screen are present and correct.

### Steps

1. Open Settings screen (should already be there from Test 1)
2. Check each element listed below
3. Take a screenshot of the full Settings screen

### Expected Result

Check each item:

- [ ] **Connection Status Banner** at top — shows "Connected" with green icon
- [ ] **Note Label** — text explaining settings purpose is visible below the banner
- [ ] **Module ID card** — shows a number (e.g. "1" or "2-16"), card is tappable
- [ ] **CAN Protocol card** — shows protocol name (e.g. "PYLON"), card is tappable
- [ ] **RS485 Protocol card** — shows protocol name (e.g. "PYLON"), card is tappable
- [ ] **Save Button** — visible at the bottom, should be **disabled** (greyed out) if no changes made
- [ ] **Information Banner** — text at the bottom with instructions
- [ ] **Version** — app version visible at the bottom of the screen

**Important rule:** If Module ID = 1, CAN and RS485 cards should be **active** (tappable). If Module ID = 2-16, CAN and RS485 should be **locked** (not tappable).

### If FAILED

1. Screenshot Settings screen
2. Diagnostics → **Send Logs**
3. Notes — just list missing elements, e.g.: `no Save button, no Note Label`

> Logs don't capture UI layout — only you can see if an element is missing or in the wrong place.

---

## Test 3: Protocol Data Loading (CRITICAL)

**Goal:** Verify Module ID, CAN, and RS485 values load correctly (not "--" or empty).

### Steps

1. Go to Settings screen
2. Wait 3 seconds for all data to load
3. Check the values shown on each card:
   - **Module ID card** → selected value
   - **CAN Protocol card** → selected protocol name
   - **RS485 Protocol card** → selected protocol name
4. Take a screenshot showing all three values

### Expected Result

- **Module ID** shows a number (1-16), NOT "--" or blank
- **CAN Protocol** shows a protocol name (e.g. "PYLON", "SMA", etc.), NOT "--" or blank
- **RS485 Protocol** shows a protocol name, NOT "--" or blank
- No status labels show "Loading..." for more than 3 seconds

### Known issue (from iOS)

The app sends protocol requests in sequence with 600ms delays:
- Module ID → 0ms
- CAN → 600ms
- RS485 → 1200ms

If any value shows "--", it may be a timing/race condition (same bug as iOS builds 45-47).

### If FAILED

1. Screenshot Settings screen (don't leave the screen!)
2. Wait 10 more seconds — did values appear? Note: `yes after Xs` or `still --`
3. Diagnostics → **Send Logs**
4. Notes — which field: `Module ID --` / `CAN --` / `RS485 --`

> Logs show protocol data at the moment of export, but they won't tell us if "--" appeared briefly and then loaded, or stayed forever. That's why we need to know how long you waited.

---

## Test 4: Protocol Change + Save (CRITICAL)

**Goal:** Verify changing a protocol and saving works correctly.

### Steps

1. Go to Settings screen
2. **Write down** the current values of Module ID, CAN, RS485
3. Tap the **CAN Protocol card**
4. A dropdown list appears — select a **different** protocol from the list
5. Check: the **status label** below CAN card shows the new selection
6. Check: the **Save Button** becomes **enabled** (not greyed out)
7. Tap **Save Button**
8. A confirmation dialog appears — tap **Confirm** (or "OK")
9. Wait — the battery will restart (takes 5-10 seconds)
10. The app should show a disconnection message
11. Wait for the battery to restart and reconnect (or reconnect manually)
12. Go to Settings screen
13. Check: CAN Protocol shows the **new** value you selected

### Expected Result

- After step 4: dropdown shows list of available protocols
- After step 5: status label shows pending change text
- After step 6: Save button is active/clickable
- After step 7-8: confirmation dialog appears
- After step 9: battery disconnects (this is expected — battery restarts)
- After step 13: Settings shows the **new** CAN protocol value

### If FAILED

1. Screenshot Settings before Save and after reconnection
2. Diagnostics → **Send Logs**
3. Notes — short: `dialog: yes/no`, `battery restart: yes/no`, `new value saved: yes/no`

> Logs record protocol values and connection events, but they can't tell us if the confirmation dialog appeared on screen or if the Save button changed state. Write that down.

### After this test
**Restore the original protocol:** repeat steps 3-13 to set CAN back to the original value you wrote down.

---

## Test 5: Mid-Session Reconnect → Settings (CRITICAL)

**Goal:** Verify Settings data loads correctly after BLE signal loss and reconnection.

### Steps

1. Ensure the app is connected (check Settings — banner shows "Connected")
2. Go to Settings screen, **write down** values: Module ID, CAN, RS485
3. **Walk away** from the battery module (go to another room, ~10+ meters)
4. Wait until the Settings banner shows disconnection
5. **Walk back** to the battery module (within 3-5 meters)
6. Keep the app in the foreground
7. Wait up to 10 seconds for auto-reconnect
8. Once reconnected, check Settings screen: Module ID, CAN, RS485 values

### Expected Result

- Step 4: Connection Status Banner changes to "Disconnected"
- Step 7: App automatically reconnects — banner returns to "Connected"
- Step 8: All three values (Module ID, CAN, RS485) match what you wrote in step 2
- No "--" values on the Settings screen after reconnection
- No crash, no freeze

### Alternative (if auto-reconnect does not happen)

If after 10 seconds the app does NOT reconnect:
1. Go back, connect manually (Bluetooth card → scan → tap module)
2. Go to Settings screen, check values

### If FAILED

1. Screenshot Settings screen after reconnection
2. Diagnostics → **Send Logs**
3. Notes — short: `auto / manual`, `waited Xs`, `crash / freeze / ok`, `Settings: ok / -- on [field]`

> Logs show CONNECTION and DISCONNECTION events with timestamps, but they have no idea whether the reconnect happened by itself or you tapped the button manually. Only you know that.

---

## Test 6: Home Screen Cards (CRITICAL)

**Goal:** Verify that Selected ID, Selected CAN, and Selected RS485 cards appear on the Home screen and show correct values after connection.

### Layout reference

```
┌─────────┬─────────┬─────────┐
│ -- V    │ -- A    │ --°F/°C │  ← existing cards
│ Voltage │ Current │ Temp.   │
├─────────┼─────────┼─────────┤
│  --     │  --     │  --     │  ← new cards
│Selected │Selected │Selected │
│  ID     │  CAN    │ RS485   │
└─────────┴─────────┴─────────┘
[Summary] [Cell Voltage] [Temperature]
```

### Steps

1. Open the app (make sure NOT connected yet)
2. Look at the Home screen — find the row of three cards **below** Voltage/Current/Temp
3. Check: all three new cards show "--" (no data yet)
4. Connect to the battery module (tap Bluetooth card → scan → tap module name)
5. Wait up to 5 seconds for BMS data to load
6. Wait up to 5 more seconds for settings data to load (total ~10 seconds)
7. Check the three new cards:
   - **Selected ID** — shows "ID1" (or "ID2"–"ID16")
   - **Selected CAN** — shows a protocol name (e.g. "P01-GRW")
   - **Selected RS485** — shows a protocol name (e.g. "P01-GRW")
8. Go to **Settings** screen (gear icon)
9. Compare: values on Home screen cards must match values on Settings screen
10. Go back to Home screen — values should still be there (not "--")

### Expected Result

- Step 3: All three cards show "--" before connection
- Step 7: All three cards show real values (not "--")
- Step 9: Home screen values match Settings screen values exactly
- Step 10: Values persist after navigating back

### If FAILED

1. Screenshot Home screen showing the three cards
2. Screenshot Settings screen for comparison
3. Diagnostics → **Send Logs**
4. Notes — short: `ID: ok/-- `, `CAN: ok/--`, `RS485: ok/--`, `match Settings: yes/no`

> Cards load ~2 seconds after BMS data. If you see "--" briefly and then values appear, that is normal. Only mark FAIL if values stay "--" after 10 seconds.

---

## After All Tests

### Required deliverables

1. **Filled results table** (copy from above, fill in all columns)
2. **Screenshots:**
   - Settings screen (showing Module ID, CAN, RS485 values)
   - Diagnostics screen (showing event logs)
3. **Logs:** Should already be sent via email after each failed test
4. **One final Send Logs** from Diagnostics (even if all tests passed)

### How to send results

1. Fill in the results table
2. Attach all screenshots
3. Send to the development team

---

## Quick Reference

### Screen navigation
```
Home screen
  ├── Bluetooth card (tap to connect)
  ├── Battery progress circle (SOC %)
  ├── Voltage / Current / Temp cards
  ├── Selected ID / Selected CAN / Selected RS485 cards
  └── Tabs: Summary | Cell Voltage | Temperature

Settings screen
  ├── Connection Status Banner (top — green/red)
  ├── Note Label (description text)
  ├── Module ID card (tap to change, shows 1-16)
  ├── CAN Protocol card (tap to change, shows protocol name)
  ├── RS485 Protocol card (tap to change, shows protocol name)
  ├── Save button (saves changes, restarts battery)
  ├── Information Banner (instructions text)
  └── Version (app version at bottom)

Diagnostics screen (hidden — long press on version to open)
  ├── Back button (top left)
  ├── Events list (scrollable, shows timestamps)
  └── Send Logs button (creates JSON → opens email)
```

### How to open Diagnostics
Settings → scroll to bottom → **long press on version number** (hold 2 sec) → Diagnostics opens

### What "--" means
If you see "--" instead of a value on the Settings screen, it means the protocol data has not loaded yet. Wait 3-5 seconds. If it persists, that is a bug — mark the test as FAILED and send logs.

### Battery restart after Save
When you tap Save in Settings, the battery module restarts. This is **normal behavior**. The app will disconnect temporarily. Wait for it to reconnect, then go back to Settings to verify the new value.

### Timing reference
- Protocol data loads on Settings: ~1.2 seconds (sequential: Module ID → CAN → RS485)
- Battery restart after Save: 5-10 seconds
- Auto-reconnect after signal loss: up to 10 seconds

---

## Перевод шагов на русский

### Как открыть Diagnostics (важно!)
Экран Diagnostics скрытый. Чтобы открыть:
1. Перейти на экран **Settings**
2. Пролистать вниз — найти **номер версии** приложения
3. **Долго нажать** (удерживать 2 секунды) на текст версии
4. Откроется экран Diagnostics

---

### Тест 1: Подключение + открыть Settings (CRITICAL)

**Цель:** Подключиться к модулю и проверить, что экран Settings открывается с данными.

1. Полностью закрыть приложение (смахнуть из списка последних приложений)
2. Выключить Bluetooth на телефоне
3. Подождать 5 секунд
4. Включить Bluetooth
5. Открыть приложение
6. Подключиться к модулю батареи (нажать на карточку Bluetooth → скан → нажать на имя модуля)
7. Подождать подключения (до 5 секунд)
8. Нажать на иконку **Settings** (шестерёнка)
9. Подождать 3 секунды, пока загрузятся данные протоколов

**Ожидаемый результат:**
- Экран Settings открывается
- Баннер статуса подключения вверху — "Connected", зелёная иконка
- Карточка Module ID показывает значение (число 1-16)
- Карточка CAN Protocol показывает название протокола
- Карточка RS485 Protocol показывает название протокола
- Нигде нет "--" или пустых значений

**Если не прошёл:**
1. Скриншот Settings
2. Diagnostics → **Send Logs**
3. Заметка: `connected / not connected`, какие поля показывают "--"

> Логи покажут события подключения и данные протоколов, но не покажут, загрузился ли сам экран визуально или завис. Это видите только вы.

---

### Тест 2: UI экрана Settings (CRITICAL)

**Цель:** Проверить, что все элементы интерфейса на экране Settings на месте.

1. Открыть экран Settings (уже должен быть открыт после Теста 1)
2. Проверить каждый элемент из списка ниже
3. Сделать скриншот всего экрана Settings

**Чеклист:**
- [ ] Баннер статуса подключения вверху — "Connected", зелёная иконка
- [ ] Note Label — текст с описанием назначения настроек, виден под баннером
- [ ] Карточка Module ID — показывает число (например "1" или "2-16"), нажимается
- [ ] Карточка CAN Protocol — показывает название протокола (например "PYLON"), нажимается
- [ ] Карточка RS485 Protocol — показывает название протокола, нажимается
- [ ] Кнопка Save — видна внизу, должна быть **неактивна** (серая), если ничего не менялось
- [ ] Information Banner — текст с инструкциями внизу
- [ ] Version — версия приложения видна внизу экрана

**Важное правило:** Если Module ID = 1, карточки CAN и RS485 должны быть **активны** (нажимаются). Если Module ID = 2-16, CAN и RS485 должны быть **заблокированы** (не нажимаются).

**Если не прошёл:**
1. Скриншот Settings
2. Diagnostics → **Send Logs**
3. Заметка: просто перечислить чего нет, например: `нет Save кнопки, нет Note Label`

> Логи не фиксируют внешний вид экрана — только вы видите, есть элемент на месте или нет.

---

### Тест 3: Загрузка данных протоколов (CRITICAL)

**Цель:** Проверить, что Module ID, CAN и RS485 загружаются корректно (не "--" и не пустые).

1. Перейти на экран Settings
2. Подождать 3 секунды, пока все данные загрузятся
3. Проверить значения на каждой карточке:
   - Карточка Module ID → выбранное значение
   - Карточка CAN Protocol → название протокола
   - Карточка RS485 Protocol → название протокола
4. Сделать скриншот с тремя значениями

**Ожидаемый результат:**
- Module ID — число (1-16), НЕ "--" и НЕ пустое
- CAN Protocol — название протокола (например "PYLON", "SMA"), НЕ "--"
- RS485 Protocol — название протокола, НЕ "--"

**Известная проблема (из iOS):** Запросы отправляются последовательно с задержками 600ms. Если значение "--" — возможно race condition (аналог бага iOS builds 45-47).

**Если не прошёл:**
1. Скриншот Settings (не уходить с экрана!)
2. Подождать ещё 10 секунд — появились значения? Записать: `да, через Xс` или `так и осталось --`
3. Diagnostics → **Send Logs**
4. Заметка: какое поле: `Module ID --` / `CAN --` / `RS485 --`

> Логи покажут данные протоколов на момент экспорта, но не расскажут, мелькнуло "--" на секунду и загрузилось, или висело навсегда. Поэтому важно записать, сколько ждали.

---

### Тест 4: Смена протокола + Save (CRITICAL)

**Цель:** Проверить, что смена протокола и сохранение работают корректно.

1. Перейти на экран Settings
2. **Записать** текущие значения Module ID, CAN, RS485
3. Нажать на **карточку CAN Protocol**
4. Появляется выпадающий список — выбрать **другой** протокол
5. Проверить: **статус** под карточкой CAN показывает новый выбор
6. Проверить: **кнопка Save** стала **активна** (не серая)
7. Нажать **Save**
8. Появляется диалог подтверждения — нажать **Confirm** (или "OK")
9. Подождать — батарея перезагрузится (5-10 секунд)
10. Приложение должно показать отключение
11. Подождать перезагрузку батареи и переподключение (или переподключиться вручную)
12. Перейти на экран Settings
13. Проверить: CAN Protocol показывает **новое** значение

**Если не прошёл:**
1. Скриншот Settings до Save и после переподключения
2. Diagnostics → **Send Logs**
3. Заметка: `диалог: да/нет`, `батарея перезагрузилась: да/нет`, `новое значение сохранилось: да/нет`

> Логи запишут значения протоколов и события подключения, но не расскажут, появился ли диалог подтверждения и поменялось ли состояние кнопки Save. Это можете заметить только вы.

**После теста:** вернуть оригинальный протокол — повторить шаги 3-13, установив CAN обратно.

---

### Тест 5: Переподключение посреди сессии → Settings (CRITICAL)

**Цель:** Проверить, что данные Settings загружаются корректно после потери и восстановления BLE-сигнала.

1. Убедиться, что приложение подключено (баннер Settings — "Connected")
2. На экране Settings **записать** значения: Module ID, CAN, RS485
3. **Уйти** от модуля батареи (в другую комнату, ~10+ метров)
4. Подождать, пока баннер Settings покажет отключение
5. **Вернуться** к модулю батареи (в пределах 3-5 метров)
6. Держать приложение на переднем плане
7. Подождать до 10 секунд для автоматического переподключения
8. После переподключения проверить Settings: Module ID, CAN, RS485

**Ожидаемый результат:**
- Шаг 4: баннер меняется на "Disconnected"
- Шаг 7: автоматическое переподключение — баннер возвращается на "Connected"
- Шаг 8: все три значения совпадают с записанными в шаге 2, нет "--"

**Если авто-реконнект не сработал:** подключиться вручную, проверить значения.

**Если не прошёл:**
1. Скриншот Settings после переподключения
2. Diagnostics → **Send Logs**
3. Заметка: `авто / вручную`, `ждал Xс`, `краш / фриз / ок`, `Settings: ок / -- на [поле]`

> Логи покажут события CONNECTION и DISCONNECTION с таймстемпами, но понятия не имеют, переподключение произошло само или вы нажали кнопку. Это знаете только вы.

---

### Тест 6: Карточки на главном экране (CRITICAL)

**Цель:** Проверить, что карточки Selected ID, Selected CAN, Selected RS485 отображаются на главном экране и показывают правильные значения после подключения.

**Расположение на экране:**

```
┌─────────┬─────────┬─────────┐
│ -- V    │ -- A    │ --°F/°C │  ← существующие
│ Voltage │ Current │ Temp.   │
├─────────┼─────────┼─────────┤
│  --     │  --     │  --     │  ← новые
│Selected │Selected │Selected │
│  ID     │  CAN    │ RS485   │
└─────────┴─────────┴─────────┘
[Summary] [Cell Voltage] [Temperature]
```

1. Открыть приложение (убедиться что НЕ подключены)
2. На главном экране найти ряд из трёх карточек **под** Voltage/Current/Temp
3. Проверить: все три новые карточки показывают "--" (нет данных)
4. Подключиться к модулю батареи (нажать Bluetooth → скан → нажать на имя модуля)
5. Подождать до 5 секунд загрузки BMS данных
6. Подождать ещё до 5 секунд загрузки settings данных (всего ~10 секунд)
7. Проверить три новые карточки:
   - **Selected ID** — показывает "ID1" (или "ID2"–"ID16")
   - **Selected CAN** — показывает название протокола (например "P01-GRW")
   - **Selected RS485** — показывает название протокола (например "P01-GRW")
8. Перейти на экран **Settings** (иконка шестерёнки)
9. Сравнить: значения на карточках Home screen должны совпадать со значениями на Settings
10. Вернуться на Home screen — значения должны остаться (не "--")

**Ожидаемый результат:**
- Шаг 3: все три карточки показывают "--" до подключения
- Шаг 7: все три карточки показывают реальные значения (не "--")
- Шаг 9: значения Home screen совпадают с Settings
- Шаг 10: значения сохраняются после навигации обратно

**Если не прошёл:**
1. Скриншот Home screen с тремя карточками
2. Скриншот Settings для сравнения
3. Diagnostics → **Send Logs**
4. Заметка: `ID: ок/--`, `CAN: ок/--`, `RS485: ок/--`, `совпадают с Settings: да/нет`

> Карточки загружаются ~2 секунды после BMS данных. Если "--" мелькнуло на секунду и потом появились значения — это нормально. Отмечать FAIL только если "--" остаётся дольше 10 секунд.

---

### После всех тестов

**Что нужно прислать:**
1. Заполненная таблица результатов
2. Скриншоты: экран Settings (значения Module ID, CAN, RS485), экран Diagnostics
3. Логи — должны быть уже отправлены по email после каждого проваленного теста
4. Один финальный **Send Logs** из Diagnostics (даже если все тесты прошли)
