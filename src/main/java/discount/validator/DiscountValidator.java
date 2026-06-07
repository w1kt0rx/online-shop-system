package discount.validator;

import discount.dto.CreateDiscountRequest;
import exception.InvalidProductException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DiscountValidator {
    public static void validate(CreateDiscountRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.code() == null || request.code().isBlank()) {
            errors.add("Discount code cannot be blank");
        }
        if (request.value() == null || request.value().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Discount value must be positive");
        }
        if (request.validFrom() == null || request.validTo() == null) {
            errors.add("Validity dates cannot be null");
        }
        if (request.validFrom() != null && request.validTo() != null && request.validTo().isBefore(request.validFrom())) {
            errors.add("validTo must be after validFrom");
        }

        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }
}
