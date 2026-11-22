package com.rakim.event47;

/** Pure-Java event input checks, independent of Android UI widgets. */
public final class EventValidator {
    private EventValidator() {}

    public static boolean hasTitle(String title) {
        return title != null && !title.trim().isEmpty();
    }

    public static boolean hasReminderDestination(boolean reminderEnabled, String phone) {
        if (!reminderEnabled) return true;
        if (phone == null) return false;
        return phone.replaceAll("[^0-9+]", "").length() >= 4;
    }
}
