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

        assertThat(result).contains("COMPUTER with id 1 not found");
    }

    @Test
    void shouldRouteUnexpectedExceptionThroughHandleUnexpected() {
        RuntimeException ex = new RuntimeException("Unexpected error");

        String result = handler.handleAny(ex);

        assertThat(result).startsWith("[UNEXPECTED_ERROR]");
    }
}
