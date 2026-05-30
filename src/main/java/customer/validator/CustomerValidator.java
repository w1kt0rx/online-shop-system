package customer.validator;

import exception.InvalidProductException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomerValidator {

    public static void validate(Long id, String name) {
        List<String> errors = new ArrayList<>();

        if (id == null || id < 0) {
            errors.add("Customer id cannot be null or negative");
        }
        if (name == null || name.isBlank()) {
            errors.add("Customer name cannot be blank");
        }

        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Customer name cannot be blank");
        }
    }
}
