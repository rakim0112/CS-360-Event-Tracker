# EVENT / 47 — CS-360 Project Three

**Rakim Spriggs | Southern New Hampshire University**

EVENT / 47 is an Android event tracker I worked on for CS-360. I wanted an app that could keep school, work, and other commitments in one place without requiring an internet connection. Users can create an account, sign in, and save events with dates, times, locations, and notes. The data is stored locally in SQLite.

**[Project Three app code ZIP](portfolio/CS360_Project_Three_App_Code_Design.zip)** · **[Earlier Project Two UI layouts](coursework/project-two/)** · **[Testing notes](TESTING.md)**

The ZIP contains the Project Three Android Studio Java source, implemented UI, app resources, and Gradle project files. The original Project Two designs are available separately in the linked `coursework/project-two` folder. To run the Project Three app, extract the ZIP, open `CS360_Event47` in Android Studio, sync Gradle, and run the `app` configuration.

## Module Eight Reflection

My goal was to make it easy to check upcoming events and make changes without digging through menus. I started with a login screen and built an events dashboard, a calendar picker, an add/edit screen, and a profile screen. The dashboard has a next-event card, search, date filters, and a two-column grid. I kept the black-and-red design consistent and made the main actions easy to spot. Those choices came from the UI work in Project Two: I wanted the screens to feel connected and to make sense to someone opening the app for the first time.

I built the app in Java using Android Studio. I kept account and event storage in a `DatabaseHelper` class and used `EventRecord` to pass event details between the database and the screens. Working feature by feature helped me make progress: first login and event storage, then the grid, editing controls, filters, and reminders. One coding decision I would change is putting so much screen-building logic in `MainActivity`. It made the project manageable at first, but some methods grew too long. In another version I would split the screens into smaller components so the code is easier to read and maintain.

For testing, I used a Pixel 6 to create an account, sign in, add and delete events, view the grid, open the editor, and try both responses to the SMS permission request. This confirmed that the main event features still work when permission is denied. I did **not** finish the restart test for saved edits or SQLite persistence, and I did **not** confirm SMS delivery. I tried using a Pixel 6a emulator for the message test, but it became unresponsive. Keeping track of what I tested, rather than assuming a feature works because its controls are visible, was an important lesson.

SMS reminders were my biggest design challenge. The feature needed more than a permission button: users also needed a place to enter a phone number and a clear way to trigger reminders. I added an optional number field and a manual check for events due within 24 hours. Users can turn reminders off for an event, and Android app settings control whether SMS permission is granted. I still need to test message delivery between working devices and check the permission-revocation flow before calling the feature finished.

The part I am most satisfied with is connecting the event grid and account system to SQLite. It helped me understand how data, interface controls, and navigation fit together in an actual app. I plan to use the same step-by-step development and testing process on future projects, while organizing the UI into smaller classes from the beginning. Before publishing EVENT / 47, I would finish the outstanding tests, review device compatibility, and examine Google Play's SMS permission requirements.

*This repository is a coursework portfolio artifact. It is not a verified Google Play release, and the SMS delivery test remains incomplete.*
