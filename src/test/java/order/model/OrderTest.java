package order.model;

import cart.model.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.model.electronics.Electronics;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    private Electronics product;
    private CartItem cartItem;
    private Order order;

    @BeforeEach
    void setUp() {
        product = new Electronics(1L, "Monitor", new BigDecimal("800"), 10);
        cartItem = new CartItem(product, 2);
        order = new Order(1L, 1L, List.of(cartItem));
    }

    @Test
    void shouldCreateOrderWithPendingStatus() {
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void shouldSetCreatedAtTimestampOnCreation() {
        ZonedDateTime before = ZonedDateTime.now().minusSeconds(1);
        Order freshOrder = new Order(2L, 1L, List.of(cartItem));
        ZonedDateTime after = ZonedDateTime.now().plusSeconds(1);

        assertThat(freshOrder.getCreatedAt()).isAfter(before).isBefore(after);
    }

    @Test
    void shouldCalculateTotalPriceCorrectly() {
        // 2 * 800 = 1600
        assertThat(order.getTotalPrice()).isEqualByComparingTo(new BigDecimal("1600"));
    }

    @Test
    void shouldCalculateTotalPriceForMultipleItems() {
        Electronics keyboard = new Electronics(2L, "Keyboard", new BigDecimal("150"), 5);
        CartItem keyboardItem = new CartItem(keyboard, 3);
        Order multiOrder = new Order(3L, 1L, List.of(cartItem, keyboardItem));

        // (2*800) + (3*150) = 1600 + 450 = 2050
        assertThat(multiOrder.getTotalPrice()).isEqualByComparingTo(new BigDecimal("2050"));
    }

    @Test
    void shouldConfirmOrder() {
        order.confirm();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldCancelOrder() {
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldHaveImmutableItems() {
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> order.getItems().add(cartItem)
        );
    }

    @Test
    void shouldStoreCopyOfItems() {
        List<CartItem> mutableItems = new java.util.ArrayList<>(List.of(cartItem));
        Order o = new Order(4L, 1L, mutableItems);
        mutableItems.clear();

        assertThat(o.getItems()).hasSize(1);
    }

    @Test
    void shouldStoreCustomerId() {
        assertThat(order.getCustomerId()).isEqualTo(1L);
    }

    @Test
    void shouldReturnEmptyTotalForEmptyItemsList() {
        Order emptyOrder = new Order(5L, 1L, List.of());
        assertThat(emptyOrder.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
