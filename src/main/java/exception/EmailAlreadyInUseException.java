package exception;

/** Thrown when registering with an email that already belongs to another customer. */
public class EmailAlreadyInUseException extends ShopException {

    public EmailAlreadyInUseException(String email) {
        super(String.format("Email '%s' is already registered", email));
    }
}
