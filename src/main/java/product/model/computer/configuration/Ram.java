package product.model.computer.configuration;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum Ram {
    RAM_8GB(8, new BigDecimal("200")),
    RAM_16GB(16, new BigDecimal("400")),
    RAM_32GB(32, new BigDecimal("800")),
    RAM_64(64, new BigDecimal("1500"));

    private final int capacity;
    private final BigDecimal price;

    Ram(int capacity, BigDecimal price) {
        this.capacity = capacity;
        this.price = price;
    }
}
