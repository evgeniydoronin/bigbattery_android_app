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
- **Diagnostics** — accessible from navigation, has "Send Logs" button

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

- Screenshot the Settings screen
- Note which values show "--" or are missing
- Go to Diagnostics → tap **Send Logs**
- Write in Notes: "Connect + Settings — [what failed]"

### Log lines to look for
```
CONNECTION event — successful connection
protocolInfo section in JSON:
  "moduleId" — should have a value
  "canProtocol" — should have a value
  "rs485Protocol" — should have a value
```

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

- Screenshot the Settings screen
- Note which element is missing or wrong
- Go to Diagnostics → tap **Send Logs**
- Write in Notes: "Settings UI — [what is missing/wrong]"

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

- Screenshot showing the "--" values
- **Do NOT navigate away** — stay on Settings screen
- Wait 10 more seconds, check if values appear
- Go to Diagnostics → tap **Send Logs**
- Write in Notes: "Protocol loading — [which field shows '--', how long you waited]"

### Log lines to look for
```
protocolInfo section in diagnostics JSON:
  "moduleId" — should have a value
  "canProtocol" — should have a value
  "rs485Protocol" — should have a value
```

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

- Screenshot before and after Save
- Note if the confirmation dialog appeared
- Note if battery restarted or not
- Go to Diagnostics → tap **Send Logs**
- Write in Notes: "Protocol save — [what happened]"

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
2. Go to Settings screen
3. Check values
4. Note: "Auto-reconnect did not work, manual reconnect required"

### If FAILED

- Note how long you waited before giving up
- Note if the app crashed or froze
- Screenshot Settings screen after reconnection (especially "--" values)
- Go to Diagnostics → tap **Send Logs**
- Write in Notes: "Mid-session reconnect — [auto/manual], waited [X] seconds, Settings values: [what you see]"

### Log lines to look for
```
DISCONNECTION event — when signal was lost
CONNECTION event — when reconnected
```

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

- Note any error messages on screen
- Screenshot Settings screen (especially "--" values)
- Go to Diagnostics → tap **Send Logs**
- Write in Notes: "Cross-session — [auto/manual/failed], Settings values: [what you see]"

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

- Screenshot any time values show "--" or differ
- Note which round trip failed (1st, 2nd, or 3rd)
- Go to Diagnostics → tap **Send Logs**
- Write in Notes: "Settings navigation — round trip [#], [which value changed/disappeared]"

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

- Screenshot the Diagnostics screen
- Screenshot any error when trying to send
- Note: "Diagnostics export — [what went wrong]"
- If email won't open: check that an email app is set up on the phone

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

Diagnostics screen
  ├── Back button (top left)
  ├── Events list (scrollable, shows timestamps)
  └── Send Logs button (creates JSON → opens email)
```

### What "--" means
If you see "--" instead of a value on the Settings screen, it means the protocol data has not loaded yet. Wait 3-5 seconds. If it persists, that is a bug — mark the test as FAILED and send logs.

### Battery restart after Save
When you tap Save in Settings, the battery module restarts. This is **normal behavior**. The app will disconnect temporarily. Wait for it to reconnect, then go back to Settings to verify the new value.

### Timing reference
- Protocol data loads on Settings: ~1.2 seconds (sequential: Module ID → CAN → RS485)
- Battery restart after Save: 5-10 seconds
- Auto-reconnect after signal loss: up to 10 seconds
