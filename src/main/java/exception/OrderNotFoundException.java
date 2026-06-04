package exception;

public class OrderNotFoundException extends ShopException {
    public OrderNotFoundException(String message) {
        super(message, "ORDER_NOT_FOUND");
    }
}
