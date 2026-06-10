package common.time;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * <p>Solves time zone issues by consistently using
 * ZonedDateTime instead of java.time.LocalDateTime.
 * LocalDateTime does not store time zone information —
 * the same "2024-01-15T14:30" can represent completely different moments
 * in Warsaw and New York.ZonedDateTime always knows its time zone.</p>
 *
 * <p>The default time zone is Europe/Warsaw. Even when the application
 * runs on a server in a different time zone, orders will still receive
 * Polish timestamps.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShopClock {

    /** Default store time zone — Europe/Warsaw (UTC+1/UTC+2). */
    public static ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Warsaw");

    /**
     * Returns the current time in the store's default time zone (Europe/Warsaw).
     *
     * @return the current ZonedDateTime in the Europe/Warsaw time zone
     */
    public static ZonedDateTime now() {
        return ZonedDateTime.now(DEFAULT_ZONE);
    }

    /**
     * Returns the current time in the specified time zone.
     * Useful when a customer is in a different time zone and wants to see the local time.
     *
     * @param zone the target time zone
     * @return the current ZonedDateTime in the specified time zone
     */
    public static ZonedDateTime nowIn(ZoneId zone) {
        return ZonedDateTime.now(zone);
    }

    /**
     * Converts a date and time to the target time zone without changing the actual moment in time.
     * The physical instant remains the same — only its representation changes.
     *
     * @param dateTime the source date and time
     * @param target the target time zone
     * @return the same moment represented in a different time zone
     */
    public static ZonedDateTime convertTo(ZonedDateTime dateTime, ZoneId target) {
        return dateTime.withZoneSameInstant(target);
    }

    /**
     * Converts a date and time to an Instant — a point on the UTC timeline.
     * Useful for comparisons and time-zone-independent persistence.
     *
     * @param dateTime the date and time to convert
     * @return the corresponding Instant
     */
    public static Instant toInstant(ZonedDateTime dateTime) {
        return dateTime.toInstant();
    }

}
