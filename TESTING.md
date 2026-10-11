# Testing notes

## Completed

- Account creation and login on the Pixel 6
- Adding events and displaying them in the two-column grid
- Deleting an event
- Opening an event to edit it
- Granting SMS permission
- Denying SMS permission while continuing to use the event features

## Not fully verified

- Saving edits, closing the app, and checking the changed event after reopening
- Confirming SQLite data survives a full app restart
- Actual SMS delivery to another device

I tried a Pixel 6a emulator for the SMS test, but it became unresponsive. I stopped the test rather than count an unconfirmed SMS as working.

The reminder button checks only for events due within the next 24 hours. It doesn't schedule automatic background delivery.
