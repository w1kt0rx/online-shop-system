package order.validator;

import cart.model.Cart;
import exception.EmptyCartException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderValidator {

    public static void validateCart(Cart cart) {
        if ((cart == null) || cart.getCartItems().isEmpty()) {
            throw new EmptyCartException("Cannot place an order with an empty cart");
        }
    }
}
