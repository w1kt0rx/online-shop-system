package order.repository.impl.file;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSnapshot(
        Long id,
        Long customerId,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime confirmedAt,
        String status
) {
}
