package cli;

import cart.dto.CartDto;
import cart.service.CartService;
import discount.service.DiscountService;
import exception.handler.GlobalExceptionHandler;
import invoice.dto.InvoiceDto;
import order.dto.OrderDto;
import order.facade.OrderFacade;
import order.model.OrderProcessingResult;
import order.service.OrderService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletionException;

public class OrderMenu extends BaseMenu {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrderService orderService;
    private final OrderFacade orderFacade;
    private final DiscountService discountService;
    private final CartService cartService;
    private final CartMenu cartMenu;

    public OrderMenu(
            Scanner scanner,
            Session session,
            GlobalExceptionHandler exHandler,
            OrderService orderService,
            OrderFacade orderFacade,
            DiscountService discountService,
            CartService cartService,
            CartMenu cartMenu
    ) {
        super(scanner, session, exHandler);
        this.orderService = orderService;
        this.orderFacade = orderFacade;
        this.discountService = discountService;
        this.cartService = cartService;
        this.cartMenu = cartMenu;
    }

    public void placeOrder() {
        cartMenu.viewCart();
        CartDto cart = cartService.getCart(session.getCurrentCustomerId());
        if (cart.items().isEmpty()) {
            print("Cart is empty. Add products before ordering.");
            return;
        }

        print(NL + "Discount code? (press Enter to skip): ");
        String code = scanner.nextLine().trim();
        String confirmedCode = null;

        if (!code.isEmpty()) {
            Optional<String> description = discountService.describeDiscount(code);
            if (description.isPresent()) {
                BigDecimal discounted = discountService.previewDiscountedTotal(code, cart.totalPrice());
                print(String.format("  Discount: %s", description.get()));
                print(String.format("  Original total:   %.2f PLN", cart.totalPrice().doubleValue()));
                print(String.format("  After discount:   %.2f PLN", discounted.doubleValue()));
                confirmedCode = code;
            } else {
                print("  Code not found or expired — proceeding without discount.");
            }
        }

        print(NL + "Confirm order? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
            print("Cancelled.");
            return;
        }

        try {
            print(NL + "Processing your order");
            InvoiceDto invoice = orderFacade.processOrderAsync(session.getCurrentCustomerId(), confirmedCode).join();
            printInvoice(invoice);
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex) {
                print(exHandler.handleAny(ex));
            } else {
                print(exHandler.handleUnexpected(e));
            }
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    public void printInvoice(InvoiceDto invoice) {
        print(NL + DLINE);
        print("              INVOICE / ORDER CONFIRMATION");
        print(DLINE);
        print("  Invoice No.:    " + invoice.id());
        print("  Order No.:      " + invoice.orderId());
        print("  Customer:       " + invoice.customerName() + "  (ID: " + invoice.customerId() + ")");
        print("  Date:           " + invoice.issuedAt().format(DATE_FMT));
        print(LINE);
        print(String.format("  %-28s  %5s  %12s", "Product", "Qty", "Amount"));
        print(LINE);
        invoice.items().forEach(item -> print(String.format(
                "  %-28s  %5d  %10.2f PLN",
                item.productName(), item.quantity(), item.totalPrice().doubleValue())));
        print(LINE);
        print(String.format("  %-34s  %10.2f PLN", "TOTAL:", invoice.totalAmount().doubleValue()));
        print(DLINE);
        print("  Thank you for your purchase!");
        print(DLINE);
    }

    public void viewOrders() {
        try {
            List<OrderDto> orders = orderService.getOrdersByCustomer(session.getCurrentCustomerId());
            print(NL + LINE);
            print("  YOUR ORDERS");
            print(LINE);
            if (orders.isEmpty()) {
                print("  No orders found.");
            } else {
                orders.forEach(o -> {
                    print(String.format("  Order #%d  |  %s  |  %.2f PLN  |  %s",
                            o.id(), o.orderStatus(),
                            o.totalPrice().doubleValue(),
                            o.createdAt().format(DATE_FMT)));
                    o.items().forEach(item -> print(String.format(
                            "    %-30s  x%d   %.2f PLN",
                            item.productName(), item.quantity(), item.totalPrice().doubleValue())));
                    print("");
                });
            }
            print(LINE);
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    public void batchCheckout() {
        print(NL + LINE);
        print("  BATCH CHECKOUT ");
        print(LINE);
        print("Enter customer IDs to check out, separated by commas: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            print("No customer IDs provided.");
            return;
        }

        List<Long> customerIds = new ArrayList<>();
        for (String part : input.split(",")) {
            try {
                customerIds.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException ignored) {
                print("  Skipping invalid customer ID: " + part.trim());
            }
        }

        if (customerIds.isEmpty()) {
            print("No valid customer IDs provided.");
            return;
        }

        print(String.format("Processing %d order(s)", customerIds.size()));
        List<OrderProcessingResult> results = orderFacade.processBatchOrders(customerIds);

        print(LINE);
        for (OrderProcessingResult result : results) {
            if (result.success()) {
                print(String.format("  Customer #%d: OK — invoice #%d, total %.2f PLN",
                        result.customerId(), result.invoice().id(),
                        result.invoice().totalAmount().doubleValue()));
            } else {
                print(String.format("  Customer #%d: FAILED — %s",
                        result.customerId(), result.errorMessage()));
            }
        }
        print(LINE);
    }
}