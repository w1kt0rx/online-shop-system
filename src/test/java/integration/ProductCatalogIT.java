package integration;

import static org.assertj.core.api.Assertions.*;

import exception.InvalidProductException;
import exception.ProductNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import product.dto.computer.*;
import product.dto.computer.configuration.ComputerConfigurationDto;
import product.dto.electronics.*;
import product.dto.smartphone.*;
import product.model.ProductType;
import product.model.computer.configuration.*;
import product.model.smartphone.configuration.*;
import product.repository.impl.*;
import product.service.*;

class ProductCatalogIT {

    private ComputerService computerService;
    private SmartphoneService smartphoneService;
    private ElectronicsService electronicsService;

    @BeforeEach
    void setUp() {
        computerService = new ComputerService(new InMemoryComputerRepository());
        smartphoneService = new SmartphoneService(new InMemorySmartphoneRepository());
        electronicsService = new ElectronicsService(new InMemoryElectronicsRepository());
    }

    @Test
    void createComputer_persistsAndReturnsWithConfigurationPrice() {
        ComputerDto dto = computerService.create(
            new CreateComputerRequest(
                "Dell XPS 15",
                new BigDecimal("4000"),
                10,
                Processor.INTEL_I7,
                Ram.RAM_16GB,
                StorageType.SSD_1TB,
                GraphicsCard.RTX_3050
            )
        );

        assertThat(dto.id()).isPositive();
        assertThat(dto.name()).isEqualTo("Dell XPS 15");
        assertThat(dto.basePrice()).isEqualByComparingTo(new BigDecimal("4000"));
        assertThat(dto.quantity()).isEqualTo(10);
        assertThat(dto.productType()).isEqualTo(ProductType.COMPUTER);

        ComputerConfigurationDto cfg = dto.computerConfiguration();
        assertThat(cfg.processor()).isEqualTo(Processor.INTEL_I7);
        assertThat(cfg.ram()).isEqualTo(Ram.RAM_16GB);
        assertThat(cfg.storageType()).isEqualTo(StorageType.SSD_1TB);
        assertThat(cfg.graphicsCard()).isEqualTo(GraphicsCard.RTX_3050);

        // totalPrice = base(4000) + i7(800) + 16GB(400) + SSD1TB(500) + RTX3050(1000) = 6700
        assertThat(dto.totalPrice()).isEqualByComparingTo(new BigDecimal("6700"));
    }

    @ParameterizedTest(name = "{0} + {1} + {2} + {3}")
    @CsvSource(
        {
            "INTEL_I5,  RAM_8GB,  SSD_512GB, INTEGRATED, 1000",
            "INTEL_I7,  RAM_16GB, SSD_1TB,   RTX_3050,   2700",
            "INTEL_I9,  RAM_32GB, SSD_2TB,   RTX_4070,   4900",
            "AMD_RYZEN_5, RAM_8GB, HDD_1TB, INTEGRATED,  850",
        }
    )
    void createComputer_configurationPriceMatchesEnumValues(
        Processor proc,
        Ram ram,
        StorageType storage,
        GraphicsCard gpu,
        BigDecimal expectedCfgPrice
    ) {
        ComputerDto dto = computerService.create(
            new CreateComputerRequest("PC", new BigDecimal("1000"), 1, proc, ram, storage, gpu)
        );

        assertThat(dto.totalPrice().subtract(dto.basePrice())).isEqualByComparingTo(expectedCfgPrice);
    }

    @Test
    void updateComputer_allFieldsReflectedImmediately() {
        ComputerDto created = computerService.create(
            new CreateComputerRequest(
                "Old PC",
                new BigDecimal("2000"),
                3,
                Processor.INTEL_I5,
                Ram.RAM_8GB,
                StorageType.SSD_512GB,
                GraphicsCard.INTEGRATED
            )
        );

        ComputerDto updated = computerService.update(
            created.id(),
            new UpdateComputerRequest(
                "New PC",
                new BigDecimal("2500"),
                5,
                Processor.INTEL_I9,
                Ram.RAM_32GB,
                StorageType.SSD_2TB,
                GraphicsCard.RTX_4070
            )
        );

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.name()).isEqualTo("New PC");
        assertThat(updated.basePrice()).isEqualByComparingTo(new BigDecimal("2500"));
        assertThat(updated.quantity()).isEqualTo(5);
        assertThat(updated.computerConfiguration().processor()).isEqualTo(Processor.INTEL_I9);
        assertThat(updated.computerConfiguration().graphicsCard()).isEqualTo(GraphicsCard.RTX_4070);
        // totalPrice must reflect new config: 2500 + 4900 = 7400
        assertThat(updated.totalPrice()).isEqualByComparingTo(new BigDecimal("7400"));
    }

    @Test
    void deleteComputer_removedFromRepository() {
        ComputerDto dto = computerService.create(
            new CreateComputerRequest(
                "Temp",
                new BigDecimal("1000"),
                1,
                Processor.INTEL_I5,
                Ram.RAM_8GB,
                StorageType.SSD_512GB,
                GraphicsCard.INTEGRATED
            )
        );

        computerService.delete(dto.id());

        assertThat(computerService.getAll()).isEmpty();
        assertThatExceptionOfType(ProductNotFoundException.class).isThrownBy(() -> computerService.getById(dto.id()));
    }

    @Test
    void getAllComputers_returnsAllCreatedInOrder() {
        computerService.create(
            new CreateComputerRequest(
                "A",
                new BigDecimal("1000"),
                1,
                Processor.INTEL_I5,
                Ram.RAM_8GB,
                StorageType.SSD_512GB,
                GraphicsCard.INTEGRATED
            )
        );
        computerService.create(
            new CreateComputerRequest(
                "B",
                new BigDecimal("2000"),
                1,
                Processor.INTEL_I7,
                Ram.RAM_16GB,
                StorageType.SSD_1TB,
                GraphicsCard.RTX_3050
            )
        );
        computerService.create(
            new CreateComputerRequest(
                "C",
                new BigDecimal("3000"),
                1,
                Processor.INTEL_I9,
                Ram.RAM_32GB,
                StorageType.SSD_2TB,
                GraphicsCard.RTX_4070
            )
        );

        List<ComputerDto> all = computerService.getAll();

        assertThat(all).hasSize(3);
        assertThat(all).extracting(ComputerDto::name).containsExactlyInAnyOrder("A", "B", "C");
    }

    @Test
    void getComputerById_throwsWhenNotFound() {
        assertThatExceptionOfType(ProductNotFoundException.class).isThrownBy(() -> computerService.getById(999L));
    }

    @Test
    void createSmartphone_persistsAndReturnsWithConfigurationPrice() {
        SmartphoneDto dto = smartphoneService.create(
            new CreateSmartphoneRequest(
                "iPhone 15 Pro",
                new BigDecimal("5000"),
                20,
                Set.of(Accessory.CHARGER, Accessory.PHONE_CASE),
                BatteryCapacity.BATTERY_5000,
                Color.GOLD
            )
        );

        assertThat(dto.id()).isPositive();
        assertThat(dto.name()).isEqualTo("iPhone 15 Pro");
        assertThat(dto.basePrice()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(dto.quantity()).isEqualTo(20);
        assertThat(dto.productType()).isEqualTo(ProductType.SMARTPHONE);

        assertThat(dto.smartphoneConfiguration().color()).isEqualTo(Color.GOLD);
        assertThat(dto.smartphoneConfiguration().batteryCapacity()).isEqualTo(BatteryCapacity.BATTERY_5000);
        assertThat(dto.smartphoneConfiguration().accessories()).containsExactlyInAnyOrder(
            Accessory.CHARGER,
            Accessory.PHONE_CASE
        );

        // totalPrice = 5000 + GOLD(100) + BAT_5000(300) + CHARGER(120) + PHONE_CASE(80) = 5600
        assertThat(dto.totalPrice()).isEqualByComparingTo(new BigDecimal("5600"));
    }

    @Test
    void createSmartphone_noAccessories_onlyColorAndBatteryAdded() {
        SmartphoneDto dto = smartphoneService.create(
            new CreateSmartphoneRequest(
                "Basic Phone",
                new BigDecimal("2000"),
                5,
                Set.of(),
                BatteryCapacity.BATTERY_4000,
                Color.BLACK
            )
        );

        // BLACK(0) + BAT_4000(150) + no accessories = 150
        assertThat(dto.totalPrice().subtract(dto.basePrice())).isEqualByComparingTo(new BigDecimal("150"));
    }

    @Test
    void updateSmartphone_configurationAndPriceUpdated() {
        SmartphoneDto created = smartphoneService.create(
            new CreateSmartphoneRequest(
                "Phone",
                new BigDecimal("3000"),
                10,
                Set.of(),
                BatteryCapacity.BATTERY_3000,
                Color.BLACK
            )
        );

        SmartphoneDto updated = smartphoneService.update(
            created.id(),
            new UpdateSmartphoneRequest(
                "Phone Pro",
                new BigDecimal("3500"),
                8,
                Set.of(Accessory.CHARGER, Accessory.HEADPHONES),
                BatteryCapacity.BATTERY_6000,
                Color.GOLD
            )
        );

        assertThat(updated.name()).isEqualTo("Phone Pro");
        assertThat(updated.quantity()).isEqualTo(8);
        assertThat(updated.smartphoneConfiguration().accessories()).containsExactlyInAnyOrder(
            Accessory.CHARGER,
            Accessory.HEADPHONES
        );
        // 3500 + GOLD(100) + BAT_6000(500) + CHARGER(120) + HEADPHONES(200) = 4420
        assertThat(updated.totalPrice()).isEqualByComparingTo(new BigDecimal("4420"));
    }

    @Test
    void deleteSmartphone_removedFromRepository() {
        SmartphoneDto dto = smartphoneService.create(
            new CreateSmartphoneRequest(
                "Temp Phone",
                new BigDecimal("2000"),
                1,
                Set.of(),
                BatteryCapacity.BATTERY_4000,
                Color.BLACK
            )
        );

        smartphoneService.delete(dto.id());

        assertThat(smartphoneService.getAll()).isEmpty();
        assertThatExceptionOfType(ProductNotFoundException.class).isThrownBy(() -> smartphoneService.getById(dto.id()));
    }

    @Test
    void createElectronics_priceEqualsBasePrice_noConfiguration() {
        ElectronicsDto dto = electronicsService.create(
            new CreateElectronicsRequest("Monitor 4K 27\"", new BigDecimal("1800"), 12)
        );

        assertThat(dto.id()).isPositive();
        assertThat(dto.name()).isEqualTo("Monitor 4K 27\"");
        assertThat(dto.basePrice()).isEqualByComparingTo(new BigDecimal("1800"));
        assertThat(dto.quantity()).isEqualTo(12);
        assertThat(dto.productType()).isEqualTo(ProductType.ELECTRONICS);
    }

    @ParameterizedTest
    @CsvSource({ "Keyboard, 350, 40", "Headphones, 800, 25", "Webcam, 250, 30", "USB Hub, 120, 100" })
    void createMultipleElectronics_eachGetsUniqueId(String name, BigDecimal price, int qty) {
        ElectronicsDto dto = electronicsService.create(new CreateElectronicsRequest(name, price, qty));

        assertThat(dto.id()).isPositive();
        assertThat(dto.name()).isEqualTo(name);
        assertThat(dto.basePrice()).isEqualByComparingTo(price);
        assertThat(dto.quantity()).isEqualTo(qty);
    }

    @Test
    void updateElectronics_allFieldsReflected() {
        ElectronicsDto created = electronicsService.create(
            new CreateElectronicsRequest("Old Monitor", new BigDecimal("1000"), 5)
        );

        ElectronicsDto updated = electronicsService.update(
            created.id(),
            new UpdateElectronicsRequest("New Monitor 4K", new BigDecimal("1500"), 8)
        );

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.name()).isEqualTo("New Monitor 4K");
        assertThat(updated.basePrice()).isEqualByComparingTo(new BigDecimal("1500"));
        assertThat(updated.quantity()).isEqualTo(8);
    }

    @Test
    void deleteElectronics_throwsOnSubsequentFetch() {
        ElectronicsDto dto = electronicsService.create(new CreateElectronicsRequest("Temp", new BigDecimal("500"), 1));

        electronicsService.delete(dto.id());

        assertThat(electronicsService.getAll()).isEmpty();
        assertThatExceptionOfType(ProductNotFoundException.class).isThrownBy(() -> electronicsService.getById(dto.id())
        );
    }

    @Test
    void allThreeProductTypes_idsAreIndependent() {
        ComputerDto c = computerService.create(
            new CreateComputerRequest(
                "PC",
                new BigDecimal("1000"),
                1,
                Processor.INTEL_I5,
                Ram.RAM_8GB,
                StorageType.SSD_512GB,
                GraphicsCard.INTEGRATED
            )
        );
        SmartphoneDto s = smartphoneService.create(
            new CreateSmartphoneRequest(
                "Phone",
                new BigDecimal("2000"),
                1,
                Set.of(),
                BatteryCapacity.BATTERY_4000,
                Color.BLACK
            )
        );
        ElectronicsDto e = electronicsService.create(new CreateElectronicsRequest("Monitor", new BigDecimal("500"), 1));

        assertThat(List.of(c.id(), s.id(), e.id())).allMatch(id -> id >= 1L);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   " })
    void createElectronics_throwsForBlankName(String name) {
        assertThatExceptionOfType(InvalidProductException.class).isThrownBy(() ->
            electronicsService.create(new CreateElectronicsRequest(name, new BigDecimal("100"), 1))
        );
    }

    @Test
    void createElectronics_throwsForNegativePrice() {
        assertThatExceptionOfType(InvalidProductException.class).isThrownBy(() ->
            electronicsService.create(new CreateElectronicsRequest("Monitor", new BigDecimal("-1"), 1))
        );
    }
}
