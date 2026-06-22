package customer.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hashes and verifies passwords using SHA-256 with a per-password random salt.
 *
 * <p>Stored format: {@code base64(salt):base64(hash)} — the salt travels with
 * the hash so verification never needs a separate lookup. A new random salt
 * is generated on every {@link #hash(String)} call, so two identical
 * passwords produce different stored values (defeats rainbow tables and
 * makes equal-password detection impossible from the stored string alone).</p>
 *
 * <p>This relies only on the JDK ({@code java.security}) — no external
 * dependency. For a production system, a slow adaptive hash such as
 * BCrypt or Argon2 would be preferable since SHA-256 is fast and therefore
 * more brute-forceable; that trade-off is accepted here to keep the
 * project dependency-free.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PasswordHasher {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hashes a plaintext password with a freshly generated random salt.
     *
     * @param plainPassword the plaintext password to hash; must not be null
     * @return a string in the form {@code base64(salt):base64(hash)}
     */
    public static String hash(String plainPassword) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = digest(plainPassword, salt);
        return encode(salt) + ":" + encode(hash);
    }

    /**
     * Verifies a plaintext password against a previously stored hash.
     *
     * @param plainPassword the plaintext password supplied at login
     * @param storedHash    the value previously returned by {@link #hash(String)}
     * @return {@code true} if the password matches; {@code false} on mismatch
     *         or if storedHash is malformed
     */
    public static boolean matches(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split(":", 2);
        if (parts.length != 2) {
            return false;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            byte[] actualHash = digest(plainPassword, salt);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] digest(String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ALGORITHM + " not available on this JVM", e);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}