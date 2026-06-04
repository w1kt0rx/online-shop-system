package customer.mapper;

import customer.dto.CustomerDto;
import customer.model.Customer;
import org.junit.jupiter.api.Test;
import product.model.electronics.Electronics;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    @Test
    void shouldMapCustomerWithEmptyCartToDto() {
        Customer customer = new Customer(1L, "Jan Kowalski");

        CustomerDto dto = CustomerMapper.toDto(customer);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Jan Kowalski");
        assertThat(dto.cart()).isNotNull();
        assertThat(dto.cart().items()).isEmpty();
        assertThat(dto.cart().totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldMapCustomerWithItemsInCartToDto() {
        Customer customer = new Customer(2L, "Anna Nowak");
        Electronics product = new Electronics(1L, "Monitor", new BigDecimal("500"), 10);
        customer.getCart().addProduct(product, 2);

        CustomerDto dto = CustomerMapper.toDto(customer);

        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.name()).isEqualTo("Anna Nowak");
        assertThat(dto.cart().items()).hasSize(1);
        assertThat(dto.cart().totalPrice()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    void shouldPreserveCustomerNameExactly() {
        Customer customer = new Customer(3L, "Józef Ząbek-Wiśniewski");

        CustomerDto dto = CustomerMapper.toDto(customer);

        assertThat(dto.name()).isEqualTo("Józef Ząbek-Wiśniewski");
    }
}
