package com.rakim.event47;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Local, persistent SQLite storage for accounts and per-account events. */
public final class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "event47.sqlite";
    private static final int DB_VERSION = 1;
    private static final int HASH_ITERATIONS = 120_000;
    private static final int HASH_BYTES = 32;
    private final SecureRandom random = new SecureRandom();

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        setWriteAheadLoggingEnabled(true);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE COLLATE NOCASE, salt TEXT NOT NULL, pass_hash TEXT NOT NULL)");
        db.execSQL("CREATE TABLE events (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "owner_id INTEGER NOT NULL, title TEXT NOT NULL, when_ms INTEGER NOT NULL, " +
                "location TEXT NOT NULL DEFAULT '', notes TEXT NOT NULL DEFAULT '', " +
                "sms_phone TEXT NOT NULL DEFAULT '', reminder INTEGER NOT NULL DEFAULT 0, " +
                "reminded_at INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX idx_events_owner_date ON events(owner_id, when_ms)");
    }

    @Override public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the initial schema; later versions will use migrations.
    }

    public long createUser(String username, String password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        ContentValues values = new ContentValues();
        values.put("username", username.trim());
        values.put("salt", Base64.encodeToString(salt, Base64.NO_WRAP));
        values.put("pass_hash", Base64.encodeToString(deriveHash(password, salt), Base64.NO_WRAP));
        return getWritableDatabase().insertWithOnConflict("users", null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    /** Returns user ID only if a constant-time hash comparison succeeds. */
    public long authenticate(String username, String password) {
        try (Cursor cursor = getReadableDatabase().query("users",
                new String[]{"id", "salt", "pass_hash"},
                "username = ? COLLATE NOCASE", new String[]{username.trim()},
                null, null, null)) {
            if (!cursor.moveToFirst()) return -1;
            byte[] salt = Base64.decode(cursor.getString(1), Base64.DEFAULT);
            byte[] expected = Base64.decode(cursor.getString(2), Base64.DEFAULT);
            byte[] actual = deriveHash(password, salt);
            return MessageDigest.isEqual(expected, actual) ? cursor.getLong(0) : -1;
        }
    }

    private byte[] deriveHash(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, HASH_ITERATIONS, HASH_BYTES * 8);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash account credentials", e);
        } finally {
            spec.clearPassword();
        }
    }

    public long saveEvent(EventRecord e) {
        ContentValues v = new ContentValues();
        v.put("owner_id", e.ownerId);
        v.put("title", e.title);
        v.put("when_ms", e.whenMillis);
        v.put("location", e.location);
        v.put("notes", e.notes);
        v.put("sms_phone", e.phone);
        v.put("reminder", e.reminderEnabled ? 1 : 0);
        // Editing an event makes it eligible for a new reminder.
        v.put("reminded_at", 0);
        if (e.id == 0) return getWritableDatabase().insert("events", null, v);
        return getWritableDatabase().update("events", v,
                "id=? AND owner_id=?", new String[]{String.valueOf(e.id), String.valueOf(e.ownerId)});
    }

    public boolean deleteEvent(long eventId, long ownerId) {
        return getWritableDatabase().delete("events", "id=? AND owner_id=?",
                new String[]{String.valueOf(eventId), String.valueOf(ownerId)}) > 0;
    }

    public void markReminded(long eventId, long ownerId) {
        ContentValues v = new ContentValues();
        v.put("reminded_at", System.currentTimeMillis());
        getWritableDatabase().update("events", v, "id=? AND owner_id=? AND reminded_at=0",
                new String[]{String.valueOf(eventId), String.valueOf(ownerId)});
    }

    public List<EventRecord> getEvents(long userId) {
        ArrayList<EventRecord> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("events", null, "owner_id=?",
                new String[]{String.valueOf(userId)}, null, null, "when_ms ASC")) {
            while (c.moveToNext()) {
                EventRecord e = new EventRecord();
                e.id = c.getLong(c.getColumnIndexOrThrow("id"));
                e.ownerId = c.getLong(c.getColumnIndexOrThrow("owner_id"));
                e.title = c.getString(c.getColumnIndexOrThrow("title"));
                e.whenMillis = c.getLong(c.getColumnIndexOrThrow("when_ms"));
                e.location = c.getString(c.getColumnIndexOrThrow("location"));
                e.notes = c.getString(c.getColumnIndexOrThrow("notes"));
                e.phone = c.getString(c.getColumnIndexOrThrow("sms_phone"));
                e.reminderEnabled = c.getInt(c.getColumnIndexOrThrow("reminder")) != 0;
                e.remindedAt = c.getLong(c.getColumnIndexOrThrow("reminded_at"));
                list.add(e);
            }
        }
        return list;
    }
}
