package invoice.mapper;

import invoice.dto.InvoiceDto;
import invoice.model.Invoice;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InvoiceMapper {

    public static InvoiceDto toDto(Invoice invoice) {
        return new InvoiceDto(
                invoice.getId(),
                invoice.getOrderId(),
                invoice.getCustomerId(),
                invoice.getCustomerName(),
                invoice.getItems(),
                invoice.getTotalAmount(),
                invoice.getIssuedAt()
        );
    }
}
