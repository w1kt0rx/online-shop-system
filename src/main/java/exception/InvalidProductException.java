package exception;

public class InvalidProductException extends ShopException {
    public InvalidProductException(String message) {
        super(message, "INVALID_PRODUCT");
    }
}
