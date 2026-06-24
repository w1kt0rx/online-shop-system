package common.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.temporal.ChronoUnit;

class TimeUtilsTest {

    @Test
    void shouldReturnCurrentTimeInWarsawZone() {
        ZonedDateTime now = TimeUtils.now();

        assertThat(now.getZone()).isEqualTo(TimeUtils.DEFAULT_ZONE);
        assertThat(now.getZone().getId()).isEqualTo("Europe/Warsaw");
    }

    @Test
    void shouldReturnTimeCloseToCurrentInstant() {
        ZonedDateTime before = ZonedDateTime.now(TimeUtils.DEFAULT_ZONE).minusSeconds(1);
        ZonedDateTime result = TimeUtils.now();
        ZonedDateTime after = ZonedDateTime.now(TimeUtils.DEFAULT_ZONE).plusSeconds(1);

        assertThat(result).isAfter(before).isBefore(after);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Europe/London", "America/New_York", "Asia/Tokyo", "UTC"})
    void shouldReturnTimeInRequestedZone(String zoneId) {
        ZoneId zone = ZoneId.of(zoneId);
        ZonedDateTime result = TimeUtils.nowIn(zone);

        assertThat(result.getZone()).isEqualTo(zone);
    }

    @Test
    void shouldPreserveSameInstantWhenConvertingZones() {
        ZonedDateTime warsaw = TimeUtils.now();
        ZonedDateTime tokyo = TimeUtils.convertTo(warsaw, ZoneId.of("Asia/Tokyo"));

        assertThat(warsaw.toInstant()).isEqualTo(tokyo.toInstant());
        assertThat(warsaw.getZone()).isNotEqualTo(tokyo.getZone());
    }

    @Test
    void shouldConvertToInstant() {
        ZonedDateTime now = TimeUtils.now();
        Instant instant = TimeUtils.toInstant(now);
        Instant systemInstant = Instant.now();

        assertThat(instant).isCloseTo(systemInstant, within(2, ChronoUnit.SECONDS));
    }

    @Test
    void shouldHaveWarsawAsDefaultZone() {
        assertThat(TimeUtils.DEFAULT_ZONE).isEqualTo(ZoneId.of("Europe/Warsaw"));
    }

    @Test
    void shouldNotMixUpLocalTimes() {
        ZonedDateTime warsaw = TimeUtils.now();
        ZonedDateTime tokyo = TimeUtils.nowIn(ZoneId.of("Asia/Tokyo"));

        assertThat(warsaw.toInstant())
                .isCloseTo(tokyo.toInstant(), within(2, ChronoUnit.SECONDS));

        assertThat(warsaw.getZone()).isNotEqualTo(tokyo.getZone());
    }
}
