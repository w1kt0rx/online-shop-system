package product.model.smartphone.configuration;


import exception.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SmartphoneConfigurationTest {

    private SmartphoneConfiguration config;

    @BeforeEach
    void setUp() {
        config = new SmartphoneConfiguration();
    }

    @Test
    void shouldHaveCorrectDefaults() {
        assertThat(config.getColor()).isEqualTo(Color.BLACK);
        assertThat(config.getBatteryCapacity()).isEqualTo(BatteryCapacity.BATTERY_4000);
        assertThat(config.getAccessories()).isEmpty();
    }

    @Test
    void shouldConfigureAllFields() {
        config.configure(Color.GOLD, BatteryCapacity.BATTERY_6000, Set.of(Accessory.CHARGER, Accessory.PHONE_CASE));

        assertThat(config.getColor()).isEqualTo(Color.GOLD);
        assertThat(config.getBatteryCapacity()).isEqualTo(BatteryCapacity.BATTERY_6000);
        assertThat(config.getAccessories()).containsExactlyInAnyOrder(Accessory.CHARGER, Accessory.PHONE_CASE);
    }

    @ParameterizedTest
    @MethodSource("provideAllColors")
    void shouldUpdateColorForAllValues(Color color) {
        config.updateColor(color);
        assertThat(config.getColor()).isEqualTo(color);
    }

    @ParameterizedTest
    @MethodSource("provideAllBatteries")
    void shouldUpdateBatteryCapacityForAllValues(BatteryCapacity battery) {
        config.updateBatteryCapacity(battery);
        assertThat(config.getBatteryCapacity()).isEqualTo(battery);
    }

    @Test
    void shouldAddAndRemoveAccessory() {
        config.addAccessory(Accessory.CHARGER);
        assertThat(config.getAccessories()).contains(Accessory.CHARGER);

        config.removeAccessory(Accessory.CHARGER);
        assertThat(config.getAccessories()).doesNotContain(Accessory.CHARGER);
    }

    @Test
    void shouldNotDuplicateAccessory() {
        config.addAccessory(Accessory.CHARGER);
        config.addAccessory(Accessory.CHARGER);
        assertThat(config.getAccessories()).hasSize(1);
    }

    @Test
    void shouldClearAllAccessories() {
        config.addAccessory(Accessory.CHARGER);
        config.addAccessory(Accessory.HEADPHONES);
        config.clearAccessories();

        assertThat(config.getAccessories()).isEmpty();
    }

    @Test
    void shouldCalculateDefaultAdditionalPrice() {
        // BLACK(0) + BATTERY_4000(150) = 150
        assertThat(config.calculateAdditionalPrice()).isEqualByComparingTo(new BigDecimal("150"));
    }

    @Test
    void shouldCalculateAdditionalPriceWithAccessories() {
        config.configure(Color.GOLD, BatteryCapacity.BATTERY_5000, Set.of(Accessory.CHARGER, Accessory.HEADPHONES));
        // GOLD(100) + BATTERY_5000(300) + CHARGER(120) + HEADPHONES(200) = 720
        assertThat(config.calculateAdditionalPrice()).isEqualByComparingTo(new BigDecimal("720"));
    }

    @Test
    void shouldThrowWhenAddingNullAccessory() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> config.addAccessory(null));
    }

    @Test
    void shouldThrowWhenUpdatingColorToNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> config.updateColor(null));
    }

    @Test
    void shouldThrowWhenUpdatingBatteryToNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> config.updateBatteryCapacity(null));
    }

    private static Stream<Arguments> provideAllColors() {
        return Stream.of(Color.values()).map(Arguments::of);
    }

    private static Stream<Arguments> provideAllBatteries() {
        return Stream.of(BatteryCapacity.values()).map(Arguments::of);
    }
}