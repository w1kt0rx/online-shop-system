package exception;

public class OrderProcessingException extends ShopException {
    public OrderProcessingException(String message) {
        super(message);
    }

    public OrderProcessingException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
