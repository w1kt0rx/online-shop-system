package discount.model;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@ToString
public class Discount {
    private final Long id;
    private final String code;
    private final String description;
    private final DiscountType type;
    private final BigDecimal value;
    private final BigDecimal minOrderValue;
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;
    private boolean active;

    public Discount(Long id, String code, String description, DiscountType type, BigDecimal value,
                    BigDecimal minOrderValue, LocalDateTime validFrom, LocalDateTime validTo) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.type = type;
        this.value = value;
        this.minOrderValue = minOrderValue;
        this.validFrom = validFrom;
        this.validTo = validTo;
        active = true;
    }

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return active && !now.isBefore(validFrom) && !now.isAfter(validTo);
    }

    public void deActivate() {
        active = false;
    }
}
