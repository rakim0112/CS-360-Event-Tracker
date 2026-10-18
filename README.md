# EVENT / 47 — CS-360 Mobile Architecture and Programming Portfolio

**Developer:** Rakim Spriggs  
**Course:** CS-360, Southern New Hampshire University  
**Portfolio artifact:** Project Three — Android App Code Design

EVENT / 47 is a Java-based Android event-tracking application created in Android Studio. The final application builds on my Project Two UI designs and uses local SQLite storage, account sign-in, event management, and optional user-initiated SMS reminders.

## Portfolio artifact and project files

- **[Download the completed Project Three app code design ZIP](portfolio/CS360_Project_Three_App_Code_Design.zip)** — Android Studio project source, finalized UI implementation, Gradle configuration, and Project Two UI layout references.
- **[Project Three source code](app/src/main/)** — the final app's Java classes, Android manifest, and UI resources.
- **[Project Two original UI design](coursework/project-two/)** — the screen layouts I used as a baseline.
- **[Testing notes](TESTING.md)** — verified behavior and tests that still need to be completed.

To open the app in Android Studio, extract the portfolio ZIP, select its `CS360_Event47` project directory, let Gradle sync, and run the `app` configuration on an Android device or emulator. The original project can also be opened directly from the repository root.

## Module Eight Journal Reflection

### What were the requirements and goals of the app, and what user needs did it address?

I developed EVENT / 47 to give users a straightforward way to organize important commitments without switching between multiple tools or relying on a continuous internet connection. The intended users include students, working adults, and people managing appointments, meetings, and creative projects. The app needed login and account creation, a clear display of stored events, ways to add or update event details, SQLite storage, and an option to request SMS reminder permission. My goal was to make scheduling practical: users should be able to find what is coming up, store dates and locations, and manage reminders without making the rest of the app dependent on SMS access.

### What screens and features supported a user-centered design, and why was the UI effective?

The design started with login and account-creation screens, followed by an events dashboard with a prominent next-event area, searchable event cards, and filters for upcoming, current, past, or all events. Users can tap an event to open its editor, add an event with the main action control, use a calendar date picker, and visit a profile area to review SMS permission status. The event editor includes a title, date, time, optional location and notes, and an optional phone number for reminders. I used a consistent black-and-red visual style, readable labels, clearly separated controls, and a two-column event grid so users could scan information without navigating through unnecessary screens. These choices were aimed at reducing confusion and keeping common actions easy to find. The layout gave me a workable foundation that I refined from the earlier Project Two screens.

### How did I approach coding, and which strategies will I use again?

I approached development by breaking the project into manageable responsibilities rather than attempting every feature simultaneously. I used Java and Android Studio for the app, a separate `DatabaseHelper` class based on `SQLiteOpenHelper` for account and event data, and an `EventRecord` model to move event information between storage and the interface. The database associates events with their owner and uses salted password hashing for stored account credentials. I implemented the screens and event controls around this structure, then added search and filtering, input validation, and opt-in SMS permission handling. In future projects I will use the same incremental approach: establish the data model, connect the main user workflow, test each interaction, and only then expand functionality.

### How did I test the app, why did testing matter, and what did it reveal?

I tested account creation and sign-in on a Pixel 6, added events to the two-column grid, deleted an event, opened the editing screen, and checked both granting and denying SMS permission. Denying SMS permission did not prevent the core event-management features from being used. Testing exposed an important distinction between an interface that presents an option and a feature whose complete behavior has been demonstrated. I did not fully verify saving edits after restarting the application or confirm that SQLite changes persisted across a clean restart. I also could not confirm delivery of an SMS to another device: the Pixel 6a emulator I used for that test became unresponsive. These results showed me why I need to document tests precisely and repeat important checks on a reliable device before claiming a feature is complete.

### Where did I need to innovate to overcome a challenge?

The SMS feature presented the clearest challenge. It was not enough to display a reminder checkbox or ask for permission; I needed a way for the user to supply a destination number, understand when a reminder could be sent, and choose whether to proceed. I added a phone-number input and a clearly labeled manual reminder check for events scheduled within the next 24 hours. The app requests permission only when needed and continues to manage events if permission is declined. Because the emulator stalled, the remaining step is to verify actual message delivery and the sending result on a suitable device. This challenge taught me to design around user control, device limitations, and the difference between implementing code and verifying behavior.

### Where was I most successful at demonstrating my skills?

I am particularly proud of connecting the event-management interface to a structured SQLite data layer. Building account creation, per-user event records, the searchable grid, and event creation and deletion required me to coordinate Java classes, user input, data storage, and navigation. It helped me move beyond designing individual screens to thinking about the relationships between features in a complete Android application. The project demonstrates my growing ability to organize code, make user-centered design decisions, and identify unfinished testing work without overstating the application's readiness.

## Future improvements and release considerations

Before attempting a public Google Play release, I would complete a clean app-restart persistence test, recheck edited-event saving, verify SMS behavior on appropriate hardware, improve accessibility and device compatibility testing, and review current Google Play policies around restricted SMS permissions. **This repository documents coursework, not a claim that the app has been publicly launched or that SMS delivery has been verified.**
