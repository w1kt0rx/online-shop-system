package discount.repository;

import discount.model.Discount;
import java.util.Optional;
import product.repository.Repository;

public interface DiscountRepository extends Repository<Discount> {
    Optional<Discount> findByCode(String code);
}
