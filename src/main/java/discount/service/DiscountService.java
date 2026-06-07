package discount.service;

import discount.dto.CreateDiscountRequest;
import discount.dto.DiscountDto;
import discount.mapper.DiscountMapper;
import discount.model.Discount;
import discount.model.DiscountFactoryStrategy;
import discount.repository.DiscountRepository;
import discount.strategy.DiscountStrategy;
import discount.validator.DiscountValidator;
import exception.DiscountNotFoundException;
import exception.InvalidProductException;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Application service for discount management and application.
 * Handles the full lifecycle of a discount: creation (with uniqueness
 * and validity checks), querying, deactivation, and price calculation via the
 * Strategy pattern DiscountStrategy.
 */
@RequiredArgsConstructor
public class DiscountService {
    private final DiscountRepository discountRepository;

    /**
     * Creates a new discount from the given request.
     *
     * @param request creation parameters; validated before use
     * @return the persisted discount as a DTO
     * @throws InvalidProductException if a discount with the same code already exists,
     *                                 or if validation fails
     */
    public DiscountDto createDiscount(CreateDiscountRequest request) {
        DiscountValidator.validate(request);

        discountRepository.findByCode(request.code()).ifPresent(
                existing -> {
                    throw new InvalidProductException(
                            "Discount with code '" + request.code() + "' already exists"
                    );
                }
        );
        Discount discount = new Discount(
                discountRepository.getNextId(),
                request.code(),
                request.description(),
                request.type(),
                request.value(),
                request.minOrderValue(),
                request.validFrom(),
                request.validTo()
        );
        return DiscountMapper.toDto(discountRepository.save(discount));
    }

    /**
     * Looks up a discount by its code.
     *
     * @param code the discount code to search for
     * @return the matching discount DTO
     * @throws DiscountNotFoundException if no discount with that code exists
     */
    public DiscountDto getByCode(String code) {
        return discountRepository.findByCode(code)
                .map(DiscountMapper::toDto)
                .orElseThrow(() -> new DiscountNotFoundException(
                        "Discount with code '" + code + "' not found"
                ));
    }

    /**
     * Returns all discounts that are currently valid (active and within date range).
     *
     * @return list of active discount DTOs; empty list if none
     */
    public List<DiscountDto> getAllActive() {
        return discountRepository.getAll().stream()
                .filter(Discount::isValid)
                .map(DiscountMapper::toDto)
                .toList();
    }

    /**
     * Returns all discounts regardless of their active/expired status.
     *
     * @return list of all discount DTOs; empty list if none
     */
    public List<DiscountDto> getAll() {
        return discountRepository.getAll().stream()
                .map(DiscountMapper::toDto)
                .toList();
    }

    /**
     * Deactivates a discount so it can no longer be applied.
     *
     * @param id identifier of the discount to deactivate
     * @throws DiscountNotFoundException if no discount with that id exists
     */
    public void deActivate(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new DiscountNotFoundException(
                        "Discount with id " + id + " not found"
                ));
        discount.deActivate();
        discountRepository.save(discount);
    }

    /**
     * Applies a discount code to an order total and returns the discounted amount.
     * The appropriate DiscountStrategy is selected via
     * DiscountFactoryStrategy based on the discount type.
     *
     * @param code       the discount code to apply
     * @param orderTotal the original order amount before discount
     * @return the discounted total; always >= 0
     * @throws DiscountNotFoundException if the code does not exist or is expired/inactive
     * @throws InvalidProductException   if the order total is below the minimum required value
     */
    public BigDecimal applyDiscount(String code, BigDecimal orderTotal) {
        Discount discount = discountRepository.findByCode(code)
                .orElseThrow(() -> new DiscountNotFoundException(
                        "Discount code '" + code + "' not found"
                ));

        if (!discount.isValid()) {
            throw new DiscountNotFoundException(
                    "Discount code '" + code + "' is expired or inactive"
            );
        }

        if (discount.getMinOrderValue() != null
                && orderTotal.compareTo(discount.getMinOrderValue()) < 0) {
            throw new InvalidProductException(
                    "Order total " + orderTotal + " pln does not meet the minimum "
                            + discount.getMinOrderValue() + " pln required for this discount");
        }

        DiscountStrategy discountStrategy = DiscountFactoryStrategy.create(discount);
        return discountStrategy.apply(orderTotal);
    }

    /**
     * Returns a human-readable description of the discount identified by code,
     * or an empty Optional if the code is unknown or the discount is no longer valid.
     *
     * @param code the discount code to describe
     * @return an Optional containing the description, or empty
     */
    public Optional<String> describeDiscount(String code) {
        return discountRepository.findByCode(code)
                .filter(Discount::isValid)
                .map(discount -> DiscountFactoryStrategy.create(discount).describe());
    }
}
