package product.validator;

import exception.InvalidProductException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductValidator {

    public static void validate(Long id, String name, BigDecimal price, Integer quantity) {
        List<String> errors = new ArrayList<>();

        validateId(id, errors);
        validateName(name, errors);
        validatePrice(price, errors);
        validateQuantity(quantity, errors);

        if(!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    private static void validateId(Long id, List<String> errors) {
        if (id == null || id < 0) {
            errors.add("Id cannot be null or negative");
        }
    }

    private static void validateName(String name, List<String> errors) {
        if (name == null || name.isBlank()) {
            errors.add("Name cannot be blank");
        }
    }

    private static void validatePrice(BigDecimal price, List<String> errors) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Price cannot be negative");
        }
    }

    private static void validateQuantity(Integer quantity, List<String> errors) {
        if (quantity < 0) {
            errors.add("Quantity cannot be negative");
        }
    }
}
