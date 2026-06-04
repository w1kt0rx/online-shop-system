package exception;

public class NotEnoughStockException extends ShopException {
    public NotEnoughStockException(String message) {
        super(message, "NOT_ENOUGH_STOCK");
    }
}
