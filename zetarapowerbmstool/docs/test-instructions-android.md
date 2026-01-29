# Android App Test Instructions — Settings Screen

**App:** Big Battery BMS Tool (Android)
**Package:** `com.zetarapower.monitor.bl`
**Scope:** Settings screen, BLE connection, Diagnostics export
**Date:** January 2025

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
- **Settings** — gear icon, shows Module ID / CAN / RS485 cards
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
| 1 | Connect + Open Settings | | | | | |
| 2 | Settings Screen UI | | | | | |
| 3 | Protocol Data Loading | | | | | |
| 4 | Protocol Change + Save | | | | | |
| 5 | Mid-Session Reconnect → Settings | | | | | |
| 6 | Cross-Session Reconnect → Settings | | | | | |
| 7 | Settings Navigation (round trips) | | | | | |
| 8 | Diagnostics Export | | | | | |

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

## Test 6: Cross-Session Reconnect → Settings (IMPORTANT)

**Goal:** Verify Settings data loads correctly after app kill and reopen.

### Steps

1. Ensure the app is connected
2. Go to Settings, **write down** values: Module ID, CAN, RS485
3. **Kill the app** completely:
   - Press the Recent Apps button (square button)
   - Swipe the app away to force close
4. Wait 3 seconds
5. Open the app again
6. Connect to the module (auto or manual — note which happened)
7. Go to Settings screen
8. Check values: Module ID, CAN, RS485

### Expected Result

- App reopens without crash
- Connection established (auto or manual)
- Settings shows the **same** values as step 2
- No "--" values after data loads (wait up to 3 seconds)

**Note which reconnect type happened** (auto or manual) in the results table.

### If FAILED

1. Screenshot Settings screen
2. Diagnostics → **Send Logs**
3. Notes — short: `auto / manual / failed`, `Settings: ok / -- on [field]`

> Same as Test 5 — logs can't distinguish auto-reconnect from manual. Also, if the app shows an error message on screen, write it down — that text doesn't end up in the logs.

### Known issue (from iOS)
In iOS builds 43-44, the saved device UUID was lost on app restart, making auto-reconnect impossible. Check if Android has the same issue.

---

## Test 7: Settings Navigation — Round Trips (IMPORTANT)

**Goal:** Verify navigating away from Settings and back preserves protocol data.

### Steps

1. Go to Settings screen
2. **Write down** (or screenshot) the values: Module ID, CAN, RS485
3. Go back (leave Settings screen)
4. Wait 3 seconds
5. Go to Settings screen again
6. Check: Module ID, CAN, RS485 values are the same as step 2
7. Repeat steps 3-6 **two more times** (total 3 round trips)

### Expected Result

- All three round trips: Settings values remain the same
- No "--" appearing on any round trip
- No delay longer than 3 seconds to show values
- Values match what was noted in step 2

### If FAILED

1. Screenshot Settings screen the moment you see "--"
2. Diagnostics → **Send Logs**
3. Notes — short: `round trip #X`, `[field] showed --` or `[field] changed value`

> Logs capture protocol data once (at export time), but they won't show that values were fine on round trip 1 and broke on round trip 3. That's what your notes are for.

### Known issue (from iOS)
In iOS builds 45-47, navigating away could cancel BLE subscriptions, causing Module ID to show "--" on return. This is the race condition bug.

---

## Test 8: Diagnostics Export (IMPORTANT)

**Goal:** Verify the Diagnostics screen works and logs can be sent.

### Steps

1. Go to the **Diagnostics** screen
2. Check: the screen shows a list of events (connection, data updates, etc.)
3. Scroll through the list — events should have timestamps
4. Tap the **Send Logs** button
5. An email compose screen should open with:
   - **Recipient:** pre-filled email address
   - **Subject:** pre-filled subject line
   - **Attachment:** JSON file (named `bigbattery_logs_android_YYYYMMDD_HHMMSS.json`)
6. Send the email

### Expected Result

- Step 2: Events list is visible and not empty
- Step 4-5: Email compose opens with attachment
- Step 6: Email sends successfully
- The JSON file contains diagnostic data (device info, battery info, protocol info, events)

### If FAILED

1. Screenshot the Diagnostics screen
2. Screenshot the error (if any)
3. Notes — short: `no events / email won't open / send error`

> If email won't open at all — most likely no email app is configured on the phone. That's a setup issue, not a bug.

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

### Тест 6: Переподключение после перезапуска → Settings (IMPORTANT)

**Цель:** Проверить, что данные Settings загружаются после полного закрытия и открытия приложения.

1. Убедиться, что приложение подключено
2. На Settings **записать** значения: Module ID, CAN, RS485
3. **Убить приложение** полностью:
   - Нажать кнопку "Последние приложения" (квадрат)
   - Смахнуть приложение для принудительного закрытия
4. Подождать 3 секунды
5. Открыть приложение заново
6. Подключиться к модулю (авто или вручную — записать какой вариант)
7. Перейти на экран Settings
8. Проверить значения: Module ID, CAN, RS485

**Ожидаемый результат:**
- Приложение открывается без краша
- Подключение установлено (авто или вручную)
- Settings показывает **те же** значения, что записаны в шаге 2
- Нет "--" после загрузки (подождать до 3 секунд)

**Записать** какой тип реконнекта произошёл (авто или ручной).

**Известная проблема (из iOS):** В iOS builds 43-44 UUID устройства терялся при перезапуске — авто-реконнект не работал. Проверить есть ли та же проблема на Android.

**Если не прошёл:**
1. Скриншот Settings
2. Diagnostics → **Send Logs**
3. Заметка: `авто / вручную / не подключился`, `Settings: ок / -- на [поле]`

> То же, что в тесте 5 — логи не отличат авто-реконнект от ручного. Если на экране появилось сообщение об ошибке — запишите текст, он в логи не попадает.

---

### Тест 7: Навигация Settings — туда-обратно (IMPORTANT)

**Цель:** Проверить, что переход с Settings и обратно сохраняет данные протоколов.

1. Перейти на экран Settings
2. **Записать** (или скриншот) значения: Module ID, CAN, RS485
3. Уйти с экрана Settings (нажать назад)
4. Подождать 3 секунды
5. Вернуться на экран Settings
6. Проверить: Module ID, CAN, RS485 — те же значения, что в шаге 2
7. Повторить шаги 3-6 **ещё два раза** (всего 3 цикла)

**Ожидаемый результат:**
- Все три цикла: значения Settings остаются прежними
- Нигде не появляется "--"
- Задержка загрузки не более 3 секунд
- Значения совпадают с записанными в шаге 2

**Известная проблема (из iOS):** В iOS builds 45-47 переход с экрана мог отменить BLE-подписки, из-за чего Module ID показывал "--" при возврате. Это баг race condition.

**Если не прошёл:**
1. Скриншот Settings в момент появления "--"
2. Diagnostics → **Send Logs**
3. Заметка: `цикл #X`, `[поле] показало --` или `[поле] изменило значение`

> Логи фиксируют данные один раз (в момент экспорта), но не расскажут, что на 1-м цикле всё было нормально, а на 3-м сломалось. Для этого и нужна ваша заметка.

---

### Тест 8: Экспорт диагностики (IMPORTANT)

**Цель:** Проверить, что экран Diagnostics работает и логи можно отправить.

1. Перейти на экран **Diagnostics**
2. Проверить: на экране отображается список событий (подключения, обновления данных и т.д.)
3. Пролистать список — у событий должны быть временные метки
4. Нажать кнопку **Send Logs**
5. Должен открыться экран создания email с:
   - **Получатель:** предзаполненный email
   - **Тема:** предзаполненная тема
   - **Вложение:** JSON-файл (имя `bigbattery_logs_android_YYYYMMDD_HHMMSS.json`)
6. Отправить email

**Ожидаемый результат:**
- Список событий виден и не пуст
- Email открывается с вложением
- Email отправляется успешно

**Если не прошёл:**
1. Скриншот Diagnostics
2. Скриншот ошибки (если есть)
3. Заметка: `нет событий / email не открывается / ошибка отправки`

> Если email вообще не открывается — скорее всего на телефоне не настроена почта. Это не баг приложения, а вопрос настройки телефона.

---

### После всех тестов

**Что нужно прислать:**
1. Заполненная таблица результатов
2. Скриншоты: экран Settings (значения Module ID, CAN, RS485), экран Diagnostics
3. Логи — должны быть уже отправлены по email после каждого проваленного теста
4. Один финальный **Send Logs** из Diagnostics (даже если все тесты прошли)
