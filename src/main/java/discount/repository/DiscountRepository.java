package discount.repository;

import discount.model.Discount;
import product.repository.Repository;

import java.util.Optional;

public interface DiscountRepository extends Repository<Discount> {
    Optional<Discount> findByCode(String code);
}
