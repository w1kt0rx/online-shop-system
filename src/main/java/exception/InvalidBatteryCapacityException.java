package exception;

public class InvalidBatteryCapacityException extends RuntimeException {
    public InvalidBatteryCapacityException(String message) {
        super(message);
    }
}
