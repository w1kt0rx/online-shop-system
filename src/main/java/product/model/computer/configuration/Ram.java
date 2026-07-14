package product.model.computer.configuration;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Ram {
    RAM_8GB(8, new BigDecimal("200")),
    RAM_16GB(16, new BigDecimal("400")),
    RAM_32GB(32, new BigDecimal("800")),
    RAM_64(64, new BigDecimal("1500"));

    private final int capacity;
    private final BigDecimal price;
}
