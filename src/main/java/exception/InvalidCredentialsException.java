package exception;

/** Thrown when login fails — unknown email or wrong password. */
public class InvalidCredentialsException extends ShopException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}