# EVENT / 47

This is my event-tracking app for CS-360 Project Three. I built it as an Android Studio Java project with a black-and-red interface.

## Features

- Create an account and sign in
- Add, edit, delete, and search events
- Filter upcoming and past events, or use the calendar
- Keep event details in a local SQLite database
- Optional SMS reminders for events in the next 24 hours

The reminder check is **manual**. The app asks for SMS permission and confirmation before attempting to send a message. The rest of the app still works if permission is denied.

## Run

Open the repository root in Android Studio, let Gradle sync, choose a device, and run the `app` configuration.

## Earlier coursework

- [Project Two UI layouts](coursework/project-two/) — initial event-tracker screen designs
- [Module Six sensor app](coursework/module-six/) — accelerometer exercise, separate from the event tracker

Each coursework folder is its own Android Studio project. Open them separately if needed; the main Project Three app lives at the repository root.

## Testing

On my Pixel 6, I was able to register/sign in, add and delete events, view the event grid, and test both permission responses for SMS. I opened the event editing screen, but still need a clean restart test to confirm saved edits and database persistence.

I couldn't complete the end-to-end SMS test because the emulator became too slow and stopped responding. **SMS delivery is not verified.** See [TESTING.md](TESTING.md) for the test notes.

The direct-SMS feature is for the class assignment. A production version would need to account for Google Play's restrictions on SMS permissions.
