package exception;

public class OrderProcessingException extends ShopException {
    public OrderProcessingException(String message) {
        super(message, "ORDER_PROCESSING_FAILED");
    }

    public OrderProcessingException(String message, Throwable cause) {
        super(message, "ORDER_PROCESSING_FAILED");
        initCause(cause);
    }
}
