package cart.mapper;

import cart.dto.CartDto;
import cart.dto.CartItemDto;
import cart.model.Cart;
import org.junit.jupiter.api.Test;
import product.model.ProductType;
import product.model.computer.Computer;
import product.model.computer.configuration.ComputerConfiguration;
import product.model.electronics.Electronics;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CartMapperTest {

    @Test
    void shouldMapEmptyCartToDto() {
        Cart cart = new Cart();
        CartDto dto = CartMapper.toDto(cart);

        assertThat(dto.items()).isEmpty();
        assertThat(dto.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldMapCartWithOneItemToDto() {
        Cart cart = new Cart();
        Electronics monitor = new Electronics(1L, "Monitor", new BigDecimal("800"), 10);
        cart.addProduct(monitor, 2);

        CartDto dto = CartMapper.toDto(cart);

        assertThat(dto.items()).hasSize(1);
        assertThat(dto.totalPrice()).isEqualByComparingTo(new BigDecimal("1600"));
    }

    @Test
    void shouldMapCartWithMultipleItemsToDto() {
        Cart cart = new Cart();
        Electronics monitor = new Electronics(1L, "Monitor", new BigDecimal("800"), 10);
        Electronics keyboard = new Electronics(2L, "Keyboard", new BigDecimal("150"), 20);
        cart.addProduct(monitor, 1);
        cart.addProduct(keyboard, 3);

        CartDto dto = CartMapper.toDto(cart);

        assertThat(dto.items()).hasSize(2);
        assertThat(dto.totalPrice()).isEqualByComparingTo(new BigDecimal("1250"));
    }

    @Test
    void shouldMapCartItemToDtoWithCorrectFields() {
        Electronics product = new Electronics(5L, "Headphones", new BigDecimal("300"), 10);
        Cart cart = new Cart();
        cart.addProduct(product, 2);

        CartItemDto itemDto = CartMapper.toItemDto(cart.getCartItems().get(0));

        assertThat(itemDto.productId()).isEqualTo(5L);
        assertThat(itemDto.productName()).isEqualTo("Headphones");
        assertThat(itemDto.productType()).isEqualTo(ProductType.ELECTRONICS);
        assertThat(itemDto.unitPrice()).isEqualByComparingTo(new BigDecimal("300"));
        assertThat(itemDto.quantity()).isEqualTo(2);
        assertThat(itemDto.totalPrice()).isEqualByComparingTo(new BigDecimal("600"));
    }

    @Test
    void shouldMapComputerCartItemWithConfiguredPrice() {
        Computer computer = new Computer(1L, "Dell XPS", new BigDecimal("3000"), 5, new ComputerConfiguration());
        Cart cart = new Cart();
        cart.addProduct(computer, 1);

        CartItemDto itemDto = CartMapper.toItemDto(cart.getCartItems().get(0));

        assertThat(itemDto.productType()).isEqualTo(ProductType.COMPUTER);
        assertThat(itemDto.unitPrice()).isEqualByComparingTo(new BigDecimal("4000"));
        assertThat(itemDto.totalPrice()).isEqualByComparingTo(new BigDecimal("4000"));
    }
}