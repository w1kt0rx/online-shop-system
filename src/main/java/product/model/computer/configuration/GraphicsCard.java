package product.model.computer.configuration;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum GraphicsCard {
    INTEGRATED("Integrated", BigDecimal.ZERO),
    GTX_1650("GTX 1650", new BigDecimal("600")),
    RTX_3050("RTX 3050", new BigDecimal("1000")),
    RTX_4070("RTX 4070", new BigDecimal("2000"));

    private final String description;
    private final BigDecimal price;
}
