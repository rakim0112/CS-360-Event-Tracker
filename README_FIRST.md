# EVENT / 47 — CS-360 Project Three
**Android Studio project:** CS360_Event47  |  **Student:** Rakim Spriggs

## Project Two → Project Three
This is a clean, standalone implementation of the **Event-Tracking App** described in the Project Two UI design. The design document was accessible, but the actual Project Two Android Studio source folder was not available here, so do not mistake this ZIP for the original Project Two workspace. It preserves the planned Next Event card, search, Upcoming/Today/Past filtering, event card grid, Add/Edit flow and Events/Calendar/Profile navigation. The new visual identity uses dark charcoal, red accents, a faint 47 background and gently moving particles. No generic purple Android template.

## Open in Android Studio Quail 4 (2026.1.4)
1. Extract the ZIP to a folder (don't open it inside the ZIP).
2. **File → Open → CS360_Event47**, the folder containing `settings.gradle.kts`.
3. Let Android Studio sync the included Gradle 9.6 / AGP 9.4.1 project. Set Gradle JDK to its embedded JDK if asked.
4. Install SDK Android API 37 from SDK Manager if your local SDK does not already have it (your Module6 project used API 37).
5. Select your Pixel 6 API 36.1 emulator and Run `app`. For Android 17 compatibility checks, use an API 37 emulator as well if available.
6. If you encounter any build errors, capture **the first error in Build Output**, not just a screenshot of the app.

## Functional test script — capture evidence for grading
| ID | Action | Expected result |
|---|---|---|
| A1 | Launch app and tap Create Account | Registration screen opens |
| A2 | Create user `demo47` with a password of 8+ characters | Dashboard opens, no event records yet |
| A3 | Sign out; try a wrong password; then log in correctly | Wrong password rejected; correct password accepted |
| D1 | Add event for tomorrow, title/location/notes, no SMS | It appears in the Upcoming event grid |
| D2 | Tap card, edit title/date/location | Grid shows updated data |
| D3 | Use Today, Past, All, search, and Calendar date picker | Correct saved events appear, and calendar can open one |
| D4 | Close/reopen app; log back into same account | Saved data is still present (SQLite persistence) |
| D5 | Delete event using confirmation | Deleted event disappears from grid |
| M1 | Create event within next 24h with SMS enabled and a test phone number, **grant permission** | Check Reminders asks confirmation; sends SMS using SMS-capable emulator/device; inspect received SMS / status |
| M2 | Deny SMS permission (fresh installation or App info → Permissions); create/edit/delete events | All core CRUD features still work; Check Reminders explains permission requirement |
| M3 | If emulator reports SMS cannot send | Record that limitation; do not claim success. Try SMS-capable emulator with two virtual devices or supported physical device |

**Note:** Sending direct SMS is course-only functionality. Google Play severely limits `SEND_SMS` for non-default SMS apps. For an actual store launch, replace with scheduled local notifications via NotificationManager, and remove `SEND_SMS` unless approved by policy.

## Source map
- `MainActivity.java`: screens, search/filter/grid, CRUD UI, permission branch and user-triggered SMS testing.
- `DatabaseHelper.java`: persistent SQLite tables, scoped CRUD, and PBKDF2 password hashes/salts.
- `EventRecord.java`: event model.
- `SignatureBackdrop.java`: near-black/red animated low-key 47 motif.
- `activity_main.xml`: root host layout.

## Status and limits
These files were authored and statically checked here (Java source structure, Gradle and XML, ZIP integrity). **No Android SDK or emulator was available in this workspace to compile or run the project.** A successful Run/Debug in your Android Studio is required before calling this APK-ready or error-free. APK not included. After testing, submit the ZIP **of this updated Android Studio project folder**, plus the separate Word launch plan. The draft launch plan must be checked against results and feedback before final submission.

## AI acknowledgment
ChatGPT assisted with initial Java/XML implementation, design choices, test planning, and launch-plan drafting. The student must review, execute, debug, and acknowledge AI usage per SNHU course guidance. No claims of executed tests were manufactured.
