package discount.service;

import discount.dto.CreateDiscountRequest;
import discount.dto.DiscountDto;
import discount.mapper.DiscountMapper;
import discount.model.Discount;
import discount.model.DiscountFactoryStrategy;
import discount.repositoy.DiscountRepository;
import discount.strategy.DiscountStrategy;
import discount.validator.DiscountValidator;
import exception.DiscountNotFoundException;
import exception.InvalidProductException;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DiscountService {
    private final DiscountRepository discountRepository;

    public DiscountDto createDiscount(CreateDiscountRequest request) {
        DiscountValidator.validate(request);

        discountRepository.findByCode(request.code()).ifPresent(
                exisiting -> {
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

    public DiscountDto getByCode(String code) {
        return discountRepository.findByCode(code)
                .map(DiscountMapper::toDto)
                .orElseThrow(() -> new DiscountNotFoundException(
                        "Discount with code '" + code + "' not found"
                ));
    }

    public List<DiscountDto> getAllActive() {
        return discountRepository.getAll().stream()
                .filter(Discount::isValid)
                .map(DiscountMapper::toDto)
                .toList();
    }

    public List<DiscountDto> getAll() {
        return discountRepository.getAll().stream()
                .map(DiscountMapper::toDto)
                .toList();
    }

    public void deActivate(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new DiscountNotFoundException(
                        "Discount with id " + id + " not found"
                ));
        discount.deActivate();
        discountRepository.save(discount);
    }

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

    public Optional<String> describeDiscount(String code) {
        return discountRepository.findByCode(code)
                .filter(Discount::isValid)
                .map(discount -> DiscountFactoryStrategy.create(discount).describe());
    }
}
