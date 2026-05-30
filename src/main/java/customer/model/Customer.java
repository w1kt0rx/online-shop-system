package customer.model;

import cart.model.Cart;
import customer.validator.CustomerValidator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Customer {
    private final Long id;
    private String name;
    private final Cart cart;

    public Customer(Long id, String name) {
        CustomerValidator.validate(id, name);
        this.id = id;
        this.name = name;
        this.cart = new Cart();
    }

    public void updateName(String name) {
        CustomerValidator.validateName(name);
        this.name = name;
    }
}
