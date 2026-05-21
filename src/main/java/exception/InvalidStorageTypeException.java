package exception;

public class InvalidStorageTypeException extends RuntimeException {
    public InvalidStorageTypeException(String message) {
        super(message);
    }
}
