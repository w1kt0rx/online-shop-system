package exception.handler;

import exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import product.model.ProductType;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }


    @ParameterizedTest
    @MethodSource("provideShopExceptions")
    void shouldFormatMessageWithErrorCode(ShopException ex, String expectedCode) {
        String result = handler.handler(ex);

        assertThat(result).startsWith("[" + expectedCode + "]");
        assertThat(result).contains(ex.getMessage());
    }

    private static Stream<Arguments> provideShopExceptions() {
        return Stream.of(
                Arguments.of(
                        new ProductNotFoundException(ProductType.COMPUTER, 1L),
                        "PRODUCT_NOT_FOUND"
                ),
                Arguments.of(
                        new InvalidProductException("Invalid data"),
                        "INVALID_PRODUCT"
                ),
                Arguments.of(
                        new InsufficientStockException("Out of stock"),
                        "INSUFFICIENT_STOCK"
                ),
                Arguments.of(
                        new EmptyCartException("Cart is empty"),
                        "EMPTY_CART"
                ),
                Arguments.of(
                        new DiscountNotFoundException("Discount not found"),
                        "DISCOUNT_NOT_FOUND"
                ),
                Arguments.of(
                        new OrderProcessingException("Processing failed"),
                        "ORDER_PROCESSING_FAILED"
                )
        );
    }


    @Test
    void shouldReturnGenericMessageForUnexpectedException() {
        RuntimeException unexpected = new RuntimeException("Database connection lost");

        String result = handler.handleUnexpected(unexpected);

        assertThat(result).startsWith("[UNEXPECTED_ERROR]");
        assertThat(result).doesNotContain("Database connection lost");
    }


    @Test
    void shouldRouteShopExceptionThroughHandler() {
        ProductNotFoundException ex =
                new ProductNotFoundException(ProductType.COMPUTER, 1L);

        String result = handler.handleAny(ex);

        assertThat(result).startsWith("[PRODUCT_NOT_FOUND]");
        assertThat(result).contains("COMPUTER with id 1 not found");
    }

    @Test
    void shouldRouteUnexpectedExceptionThroughHandleUnexpected() {
        RuntimeException ex = new RuntimeException("Unexpected error");

        String result = handler.handleAny(ex);

        assertThat(result).startsWith("[UNEXPECTED_ERROR]");
    }

    @Test
    void shouldFormatMessageAsCodePlusBracket() {
        ShopException ex =
                new ProductNotFoundException(ProductType.COMPUTER, 1L);

        String result = handler.handler(ex);

        assertThat(result).matches("\\[.+\\] .+");
    }
}
