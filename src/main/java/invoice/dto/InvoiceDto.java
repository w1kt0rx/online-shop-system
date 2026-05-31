package invoice.dto;

import cart.dto.CartItemDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceDto(
        Long id,
        Long orderId,
        Long customerId,
        String customerName,
        List<CartItemDto> items,
        BigDecimal totalAmount,
        LocalDateTime issuedAt
) {}
