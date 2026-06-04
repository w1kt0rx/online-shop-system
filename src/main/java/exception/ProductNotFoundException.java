package exception;

public class ProductNotFoundException extends ShopException {
    public ProductNotFoundException(String message) {
        super(message, "PRODUCT_NOT_FOUND");
    }
}
