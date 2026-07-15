package product.validator;

import exception.InvalidProductException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductValidator {

    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Name cannot be blank");
        }
    }

    public static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProductException("Price cannot be negative");
        }
    }

    public static void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new InvalidProductException("Quantity cannot be negative or null");
        }
    }
}
