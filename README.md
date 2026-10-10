# EVENT / 47 — CS-360 Event-Tracking App

An Android Studio Java event-tracking app built for SNHU CS-360 Project Three, based on the Project Two event-tracking UI plan.

## Features
- Private account registration and sign-in with locally stored salted password hashes
- Persistent SQLite database storing user accounts and event records
- Create, read, edit and delete event records; searchable two-column grid
- Date filters, Next Event card, Calendar and Profile views
- Optional SMS reminders with a runtime permission flow; core app remains functional when denied
- Black-and-red EVENT / 47 appearance with subtle watermark details

## Run
Open this repository's root folder in Android Studio Quail 4 (2026.1.4 Patch 1), sync Gradle, then launch `app` on an Android 16-capable emulator or test device. See [`README_FIRST.md`](README_FIRST.md) for detailed instructions.

## Testing status (October 10, 2026)
- Physical Pixel 6: app launched; user login/registration, event grid, create/delete, and SMS grant/deny flows demonstrated with screenshots.
- Event update and post-restart SQLite persistence were not independently documented in the conversation.
- **SMS delivery not verified**; the emulator was too slow to complete the two-device test. SMS is only attempted after the user explicitly checks reminders.
- The source is not represented as an independently certified error-free release or Play Store-ready app.

## Notes
- Coursework SMS functionality requests `SEND_SMS`. Public Google Play distribution of that permission is restricted. A production release should use compliant local notifications instead, and remove SMS access if unnecessary.
- Build caches, SDK paths, IDE settings, API keys, test-user databases and personal screenshots are intentionally excluded.
- **AI acknowledgment:** ChatGPT assisted with initial Java/XML code, interface design, test planning and the written launch plan. The student reviewed, tested and debugged the Android app on device and emulator as described.
