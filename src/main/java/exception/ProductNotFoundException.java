package exception;

import product.model.ProductType;

public class ProductNotFoundException extends ShopException {

    public ProductNotFoundException(ProductType productType, Long productId) {
        super(String.format("%s with id %d not found", productType, productId));
    }
}
