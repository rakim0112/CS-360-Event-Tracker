package com.rakim.event47;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** EVENT / 47: login, persistent SQLite CRUD, a card grid, and opt-in SMS reminders. */
public final class MainActivity extends Activity {
    private static final int BACKGROUND = 0xFF08090D;
    private static final int SURFACE = 0xF91C1D22;
    private static final int SURFACE_ALT = 0xFF24232A;
    private static final int TEXT = 0xFFF7F4F3;
    private static final int MUTED = 0xFFBAB5BB;
    private static final int RED = 0xFFEA3D49;
    private static final int BORDER = 0xFF4A333A;
    private static final int SMS_REQUEST = 47;
    private static final String SMS_SENT_ACTION = "com.rakim.event47.SMS_SENT";

    private final List<EventRecord> visible = new ArrayList<>();
    private DatabaseHelper db;
    private FrameLayout root;
    private long currentUserId = -1;
    private String currentUsername = "";
    private String filter = "Upcoming";
    private String query = "";
    private GridLayout eventGrid;
    private TextView emptyLabel;
    private TextView nextLabel;
    private TextView counterLabel;
    private final DateFormat dateTime = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US);
    private final DateFormat dateOnly = new SimpleDateFormat("MMM d, yyyy", Locale.US);
    private final DateFormat timeOnly = new SimpleDateFormat("h:mm a", Locale.US);
    private BroadcastReceiver smsReceiver;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.main_root);
        root.setBackgroundColor(BACKGROUND);
        db = new DatabaseHelper(this);
        registerSmsReceiver();
        showAuthentication(false);
    }

    @Override protected void onDestroy() {
        if (smsReceiver != null) unregisterReceiver(smsReceiver);
        if (db != null) db.close();
        super.onDestroy();
    }

    private void registerSmsReceiver() {
        smsReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                if (!SMS_SENT_ACTION.equals(intent.getAction())) return;
                long eventId = intent.getLongExtra("event_id", -1);
                long ownerId = intent.getLongExtra("owner_id", -1);
                if (getResultCode() == Activity.RESULT_OK && eventId > 0 && ownerId > 0) {
                    db.markReminded(eventId, ownerId);
                    toast("SMS submitted successfully");
                } else {
                    toast("SMS could not be sent. Check emulator/carrier and try again.");
                }
                if (currentUserId == ownerId && eventGrid != null) redrawEvents();
            }
        };
        IntentFilter filter = new IntentFilter(SMS_SENT_ACTION);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(smsReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(smsReceiver, filter);
        }
    }

    /** Each screen has a fresh view hierarchy; backgrounds share the same 47 aesthetic. */
    private LinearLayout makePage() {
        root.removeAllViews();
        root.addView(new SignatureBackdrop(this), new FrameLayout.LayoutParams(-1, -1));
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(18), dp(22), dp(18));
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        eventGrid = null;
        emptyLabel = null;
        nextLabel = null;
        counterLabel = null;
        return page;
    }

    private void showAuthentication(boolean creating) {
        LinearLayout page = makePage();
        page.setGravity(Gravity.CENTER_VERTICAL);
        add(page, label("47  /  MOMENTS", 13, RED, true), -1, 28);
        add(page, label(creating ? "Make your mark." : "Make every moment count.", 31, TEXT, true), -1, 12);
        add(page, label("EVENT / 47   •   THE PRIVATE EVENT JOURNAL", 11, MUTED, false), -1, 34);
        add(page, label(creating ? "CREATE ACCOUNT" : "WELCOME BACK", 13, TEXT, true), -1, 14);
        EditText username = input("Username", InputType.TYPE_CLASS_TEXT);
        add(page, username, -1, 12);
        EditText password = input("Password (at least 8 characters)",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        add(page, password, -1, 18);
        TextView submit = action(creating ? "CREATE ACCOUNT  ↗" : "SIGN IN  ↗", true);
        add(page, submit, -1, 15);
        submit.setOnClickListener(v -> {
            String u = username.getText().toString().trim();
            String p = password.getText().toString();
            if (!u.matches("[A-Za-z0-9_.-]{3,24}")) {
                username.setError("Use 3–24 letters, numbers, dots, _ or -");
                return;
            }
            if (creating && p.length() < 8) {
                password.setError("Use at least 8 characters");
                return;
            }
            if (!creating && p.isEmpty()) {
                password.setError("Password required");
                return;
            }
            if (creating) {
                long id = db.createUser(u, p);
                if (id < 0) {
                    username.setError("Username already taken");
                    return;
                }
                currentUserId = id;
            } else {
                long id = db.authenticate(u, p);
                if (id < 0) {
                    toast("Incorrect username or password");
                    return;
                }
                currentUserId = id;
            }
            currentUsername = u;
            query = "";
            filter = "Upcoming";
            showDashboard();
        });
        TextView switchMode = label(creating ? "Already have an account? Sign in"
                : "New here? Create a free account", 14, MUTED, false);
        switchMode.setGravity(Gravity.CENTER);
        switchMode.setPadding(0, dp(14), 0, dp(14));
        add(page, switchMode, -1, 12);
        switchMode.setOnClickListener(v -> showAuthentication(!creating));
        add(page, label("PRIVATE BY DESIGN  •  STORED ON THIS DEVICE", 11, MUTED, false), -1, 0);
    }

    private void showDashboard() {
        LinearLayout page = makePage();
        LinearLayout top = horizontal();
        LinearLayout brand = vertical();
        add(brand, label("EVENT  /  47", 22, TEXT, true), -1, 3);
        add(brand, label("Your moments, on record.", 12, MUTED, false), -1, 0);
        top.addView(brand, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView logout = action("EXIT", false);
        top.addView(logout, new LinearLayout.LayoutParams(dp(70), dp(45)));
        logout.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Sign out?")
                .setMessage("Your events remain saved on this device.")
                .setPositiveButton("Sign out", (d, w) -> {
                    currentUserId = -1;
                    currentUsername = "";
                    showAuthentication(false);
                }).setNegativeButton("Stay", null).show());
        add(page, top, -1, 20);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        LinearLayout content = vertical();
        scroll.addView(content);
        add(content, label("GOOD TO SEE YOU, " + currentUsername.toUpperCase(Locale.US),
                11, RED, true), -1, 10);
        LinearLayout hero = panel();
        add(hero, label("NEXT EVENT   /   47", 11, RED, true), -1, 9);
        nextLabel = label("No upcoming events yet", 20, TEXT, true);
        add(hero, nextLabel, -1, 8);
        add(hero, label("A clear view of what's coming next.", 12, MUTED, false), -1, 0);
        add(content, hero, -1, 19);

        EditText search = input("Search titles, locations or notes…", InputType.TYPE_CLASS_TEXT);
        search.setSingleLine(true);
        search.setText(query);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                query = s.toString();
                redrawEvents();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        add(content, search, -1, 14);
        LinearLayout chips = horizontal();
        for (String name : new String[]{"Upcoming", "Today", "Past", "All"}) {
            TextView chip = label(name.toUpperCase(Locale.US), 11,
                    name.equals(filter) ? Color.WHITE : MUTED, true);
            chip.setGravity(Gravity.CENTER);
            chip.setBackground(shape(name.equals(filter) ? RED : SURFACE_ALT, 16, BORDER, 1));
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, dp(40), 1f);
            cp.rightMargin = dp(7);
            chips.addView(chip, cp);
            chip.setOnClickListener(v -> {
                filter = name;
                showDashboard();
            });
        }
        add(content, chips, -1, 20);
        LinearLayout countRow = horizontal();
        counterLabel = label("YOUR EVENTS", 12, TEXT, true);
        countRow.addView(counterLabel, new LinearLayout.LayoutParams(0, -2, 1f));
        add(content, countRow, -1, 12);
        eventGrid = new GridLayout(this);
        eventGrid.setColumnCount(2);
        eventGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        add(content, eventGrid, -1, 4);
        emptyLabel = label("Nothing here yet. Add a moment worth keeping.", 14, MUTED, false);
        emptyLabel.setPadding(dp(14), dp(35), dp(14), dp(35));
        emptyLabel.setGravity(Gravity.CENTER);
        add(content, emptyLabel, -1, 15);
        TextView checkSms = action("CHECK UPCOMING SMS REMINDERS", false);
        add(content, checkSms, -1, 12);
        checkSms.setOnClickListener(v -> checkReminders());
        add(content, label("Opt-in only • Due within 24 hours • Each event sends once",
                11, MUTED, false), -1, 15);

        TextView addEvent = action("+    ADD EVENT", true);
        add(page, addEvent, -1, 9);
        addEvent.setOnClickListener(v -> showEditor(null));
        LinearLayout navigation = horizontal();
        addNav(navigation, "EVENTS", () -> { filter = "Upcoming"; showDashboard(); });
        addNav(navigation, "CALENDAR", this::showCalendarPicker);
        addNav(navigation, "PROFILE", this::showProfile);
        add(page, navigation, -1, 0);
        redrawEvents();
    }

    /** Calendar destination opens a real date picker and shows events on that date. */
    private void showCalendarPicker() {
        Calendar selection = Calendar.getInstance();
        new DatePickerDialog(this, (picker, year, month, day) -> {
            Calendar start = Calendar.getInstance();
            start.set(year, month, day, 0, 0, 0);
            start.set(Calendar.MILLISECOND, 0);
            long first = start.getTimeInMillis();
            start.add(Calendar.DAY_OF_MONTH, 1);
            long last = start.getTimeInMillis();
            ArrayList<String> matches = new ArrayList<>();
            for (EventRecord e : db.getEvents(currentUserId)) {
                if (e.whenMillis >= first && e.whenMillis < last) {
                    matches.add(timeOnly.format(new Date(e.whenMillis)) + "   " + e.title);
                }
            }
            new AlertDialog.Builder(this)
                    .setTitle(dateOnly.format(new Date(first)))
                    .setItems(matches.isEmpty() ? new String[]{"No events saved for this day"}
                            : matches.toArray(new String[0]), (d, which) -> {
                        if (matches.isEmpty()) return;
                        for (EventRecord e : db.getEvents(currentUserId)) {
                            if (e.whenMillis >= first && e.whenMillis < last
                                    && matches.get(which).equals(timeOnly.format(new Date(e.whenMillis)) + "   " + e.title)) {
                                showEditor(e);
                                break;
                            }
                        }
                    })
                    .setPositiveButton("Close", null).show();
        }, selection.get(Calendar.YEAR), selection.get(Calendar.MONTH),
                selection.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showProfile() {
        LinearLayout page = makePage();
        TextView back = label("←  MY EVENTS", 13, MUTED, true);
        add(page, back, -1, 25);
        back.setOnClickListener(v -> showDashboard());
        add(page, label("Your space.", 31, TEXT, true), -1, 18);
        add(page, label("PROFILE    /    47", 11, RED, true), -1, 25);
        LinearLayout details = panel();
        add(details, label(currentUsername, 20, TEXT, true), -1, 9);
        add(details, label(db.getEvents(currentUserId).size() + " events saved privately on this device", 13, MUTED, false), -1, 16);
        add(details, label("Reminders use opt-in SMS permission. No contacts, camera, microphone or location permission is requested.", 13, MUTED, false), -1, 0);
        add(page, details, -1, 20);
        TextView permissions = action("SMS PERMISSION STATUS", false);
        add(page, permissions, -1, 14);
        permissions.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("SMS permission")
                .setMessage(checkSelfPermission(Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_GRANTED ? "Granted. The app can send opted-in reminders when you explicitly check them."
                        : "Not granted. Your event management still works. You can opt into SMS in an event editor.")
                .setPositiveButton("OK", null).show());
        TextView signout = action("SIGN OUT", true);
        add(page, signout, -1, 0);
        signout.setOnClickListener(v -> {
            currentUserId = -1;
            showAuthentication(false);
        });
    }

    private void addNav(LinearLayout parent, String label, Runnable action) {
        TextView item = label(label, 11, MUTED, true);
        item.setGravity(Gravity.CENTER);
        item.setMinHeight(dp(50));
        parent.addView(item, new LinearLayout.LayoutParams(0, dp(52), 1f));
        item.setOnClickListener(v -> action.run());
    }

    private void redrawEvents() {
        if (eventGrid == null || currentUserId < 0) return;
        List<EventRecord> all = db.getEvents(currentUserId);
        visible.clear();
        long now = System.currentTimeMillis();
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayStart = today.getTimeInMillis();
        today.add(Calendar.DAY_OF_YEAR, 1);
        long tomorrowStart = today.getTimeInMillis();
        EventRecord next = null;
        for (EventRecord event : all) {
            if (event.whenMillis >= now && (next == null || event.whenMillis < next.whenMillis)) next = event;
            boolean matches = filter.equals("All")
                    || (filter.equals("Upcoming") && event.whenMillis >= now)
                    || (filter.equals("Today") && event.whenMillis >= todayStart && event.whenMillis < tomorrowStart)
                    || (filter.equals("Past") && event.whenMillis < now);
            String haystack = (event.title + " " + event.location + " " + event.notes).toLowerCase(Locale.US);
            if (matches && haystack.contains(query.toLowerCase(Locale.US))) visible.add(event);
        }
        if (nextLabel != null) nextLabel.setText(next == null ? "Nothing on the horizon"
                : next.title + "\n" + dateTime.format(new Date(next.whenMillis)));
        eventGrid.removeAllViews();
        if (counterLabel != null) counterLabel.setText("YOUR EVENTS    •    " + visible.size());
        if (emptyLabel != null) emptyLabel.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        for (int index = 0; index < visible.size(); index++) {
            final EventRecord event = visible.get(index);
            LinearLayout card = panel();
            card.setBackground(shape(SURFACE, 18, BORDER, 1));
            TextView title = label(event.title, 16, TEXT, true);
            title.setMaxLines(2);
            add(card, title, -1, 8);
            add(card, label(dateOnly.format(new Date(event.whenMillis)), 12, RED, true), -1, 6);
            add(card, label(timeOnly.format(new Date(event.whenMillis)), 12, MUTED, false), -1, 6);
            add(card, label(event.location.isEmpty() ? "No location" : event.location, 12, MUTED, false), -1, 12);
            add(card, label(event.reminderEnabled ? "◉ SMS reminder" : "○ No SMS", 11,
                    event.reminderEnabled ? TEXT : MUTED, false), -1, 0);
            card.setContentDescription("Edit event " + event.title + ", " + dateTime.format(new Date(event.whenMillis)));
            card.setOnClickListener(v -> showEditor(event));
            GridLayout.LayoutParams gp = new GridLayout.LayoutParams(
                    GridLayout.spec(index / 2), GridLayout.spec(index % 2, 1f));
            gp.width = 0;
            gp.height = -2;
            gp.setMargins(dp(0), dp(0), dp(index % 2 == 0 ? 9 : 0), dp(12));
            eventGrid.addView(card, gp);
        }
    }

    private void showEditor(EventRecord existing) {
        LinearLayout page = makePage();
        boolean editing = existing != null;
        TextView back = label("←  MY EVENTS", 13, MUTED, true);
        back.setPadding(0, dp(8), 0, dp(14));
        add(page, back, -1, 15);
        back.setOnClickListener(v -> showDashboard());
        add(page, label(editing ? "Edit your moment." : "A new moment.", 31, TEXT, true), -1, 10);
        add(page, label("DETAILS    /    47", 11, RED, true), -1, 24);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        LinearLayout content = vertical();
        scroll.addView(content);
        add(content, label("EVENT TITLE", 11, MUTED, true), -1, 8);
        EditText title = input("e.g. Studio session", InputType.TYPE_CLASS_TEXT);
        title.setText(editing ? existing.title : "");
        add(content, title, -1, 20);

        final Calendar chosen = Calendar.getInstance();
        if (editing) chosen.setTimeInMillis(existing.whenMillis);
        else chosen.add(Calendar.HOUR_OF_DAY, 3);
        LinearLayout dateRow = horizontal();
        TextView dateButton = action("DATE: " + dateOnly.format(chosen.getTime()), false);
        TextView timeButton = action("TIME: " + timeOnly.format(chosen.getTime()), false);
        LinearLayout.LayoutParams dates = new LinearLayout.LayoutParams(0, dp(57), 1f);
        dates.rightMargin = dp(8);
        dateRow.addView(dateButton, dates);
        dateRow.addView(timeButton, new LinearLayout.LayoutParams(0, dp(57), 1f));
        add(content, dateRow, -1, 20);
        dateButton.setOnClickListener(v -> new DatePickerDialog(this,
                (picker, year, month, day) -> {
                    chosen.set(year, month, day);
                    dateButton.setText("DATE: " + dateOnly.format(chosen.getTime()));
                }, chosen.get(Calendar.YEAR), chosen.get(Calendar.MONTH),
                chosen.get(Calendar.DAY_OF_MONTH)).show());
        timeButton.setOnClickListener(v -> new TimePickerDialog(this,
                (picker, hour, minute) -> {
                    chosen.set(Calendar.HOUR_OF_DAY, hour);
                    chosen.set(Calendar.MINUTE, minute);
                    chosen.set(Calendar.SECOND, 0);
                    timeButton.setText("TIME: " + timeOnly.format(chosen.getTime()));
                }, chosen.get(Calendar.HOUR_OF_DAY), chosen.get(Calendar.MINUTE), false).show());

        add(content, label("LOCATION", 11, MUTED, true), -1, 8);
        EditText location = input("Optional venue or address", InputType.TYPE_CLASS_TEXT);
        location.setText(editing ? existing.location : "");
        add(content, location, -1, 20);
        add(content, label("NOTES", 11, MUTED, true), -1, 8);
        EditText notes = input("Anything to remember…",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        notes.setSingleLine(false);
        notes.setMinLines(3);
        notes.setGravity(Gravity.TOP | Gravity.START);
        notes.setText(editing ? existing.notes : "");
        add(content, notes, -1, 20);

        CheckBox sms = new CheckBox(this);
        sms.setText("Enable SMS reminder");
        sms.setTextColor(TEXT);
        sms.setButtonTintList(ColorStateList.valueOf(RED));
        sms.setChecked(editing && existing.reminderEnabled);
        add(content, sms, -1, 8);
        EditText phone = input("Phone number for SMS (required if enabled)", InputType.TYPE_CLASS_PHONE);
        phone.setText(editing ? existing.phone : "");
        add(content, phone, -1, 8);
        add(content, label("To test: save an event within 24 hours, grant SMS permission, then tap Check Reminders on the main screen.",
                12, MUTED, false), -1, 25);
        sms.setOnCheckedChangeListener((button, checked) -> {
            if (checked && checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_REQUEST);
            }
        });
        TextView save = action(editing ? "SAVE CHANGES  ↗" : "SAVE EVENT  ↗", true);
        add(page, save, -1, 8);
        save.setOnClickListener(v -> {
            String name = title.getText().toString().trim();
            String destination = phone.getText().toString().trim();
            if (name.isEmpty()) {
                title.setError("An event needs a title");
                return;
            }
            if (sms.isChecked() && destination.replaceAll("[^0-9+]", "").length() < 4) {
                phone.setError("Enter an SMS destination (emulator test numbers are supported)");
                return;
            }
            EventRecord event = new EventRecord();
            event.id = editing ? existing.id : 0;
            event.ownerId = currentUserId;
            event.title = name;
            event.whenMillis = chosen.getTimeInMillis();
            event.location = location.getText().toString().trim();
            event.notes = notes.getText().toString().trim();
            event.phone = destination;
            event.reminderEnabled = sms.isChecked();
            long result = db.saveEvent(event);
            if (result <= 0) {
                toast("Event could not be saved");
                return;
            }
            toast(editing ? "Changes saved" : "Event added");
            showDashboard();
        });
        if (editing) {
            TextView delete = action("DELETE EVENT", false);
            add(page, delete, -1, 0);
            delete.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Delete this event?")
                    .setMessage("This removes the event from your device.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.deleteEvent(existing.id, currentUserId);
                        toast("Event deleted");
                        showDashboard();
                    }).setNegativeButton("Cancel", null).show());
        }
    }

    /** User-triggered check avoids background spam and can be tested on the emulator. */
    private void checkReminders() {
        List<EventRecord> eligible = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (EventRecord event : db.getEvents(currentUserId)) {
            if (event.reminderEnabled && event.remindedAt == 0
                    && event.whenMillis >= now && event.whenMillis <= now + 86_400_000L
                    && !event.phone.isEmpty()) eligible.add(event);
        }
        if (eligible.isEmpty()) {
            toast("No unsent reminders for the next 24 hours");
            return;
        }
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this).setTitle("SMS permission required")
                    .setMessage("Event reminders need SMS permission. Your events will work normally if you decline.")
                    .setPositiveButton("Request permission", (d, w) ->
                            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_REQUEST))
                    .setNegativeButton("Not now", null).show();
            return;
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY_MESSAGING)) {
            toast("This emulator/device cannot send SMS. Try another SMS-capable emulator.");
            return;
        }
        new AlertDialog.Builder(this).setTitle("Send " + eligible.size() + " reminder(s)?")
                .setMessage("Sends one SMS per eligible event to its saved phone number. Carrier SMS charges may apply on real devices.")
                .setPositiveButton("Send SMS", (d, w) -> sendReminders(eligible))
                .setNegativeButton("Cancel", null).show();
    }

    private void sendReminders(List<EventRecord> events) {
        for (EventRecord e : events) {
            try {
                Intent signal = new Intent(SMS_SENT_ACTION);
                signal.setPackage(getPackageName());
                signal.putExtra("event_id", e.id);
                signal.putExtra("owner_id", e.ownerId);
                PendingIntent completion = PendingIntent.getBroadcast(this,
                        (int) (e.id & Integer.MAX_VALUE), signal,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                SmsManager manager;
                if (Build.VERSION.SDK_INT >= 31) {
                    manager = getSystemService(SmsManager.class);
                } else {
                    manager = SmsManager.getDefault();
                }
                if (manager == null) throw new IllegalStateException("SMS unavailable");
                manager.sendTextMessage(e.phone, null,
                        "EVENT / 47: Upcoming — " + e.title + " on "
                                + dateTime.format(new Date(e.whenMillis)), completion, null);
            } catch (Exception failure) {
                toast("SMS failed for " + e.title + ". Check number and permission.");
            }
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == SMS_REQUEST) {
            boolean granted = results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED;
            toast(granted ? "SMS permission granted. Use Check Reminders on the dashboard."
                    : "SMS denied. Your event calendar still works normally.");
        }
    }

    // ---- Consistent visual components; keeping the screens concise and accessible. ----
    private LinearLayout vertical() {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        return view;
    }
    private LinearLayout horizontal() {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.HORIZONTAL);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }
    private LinearLayout panel() {
        LinearLayout view = vertical();
        view.setPadding(dp(16), dp(17), dp(16), dp(17));
        view.setBackground(shape(SURFACE, 17, BORDER, 1));
        return view;
    }
    private TextView label(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        t.setLineSpacing(dp(2), 1f);
        return t;
    }
    private EditText input(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(MUTED);
        e.setTextColor(TEXT);
        e.setTextSize(14);
        e.setInputType(type);
        e.setPadding(dp(15), dp(14), dp(15), dp(14));
        e.setBackground(shape(SURFACE_ALT, 12, BORDER, 1));
        return e;
    }
    private TextView action(String name, boolean primary) {
        TextView view = label(name, 13, primary ? Color.WHITE : TEXT, true);
        view.setGravity(Gravity.CENTER);
        view.setBackground(new RippleDrawable(ColorStateList.valueOf(0x55FFFFFF),
                shape(primary ? RED : SURFACE_ALT, 14, primary ? RED : BORDER, 1), null));
        view.setMinHeight(dp(52));
        view.setClickable(true);
        view.setFocusable(true);
        view.setContentDescription(name);
        return view;
    }
    private GradientDrawable shape(int fill, int radius, int edge, int borderDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(dp(borderDp), edge);
        return drawable;
    }
    private void add(LinearLayout parent, View child, int width, int bottomDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, -2);
        lp.bottomMargin = dp(bottomDp);
        parent.addView(child, lp);
    }
    private int dp(int value) { return (int) (getResources().getDisplayMetrics().density * value + .5f); }
    private void toast(String text) { Toast.makeText(this, text, Toast.LENGTH_LONG).show(); }
}
