package discount.validator;

import discount.dto.CreateDiscountRequest;
import discount.model.DiscountType;
import exception.InvalidProductException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DiscountValidator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal MAX_PERCENTAGE_DISCOUNT = BigDecimal.valueOf(100);
    private static final BigDecimal MAX_FIXED_AMOUNT_DISCOUNT = BigDecimal.valueOf(1000);

    public static void validate(CreateDiscountRequest request) {
        List<String> errors = new ArrayList<>();

        validateCode(request, errors);
        validateType(request, errors);
        validateValue(request, errors);
        validateDates(request, errors);
        validateTypeSpecificValue(request, errors);

        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    private static void validateCode(CreateDiscountRequest request, List<String> errors) {
        if (Optional.ofNullable(request.code())
                .filter(code -> !code.isBlank())
                .isEmpty()) {
            errors.add("Discount code cannot be blank");
        }
    }

    private static void validateType(CreateDiscountRequest request, List<String> errors) {
        if (request.type() == null) {
            errors.add("Discount type cannot be null");
        }
    }

    private static void validateValue(CreateDiscountRequest request, List<String> errors) {
        if (Optional.ofNullable(request.value())
                .filter(value -> value.compareTo(ZERO) > 0)
                .isEmpty()) {
            errors.add("Discount value must be positive");
        }
    }

    private static void validateDates(CreateDiscountRequest request, List<String> errors) {
        if (request.validFrom() == null || request.validTo() == null) {
            errors.add("Validity dates cannot be null");
            return;
        }

        if (request.validTo().isBefore(request.validFrom())) {
            errors.add("validTo must be after validFrom");
        }
    }

    private static void validateTypeSpecificValue(CreateDiscountRequest request, List<String> errors) {
        if (request.type() == null || request.value() == null) {
            return;
        }

        if (request.type() == DiscountType.PERCENTAGE
                && request.value().compareTo(MAX_PERCENTAGE_DISCOUNT) > 0) {
            errors.add("Percentage discount cannot exceed 100");
        }

        if (request.type() == DiscountType.FIXED_AMOUNT
                && request.value().compareTo(MAX_FIXED_AMOUNT_DISCOUNT) > 0) {
            errors.add("Fixed amount discount cannot exceed 1000");
        }
    }
}