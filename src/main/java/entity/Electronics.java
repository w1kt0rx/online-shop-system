package entity;

import java.math.BigDecimal;

public class Electronics extends Product {
    public Electronics(Long id, String name, BigDecimal basePrice, int quantity) {
        super(id, name, basePrice, quantity);
    }
}
