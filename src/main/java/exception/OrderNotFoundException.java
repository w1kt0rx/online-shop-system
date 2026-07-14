package exception;

public class OrderNotFoundException extends ShopException {

    public OrderNotFoundException(Long id) {
        super(String.format("Order with id: %d not found", id));
    }
}
