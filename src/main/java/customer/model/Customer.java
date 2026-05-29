package customer.model;

import cart.model.Cart;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Customer {
    private Long id;
    private String name;
    private final Cart cart;


}
