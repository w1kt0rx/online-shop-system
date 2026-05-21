package entity.smartphone;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum Color {
    BLACK("Black", BigDecimal.ZERO),
    WHITE("White", new BigDecimal("50")),
    BLUE("Blue", new BigDecimal("75")),
    RED("Red", new BigDecimal("80")),
    GOLD("Gold", new BigDecimal("100"));

    private final String description;
    private final BigDecimal price;

    Color(String description, BigDecimal price) {
        this.description = description;
        this.price = price;
    }
}
