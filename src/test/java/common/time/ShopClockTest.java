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

class ShopClockTest {

    @Test
    void shouldReturnCurrentTimeInWarsawZone() {
        ZonedDateTime now = ShopClock.now();

        assertThat(now.getZone()).isEqualTo(ShopClock.DEFAULT_ZONE);
        assertThat(now.getZone().getId()).isEqualTo("Europe/Warsaw");
    }

    @Test
    void shouldReturnTimeCloseToCurrentInstant() {
        ZonedDateTime before = ZonedDateTime.now(ShopClock.DEFAULT_ZONE).minusSeconds(1);
        ZonedDateTime result = ShopClock.now();
        ZonedDateTime after = ZonedDateTime.now(ShopClock.DEFAULT_ZONE).plusSeconds(1);

        assertThat(result).isAfter(before).isBefore(after);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Europe/London", "America/New_York", "Asia/Tokyo", "UTC"})
    void shouldReturnTimeInRequestedZone(String zoneId) {
        ZoneId zone = ZoneId.of(zoneId);
        ZonedDateTime result = ShopClock.nowIn(zone);

        assertThat(result.getZone()).isEqualTo(zone);
    }

    @Test
    void shouldPreserveSameInstantWhenConvertingZones() {
        ZonedDateTime warsaw = ShopClock.now();
        ZonedDateTime tokyo = ShopClock.convertTo(warsaw, ZoneId.of("Asia/Tokyo"));

        assertThat(warsaw.toInstant()).isEqualTo(tokyo.toInstant());
        assertThat(warsaw.getZone()).isNotEqualTo(tokyo.getZone());
    }

    @Test
    void shouldConvertToInstant() {
        ZonedDateTime now = ShopClock.now();
        Instant instant = ShopClock.toInstant(now);
        Instant systemInstant = Instant.now();

        assertThat(instant).isCloseTo(systemInstant, within(2, ChronoUnit.SECONDS));
    }

    @Test
    void shouldHaveWarsawAsDefaultZone() {
        assertThat(ShopClock.DEFAULT_ZONE).isEqualTo(ZoneId.of("Europe/Warsaw"));
    }

    @Test
    void shouldNotMixUpLocalTimes() {
        ZonedDateTime warsaw = ShopClock.now();
        ZonedDateTime tokyo = ShopClock.nowIn(ZoneId.of("Asia/Tokyo"));

        assertThat(warsaw.toInstant())
                .isCloseTo(tokyo.toInstant(), within(2, ChronoUnit.SECONDS));

        assertThat(warsaw.getZone()).isNotEqualTo(tokyo.getZone());
    }
}
