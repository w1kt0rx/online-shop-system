package customer.mapper;

import cart.mapper.CartMapper;
import customer.dto.CustomerDto;
import customer.model.Customer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomerMapper {

    public static CustomerDto toDto(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getName(),
                CartMapper.toDto(customer.getCart())
        );
    }
}