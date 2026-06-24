package customer.validator;

import exception.InvalidProductException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomerValidator {

    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();
    private static final int MIN_PASSWORD_LENGTH = 8;

    public static void validate(Long id, String name, String email, String password) {
        List<String> errors = new ArrayList<>();

        errors.addAll(validateIdErrors(id));
        errors.addAll(validateNameErrors(name));
        errors.addAll(validateEmailErrors(email));
        errors.addAll(validatePasswordErrors(password));

        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    public static void validateName(String name) {
        List<String> errors = validateNameErrors(name);
        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    public static void validateEmail(String email) {
        List<String> errors = validateEmailErrors(email);
        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    public static void validatePassword(String password) {
        List<String> errors = validatePasswordErrors(password);
        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    private static List<String> validateIdErrors(Long id) {
        List<String> errors = new ArrayList<>();

        if (id != null && id < 0) {
            errors.add("Customer id cannot be negative");
        }

        return errors;
    }

    private static List<String> validateNameErrors(String name) {
        List<String> errors = new ArrayList<>();

        if (name == null || name.isBlank()) {
            errors.add("Customer name cannot be blank");
        }

        return errors;
    }

    private static List<String> validateEmailErrors(String email) {
        List<String> errors = new ArrayList<>();

        if (email == null || email.isBlank()) {
            errors.add("Email cannot be blank");
        } else if (!EMAIL_VALIDATOR.isValid(email.trim())) {
            errors.add("Email format is invalid");
        }

        return errors;
    }

    private static List<String> validatePasswordErrors(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isBlank()) {
            errors.add("Password cannot be blank");
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            errors.add("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }

        return errors;
    }
}