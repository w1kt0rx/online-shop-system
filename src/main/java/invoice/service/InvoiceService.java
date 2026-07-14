package invoice.service;

import cart.dto.CartItemDto;
import exception.OrderNotFoundException;
import invoice.dto.InvoiceDto;
import invoice.mapper.InvoiceMapper;
import invoice.model.Invoice;
import invoice.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceDto createInvoice(
        Long orderId,
        Long customerId,
        String customerName,
        List<CartItemDto> items,
        BigDecimal finalAmount
    ) {
        Invoice invoice = new Invoice(null, orderId, customerId, customerName, items, finalAmount);

        return InvoiceMapper.toDto(invoiceRepository.save(invoice));
    }

    public InvoiceDto getInvoiceByOrderId(Long orderId) {
        return invoiceRepository
            .getAll()
            .stream()
            .filter(inv -> inv.getOrderId().equals(orderId))
            .findFirst()
            .map(InvoiceMapper::toDto)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public List<InvoiceDto> getAllInvoices() {
        return invoiceRepository.getAll().stream().map(InvoiceMapper::toDto).toList();
    }
}
