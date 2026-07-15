package product.model.smartphone.configuration;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Color {
    BLACK("Black", BigDecimal.ZERO),
    WHITE("White", new BigDecimal("50")),
    BLUE("Blue", new BigDecimal("75")),
    RED("Red", new BigDecimal("80")),
    GOLD("Gold", new BigDecimal("100"));

    private final String description;
    private final BigDecimal price;
}
