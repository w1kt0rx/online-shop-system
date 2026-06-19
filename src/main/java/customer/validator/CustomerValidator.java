package customer.validator;

import exception.InvalidProductException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomerValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Validates all fields required to register a new customer.
     *
     * @param id       proposed identifier; not null, not negative
     * @param name     display name; not blank
     * @param email    login email; valid format
     * @param password plaintext password; meets minimum strength
     * @throws InvalidProductException listing every failed check
     */
    public static void validate(Long id, String name, String email, String password) {
        List<String> errors = new ArrayList<>();

        if (id == null || id < 0) {
            errors.add("Customer id cannot be null or negative");
        }
        if (name == null || name.isBlank()) {
            errors.add("Customer name cannot be blank");
        }
        collectEmailErrors(email, errors);
        collectPasswordErrors(password, errors);

        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Customer name cannot be blank");
        }
    }

    /**
     * Validates email format.
     *
     * @param email candidate email address
     * @throws InvalidProductException if blank or not a valid email shape
     */
    public static void validateEmail(String email) {
        List<String> errors = new ArrayList<>();
        collectEmailErrors(email, errors);
        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    /**
     * Validates password strength.
     *
     * @param password candidate plaintext password
     * @throws InvalidProductException if blank or shorter than the minimum length
     */
    public static void validatePassword(String password) {
        List<String> errors = new ArrayList<>();
        collectPasswordErrors(password, errors);
        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    private static void collectEmailErrors(String email, List<String> errors) {
        if (email == null || email.isBlank()) {
            errors.add("Email cannot be blank");
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.add("Email format is invalid");
        }
    }

    private static void collectPasswordErrors(String password, List<String> errors) {
        if (password == null || password.isBlank()) {
            errors.add("Password cannot be blank");
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            errors.add("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }
    }
}