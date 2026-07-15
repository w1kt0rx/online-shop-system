package invoice.dto;

import cart.dto.CartItemDto;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record InvoiceDto(
    Long id,
    Long orderId,
    Long customerId,
    String customerName,
    List<CartItemDto> items,
    BigDecimal totalAmount,
    ZonedDateTime issuedAt
) {}
