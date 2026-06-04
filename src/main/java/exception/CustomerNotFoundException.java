package exception;

public class CustomerNotFoundException extends ShopException {
    public CustomerNotFoundException(String message) {
        super(message, "CUSTOMER_NOT_FOUND");
    }
}
