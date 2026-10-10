# CS-360 Project Three: Test Evidence Summary

Evidence screenshots were shared separately with the instructor/project archive and are intentionally not included in this source repository.

| Rubric area | Status |
| --- | --- |
| Android application installation and visual UI | Demonstrated on Pixel 6 |
| Account registration/login | Demonstrated by registration and authenticated profile |
| SQLite event creation/read | Demonstrated with multiple stored events and profiles |
| Database grid display | Demonstrated with 3 events in two columns |
| Event deletion | Demonstrated via deletion confirmation |
| Event editing and post-restart persistence | Code present; no independently verified restart test |
| SMS permission allowed | Demonstrated |
| SMS permission denied, core functionality intact | Demonstrated |
| Actual SMS delivery | NOT VERIFIED: emulator performance issues |

## Reproducing unfinished tests
1. Add an event, change its date and title, save, and confirm the grid reflects edits.
2. Force-close the app, reopen, sign in, and verify SQLite records persist.
3. On a telephony-capable emulator/test device, create an opt-in event due within 24 hours with a test number you control; grant SEND_SMS and press Check Reminders; verify receipt.
4. Deny SEND_SMS, then confirm that event creation and editing work without SMS.

Never claim SMS delivery succeeded without observing receipt.
