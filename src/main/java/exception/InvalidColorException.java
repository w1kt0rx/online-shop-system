package exception;

public class InvalidColorException extends RuntimeException {
    public InvalidColorException(String message) {
        super(message);
    }
}
