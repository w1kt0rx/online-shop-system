package exception;

public class CustomerNotFoundException extends ShopException {

    public CustomerNotFoundException(Long id) {
        super(String.format("Customer with id %d not found", id));
    }
}
