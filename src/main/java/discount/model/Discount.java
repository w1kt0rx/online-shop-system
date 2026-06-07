package discount.model;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a discount that can be applied to an order.
 * A discount is active when it is both explicitly enabled active
 * and within its validity window validFrom to validTo.
 * The monetary effect is determined by DiscountType and the value field.
 */
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

    /**
     * Creates a new, active discount.
     *
     * @param id            unique identifier
     * @param code          unique alphanumeric code customers use at checkout
     * @param description   human-readable description shown in the UI
     * @param type          calculation method DiscountType.PERCENTAGE or
     *                      DiscountType.FIXED_AMOUNT
     * @param value         numeric value interpreted according to type
     *                      10 = 10% or 10 PLN
     * @param minOrderValue optional minimum order total required to apply this discount;
     *                      ull means no minimum
     * @param validFrom     start of the validity window
     * @param validTo       end of the validity window
     */
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

    /**
     * Returns true if this discount is currently usable — i.e., it is active
     * and the current date/time falls within validFrom, validTo.
     *
     * @return true when the discount may be applied
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return active && !now.isBefore(validFrom) && !now.isAfter(validTo);
    }

    /**
     * Permanently deactivates this discount.
     * Once deactivated, isValid() will return false regardless of dates.
     */
    public void deActivate() {
        active = false;
    }
}
