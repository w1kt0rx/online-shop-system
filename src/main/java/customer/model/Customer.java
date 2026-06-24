package customer.model;

import cart.model.Cart;
import customer.security.PasswordHasher;
import customer.validator.CustomerValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A registered customer of the shop.
 *
 * <p>Authentication is email + password based. The password is never stored
 * in plaintext — only {@link #passwordHash}, produced by
 * {@link PasswordHasher#hash(String)}, is kept on the object.
 * {@link #toString()} deliberately excludes the hash so it never leaks
 * into logs.</p>
 */
@Getter
@ToString
public class Customer {
    @Setter
    private Long id;
    private String name;
    private String email;
    @ToString.Exclude
    private String passwordHash;
    private final Cart cart;

    /**
     * Creates a new customer with a hashed password.
     *
     * @param id            unique identifier assigned by the repository
     * @param name          display name
     * @param email         unique login email, validated and normalised to lowercase
     * @param plainPassword plaintext password; hashed immediately and never stored as-is
     * @throws exception.InvalidProductException if any field fails validation
     */
    public Customer(Long id, String name, String email, String plainPassword) {
        CustomerValidator.validate(id, name, email, plainPassword);
        this.id = id;
        this.name = name;
        this.email = normalizeEmail(email);
        this.passwordHash = PasswordHasher.hash(plainPassword);
        this.cart = new Cart();
    }

    /**
     * Verifies a plaintext password against this customer's stored hash.
     *
     * @param plainPassword the password supplied at login
     * @return {@code true} if it matches; {@code false} otherwise
     */
    public boolean checkPassword(String plainPassword) {
        return PasswordHasher.matches(plainPassword, passwordHash);
    }

    /**
     * Updates the display name after validation.
     *
     * @param name new display name; not blank
     */
    public void updateName(String name) {
        CustomerValidator.validateName(name);
        this.name = name;
    }

    /**
     * Updates the login email after validation. Normalised to lowercase
     * so lookups and uniqueness checks are case-insensitive.
     *
     * @param email new email address; valid format
     */
    public void updateEmail(String email) {
        CustomerValidator.validateEmail(email);
        this.email = normalizeEmail(email);
    }

    /**
     * Changes the password, re-hashing the new plaintext value.
     *
     * @param newPlainPassword new plaintext password; not blank
     */
    public void changePassword(String newPlainPassword) {
        CustomerValidator.validatePassword(newPlainPassword);
        this.passwordHash = PasswordHasher.hash(newPlainPassword);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}