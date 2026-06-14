package cli;

import cart.dto.CartDto;
import cart.service.CartService;
import customer.dto.CreateCustomerRequest;
import customer.dto.CustomerDto;
import customer.service.CustomerService;
import discount.dto.DiscountDto;
import discount.service.DiscountService;
import exception.handler.GlobalExceptionHandler;
import invoice.dto.InvoiceDto;
import order.dto.OrderDto;
import order.facade.OrderFacade;
import order.model.OrderProcessingResult;
import order.service.OrderService;
import product.dto.computer.ComputerDto;
import product.dto.computer.UpdateComputerRequest;
import product.dto.electronics.ElectronicsDto;
import product.dto.smartphone.SmartphoneDto;
import product.dto.smartphone.UpdateSmartphoneRequest;
import product.facade.ProductFacade;
import product.model.ProductType;
import product.model.computer.configuration.*;
import product.model.smartphone.configuration.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletionException;

public class ShopCLI {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String LINE = "─".repeat(55);
    private static final String DLINE = "═".repeat(55);

    private final Scanner scanner = new Scanner(System.in);

    private final ProductFacade productFacade;
    private final CartService cartService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final OrderFacade orderFacade;
    private final DiscountService discountService;
    private final GlobalExceptionHandler exHandler;

    private Long currentCustomerId = null;

    public ShopCLI(ProductFacade productFacade,
                   CartService cartService,
                   CustomerService customerService,
                   OrderService orderService,
                   OrderFacade orderFacade,
                   DiscountService discountService,
                   GlobalExceptionHandler exHandler) {
        this.productFacade = productFacade;
        this.cartService = cartService;
        this.customerService = customerService;
        this.orderService = orderService;
        this.orderFacade = orderFacade;
        this.discountService = discountService;
        this.exHandler = exHandler;
    }


    public void start() {
        printBanner();
        loginOrRegister();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt();
            switch (choice) {
                case 1 -> browseProducts();
                case 2 -> viewCart();
                case 3 -> addProductToCart();
                case 4 -> removeProductFromCart();
                case 5 -> placeOrder();
                case 6 -> viewOrders();
                case 7 -> showDiscounts();
                case 8 -> switchCustomer();
                case 9 -> batchCheckout();
                case 0 -> running = false;
                default -> print("Wrong option, try again.");
            }
        }
        print("\nThank you. Goodbye!");
    }


    private void loginOrRegister() {
        print("\n" + LINE);
        print("  1. Login (enter customer ID)");
        print("  2. Sign up as new customer");
        print(LINE);
        int choice = readInt();

        if (choice == 1) {
            print("Enter customer ID: ");
            long id = readLong();
            try {
                CustomerDto customer = customerService.getCustomerById(id);
                currentCustomerId = customer.id();
                print("Logged in as: " + customer.name());
            } catch (Exception e) {
                print(exHandler.handleAny(e));
                print("Signing you up instead...");
                registerCustomer();
            }
        } else {
            registerCustomer();
        }
    }

    private void registerCustomer() {
        print("Enter your name: ");
        String name = scanner.nextLine().trim();
        try {
            CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest(name));
            currentCustomerId = customer.id();
            print("Registered! Your ID: " + customer.id() + " (save this to log in later)");
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    private void switchCustomer() {
        loginOrRegister();
    }


    private void browseProducts() {
        print("\n" + LINE);
        print("  BROWSE PRODUCTS");
        print(LINE);
        print("  1. Computers");
        print("  2. Smartphones");
        print("  3. Electronics");
        print("  0. Back");
        print(LINE);

        switch (readInt()) {
            case 1 -> listComputers();
            case 2 -> listSmartphones();
            case 3 -> listElectronics();
        }
    }

    private void listComputers() {
        try {
            List<ComputerDto> list = productFacade.getAllComputers();
            if (list.isEmpty()) {
                print("No computers in the offer.");
                return;
            }
            print("\n  COMPUTERS:");
            print(LINE);
            list.forEach(c -> {
                print(String.format("  [ID:%-2d]  %-30s  %.2f PLN  (stock: %d)",
                        c.id(), c.name(), c.totalPrice().doubleValue(), c.quantity()));
                if (c.computerConfiguration() != null) {
                    var cfg = c.computerConfiguration();
                    print(String.format("           %s | %dGB RAM | %s | %s",
                            cfg.processor() != null ? cfg.processor().getDescription() : "-",
                            cfg.ram() != null ? cfg.ram().getCapacity() : 0,
                            cfg.storageType() != null ? cfg.storageType().getDescription() : "-",
                            cfg.graphicsCard() != null ? cfg.graphicsCard().getDescription() : "-"));
                }
                print("");
            });
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    private void listSmartphones() {
        try {
            List<SmartphoneDto> list = productFacade.getAllSmartphones();
            if (list.isEmpty()) {
                print("No smartphones in the offer.");
                return;
            }
            print("\n  SMARTPHONES:");
            print(LINE);
            list.forEach(s -> {
                print(String.format("  [ID:%-2d]  %-30s  %.2f PLN  (stock: %d)",
                        s.id(), s.name(), s.totalPrice().doubleValue(), s.quantity()));
                if (s.smartphoneConfiguration() != null) {
                    var cfg = s.smartphoneConfiguration();
                    String accessories = cfg.accessories() != null && !cfg.accessories().isEmpty()
                            ? cfg.accessories().stream().map(Accessory::getDescription).reduce((a, b) -> a + ", " + b).orElse("")
                            : "none";
                    print(String.format("           %s | %s | Accessories: %s",
                            cfg.color() != null ? cfg.color().getDescription() : "-",
                            cfg.batteryCapacity() != null ? cfg.batteryCapacity().getDescription() : "-",
                            accessories));
                }
                print("");
            });
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    private void listElectronics() {
        try {
            List<ElectronicsDto> list = productFacade.getAllElectronics();
            if (list.isEmpty()) {
                print("No electronics in the offer.");
                return;
            }
            print("\n  ELECTRONICS:");
            print(LINE);
            list.forEach(e -> print(String.format("  [ID:%-2d]  %-30s  %.2f PLN  (stock: %d)",
                    e.id(), e.name(), e.basePrice().doubleValue(), e.quantity())));
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }


    private void viewCart() {
        try {
            CartDto cart = cartService.getCart(currentCustomerId);
            print("\n" + LINE);
            print("  YOUR CART");
            print(LINE);
            if (cart.items().isEmpty()) {
                print("  Cart is empty.");
            } else {
                cart.items().forEach(i -> print(String.format(
                        "  %-30s  x%-3d  %10.2f PLN",
                        i.productName(), i.quantity(), i.totalPrice().doubleValue())));
                print(LINE);
                print(String.format("  %-34s  %10.2f PLN", "TOTAL:", cart.totalPrice().doubleValue()));
            }
            print(LINE);
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    private void addProductToCart() {
        print("\n  Choose product type:");
        print("  1. Computer   2. Smartphone   3. Electronics");
        int typeChoice = readInt();

        switch (typeChoice) {
            case 1 -> {
                listComputers();
                configureComputerAndAdd();
            }
            case 2 -> {
                listSmartphones();
                configureSmartphoneAndAdd();
            }
            case 3 -> {
                listElectronics();
                addElectronicsToCart();
            }
            default -> print("Invalid type.");
        }
    }

    private void configureComputerAndAdd() {
        print("Enter computer ID: ");
        long productId = readLong();

        print("\n  Choose processor:");
        Processor[] processors = Processor.values();
        for (int i = 0; i < processors.length; i++)
            print(String.format("  %d. %-30s  +%.0f PLN", i + 1,
                    processors[i].getDescription(), processors[i].getPrice().doubleValue()));
        int pIdx = readInt() - 1;

        print("\n  Choose RAM:");
        Ram[] rams = Ram.values();
        for (int i = 0; i < rams.length; i++)
            print(String.format("  %d. %-4dGB  +%.0f PLN", i + 1,
                    rams[i].getCapacity(), rams[i].getPrice().doubleValue()));
        int rIdx = readInt() - 1;

        print("\n  Choose storage:");
        StorageType[] storages = StorageType.values();
        for (int i = 0; i < storages.length; i++)
            print(String.format("  %d. %-30s  +%.0f PLN", i + 1,
                    storages[i].getDescription(), storages[i].getPrice().doubleValue()));
        int sIdx = readInt() - 1;

        print("\n  Choose graphics card:");
        GraphicsCard[] gpus = GraphicsCard.values();
        for (int i = 0; i < gpus.length; i++)
            print(String.format("  %d. %-30s  +%.0f PLN", i + 1,
                    gpus[i].getDescription(), gpus[i].getPrice().doubleValue()));
        int gIdx = readInt() - 1;

        print("Quantity: ");
        int qty = readInt();

        try {
            ComputerDto existing = productFacade.getComputerById(productId);
            productFacade.updateComputer(productId, new UpdateComputerRequest(
                    existing.name(), existing.basePrice(), existing.quantity(),
                    processors[pIdx], rams[rIdx], storages[sIdx], gpus[gIdx]));

            CartDto cart = cartService.addProduct(currentCustomerId, productId, ProductType.COMPUTER, qty);
            print(String.format("Added to cart! Cart total: %.2f PLN", cart.totalPrice().doubleValue()));
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    private void configureSmartphoneAndAdd() {
        print("Enter smartphone ID: ");
        long productId = readLong();

        print("\n  Choose color:");
        Color[] colors = Color.values();
        for (int i = 0; i < colors.length; i++)
            print(String.format("  %d. %-15s  +%.0f PLN", i + 1,
                    colors[i].getDescription(), colors[i].getPrice().doubleValue()));
        int cIdx = readInt() - 1;

        print("\n  Choose battery capacity:");
        BatteryCapacity[] batteries = BatteryCapacity.values();
        for (int i = 0; i < batteries.length; i++)
            print(String.format("  %d. %-20s  +%.0f PLN", i + 1,
                    batteries[i].getDescription(), batteries[i].getPrice().doubleValue()));
        int bIdx = readInt() - 1;

        print("\n  Choose accessories (comma-separated numbers, 0 = none):");
        Accessory[] accessories = Accessory.values();
        for (int i = 0; i < accessories.length; i++)
            print(String.format("  %d. %-20s  +%.0f PLN", i + 1,
                    accessories[i].getDescription(), accessories[i].getPrice().doubleValue()));

        Set<Accessory> selected = new HashSet<>();
        String input = scanner.nextLine().trim();
        if (!input.equals("0")) {
            for (String part : input.split(",")) {
                try {
                    int idx = Integer.parseInt(part.trim()) - 1;
                    if (idx >= 0 && idx < accessories.length) selected.add(accessories[idx]);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        print("Quantity: ");
        int qty = readInt();

        try {
            SmartphoneDto existing = productFacade.getSmartphoneById(productId);
            productFacade.updateSmartphone(productId, new UpdateSmartphoneRequest(
                    existing.name(), existing.basePrice(), existing.quantity(),
                    selected, batteries[bIdx], colors[cIdx]));

            CartDto cart = cartService.addProduct(currentCustomerId, productId, ProductType.SMARTPHONE, qty);
            print(String.format("Added to cart! Cart total: %.2f PLN", cart.totalPrice().doubleValue()));
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    private void addElectronicsToCart() {
        print("Enter product ID: ");
        long productId = readLong();
        print("Quantity: ");
        int qty = readInt();
        try {
            CartDto cart = cartService.addProduct(currentCustomerId, productId, ProductType.ELECTRONICS, qty);
            print(String.format("Added to cart! Cart total: %.2f PLN", cart.totalPrice().doubleValue()));
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    private void removeProductFromCart() {
        viewCart();
        CartDto cart = cartService.getCart(currentCustomerId);
        if (cart.items().isEmpty()) return;

        print("Product type (COMPUTER / SMARTPHONE / ELECTRONICS): ");
        String typeStr = scanner.nextLine().trim().toUpperCase();
        ProductType type;
        try {
            type = ProductType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            print(exHandler.handleAny(e));
            return;
        }

        print("Product ID to remove: ");
        long productId = readLong();

        try {
            cartService.removeProduct(currentCustomerId, productId, type);
            print("Product removed from cart.");
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }


    private void placeOrder() {
        viewCart();
        CartDto cart = cartService.getCart(currentCustomerId);
        if (cart.items().isEmpty()) {
            print("Cart is empty. Add products before ordering.");
            return;
        }

        print("\nDiscount code? (press Enter to skip): ");
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

        print("\nConfirm order? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
            print("Cancelled.");
            return;
        }

        try {
            print("\nProcessing your order");
            InvoiceDto invoice = orderFacade.processOrderAsync(currentCustomerId, confirmedCode).join();
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

    private void printInvoice(InvoiceDto invoice) {
        print("\n" + DLINE);
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

    private void viewOrders() {
        try {
            List<OrderDto> orders = orderService.getOrdersByCustomer(currentCustomerId);
            print("\n" + LINE);
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


    private void showDiscounts() {
        print("\n" + LINE);
        print("  ACTIVE PROMOTIONS");
        print(LINE);
        try {
            List<DiscountDto> active = discountService.getAllActive();
            if (active.isEmpty()) {
                print("  No active promotions.");
            } else {
                active.forEach(d -> {
                    String val = d.type().name().equals("PERCENTAGE")
                            ? d.value().stripTrailingZeros().toPlainString() + "%  off"
                            : d.value().toPlainString() + " PLN  off";
                    print(String.format("  [%-12s]  %-35s  -%s", d.code(), d.description(), val));
                    if (d.minOrderValue() != null && d.minOrderValue().compareTo(BigDecimal.ZERO) > 0)
                        print(String.format("               Min. order: %.2f PLN", d.minOrderValue().doubleValue()));
                    print(String.format("               Valid until: %s",
                            d.validTo().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                    print("");
                });
            }
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
        print(LINE);
    }


    /**
     * Admin-style feature: checks out the carts of several customers at once,
     * processing them concurrently via {@link OrderFacade#processBatchOrders(List)}
     * (backed by ConcurrentOrderProcessor and a fixed thread pool).
     * Failures for individual customers (e.g. empty cart, insufficient stock)
     * do not stop the rest of the batch — each customer gets their own result.
     */
    private void batchCheckout() {
        print("\n" + LINE);
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


    private void printBanner() {
        print("\n" + DLINE);
        print("                   ONLINE STORE");
        print(DLINE);
    }

    private void printMainMenu() {
        try {
            CustomerDto customer = customerService.getCustomerById(currentCustomerId);
            print("\n" + LINE);
            print("  Logged in as: " + customer.name() + "  (ID: " + currentCustomerId + ")");
        } catch (Exception e) {
            print("\n" + LINE);
        }
        print(LINE);
        print("  1. Browse Products");
        print("  2. View Cart");
        print("  3. Add Product to Cart");
        print("  4. Remove Product from Cart");
        print("  5. Place Order");
        print("  6. My Orders");
        print("  7. Promotions & Discounts");
        print("  8. Switch Customer");
        print("  9. Batch Checkout for Multiple Customers (Admin)");
        print("  0. Exit");
        print(LINE);
        System.out.print("  Choice > ");
    }

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private long readLong() {
        try {
            return Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private void print(String text) {
        System.out.println(text);
    }
}
