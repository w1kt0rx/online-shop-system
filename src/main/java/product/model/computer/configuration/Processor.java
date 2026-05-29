package product.model.computer.configuration;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum Processor {
    INTEL_I5("Intel Core i5", new BigDecimal("500")),
    INTEL_I7("Intel Core i7", new BigDecimal("800")),
    INTEL_I9("Intel Core i9", new BigDecimal("1200")),
    AMD_RYZEN_5("AMD Ryzen 5", new BigDecimal("550")),
    AMD_RYZEN_7("AMD Ryzen 5", new BigDecimal("850"));
    private final String description;
    private final BigDecimal price;

    Processor(String description, BigDecimal price) {
        this.description = description;
        this.price = price;
    }
}
