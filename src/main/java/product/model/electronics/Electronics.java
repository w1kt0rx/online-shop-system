package product.model.electronics;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.ToString;
import product.model.Product;
import product.model.ProductType;

@ToString
@Getter
public class Electronics extends Product {

    public Electronics(Long id, String name, BigDecimal basePrice, int quantity) {
        super(id, name, basePrice, quantity, ProductType.ELECTRONICS);
    }
}
