package com.rakim.event47;

/** A single user's persisted event; timestamps use Unix epoch milliseconds. */
public final class EventRecord {
    public long id;
    public long ownerId;
    public String title;
    public long whenMillis;
    public String location;
    public String notes;
    public String phone;
    public boolean reminderEnabled;
    public long remindedAt;

    public EventRecord() { }
}
