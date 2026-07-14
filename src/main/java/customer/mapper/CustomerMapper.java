package customer.mapper;

import cart.mapper.CartMapper;
import customer.dto.CustomerDto;
import customer.model.Customer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomerMapper {

    /**
     * Maps a {@link Customer} to its public DTO. The password hash is
     * intentionally never copied — {@link CustomerDto} has no field for it.
     */
    public static CustomerDto toDto(Customer customer) {
        return new CustomerDto(
            customer.getId(),
            customer.getName(),
            customer.getEmail(),
            CartMapper.toDto(customer.getCart())
        );
    }
}
