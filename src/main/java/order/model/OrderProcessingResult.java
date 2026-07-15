package order.model;

import invoice.dto.InvoiceDto;

public record OrderProcessingResult(Long customerId, boolean success, InvoiceDto invoice, String errorMessage) {
    public static OrderProcessingResult success(Long customerId, InvoiceDto invoice) {
        return new OrderProcessingResult(customerId, true, invoice, null);
    }

    public static OrderProcessingResult failure(Long customerId, String errorMessage) {
        return new OrderProcessingResult(customerId, false, null, errorMessage);
    }
}
