package com.rakim.event47;

/** Standalone JVM smoke tests: javac -d out EventValidator.java EventValidatorSmokeTest.java
 * and java -cp out com.rakim.event47.EventValidatorSmokeTest
 */
public final class EventValidatorSmokeTest {
    public static void main(String[] args) {
        assertCheck(!EventValidator.hasTitle("  "), "blank title rejected");
        assertCheck(EventValidator.hasTitle(" Meeting "), "trimmed title accepted");
        assertCheck(EventValidator.hasReminderDestination(false, ""), "no reminder no number");
        assertCheck(!EventValidator.hasReminderDestination(true, "abc"), "bad SMS number rejected");
        assertCheck(EventValidator.hasReminderDestination(true, "(404) 555-0123"), "formatted number accepted");
        System.out.println("PASS: 5 event validation checks");
    }
    private static void assertCheck(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
    }
}
