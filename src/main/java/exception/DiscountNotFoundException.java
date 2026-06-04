package exception;

public class DiscountNotFoundException extends ShopException {
    public DiscountNotFoundException(String message) {
        super(message, "DISCOUNT_NOT_FOUND");
    }
}
