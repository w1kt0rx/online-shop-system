package order.repository.impl.file;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public record OrderSnapshot(
        Long id,
        Long customerId,
        BigDecimal totalPrice,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        ZonedDateTime confirmedAt,
        String status
) {
}
