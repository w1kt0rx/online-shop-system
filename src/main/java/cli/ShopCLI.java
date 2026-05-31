package cli;

import cart.dto.CartDto;
import cart.dto.CartItemDto;
import cart.service.CartService;
import customer.dto.CreateCustomerRequest;
import customer.dto.CustomerDto;
import customer.service.CustomerService;
import exception.*;
import invoice.dto.InvoiceDto;
import order.dto.OrderDto;
import order.service.OrderProcessor;
import order.service.OrderService;
import product.dto.ComputerDto;
import product.dto.CreateComputerRequest;
import product.dto.CreateElectronicsRequest;
import product.dto.CreateSmartphoneRequest;
import product.dto.ElectronicsDto;
import product.dto.SmartphoneDto;
import product.model.ProductType;
import product.model.computer.configuration.GraphicsCard;
import product.model.computer.configuration.Processor;
import product.model.computer.configuration.Ram;
import product.model.computer.configuration.StorageType;
import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;
import product.service.ProductService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ShopCLI {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String LINE = "─".repeat(55);

    private final Scanner scanner;
    private final ProductService productService;
    private final CartService cartService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final OrderProcessor orderProcessor;

    private Long currentCustomerId = null;

    public ShopCLI(ProductService productService,
                   CartService cartService,
                   CustomerService customerService,
                   OrderService orderService,
                   OrderProcessor orderProcessor) {
        this.scanner = new Scanner(System.in);
        this.productService = productService;
        this.cartService = cartService;
        this.customerService = customerService;
        this.orderService = orderService;
        this.orderProcessor = orderProcessor;
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
                case 7 -> switchCustomer();
                case 0 -> running = false;
                default -> print("Wrong option, try again.");
            }
        }
        print("\nThank you.");
    }

    // ─── Login / Register ────────────────────────────────────────────

    private void loginOrRegister() {
        print("\n" + LINE);
        print("  1. Login (type client's ID)");
        print("  2. Sign up");
        print(LINE);
        int choice = readInt();

        if (choice == 1) {
            print("Type in client's id: ");
            long id = readLong();
            try {
                CustomerDto customer = customerService.getCustomerById(id);
                currentCustomerId = customer.id();
                print("Logged in as: " + customer.name());
            } catch (CustomerNotFoundException e) {
                print("Couldn't find client with id:  " + id + ". Sign up.");
                registerCustomer();
            }
        } else {
            registerCustomer();
        }
    }

    private void registerCustomer() {
        print("Type in name and surname: ");
        String name = scanner.nextLine().trim();
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest(name));
        currentCustomerId = customer.id();
        print("Registered! Your id: " + customer.id());
    }

    private void switchCustomer() {
        loginOrRegister();
    }

    // ─── Browse products ─────────────────────────────────────────────

    private void browseProducts() {
        print("\n" + LINE);
        print("  Browse Products");
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
        List<ComputerDto> list = productService.getAllComputers();
        if (list.isEmpty()) {
            print("No computers in the offer.");
            return;
        }
        print("\n  Computers:");
        print(LINE);
        list.forEach(c -> {
            print(String.format("  [ID:%d] %s", c.id(), c.name()));
            print(String.format("         Price: %.2f pln  |  Stock: %d pcs.", c.totalPrice().doubleValue(), c.quantity()));
            ComputerConfigLine(c);
            print("");
        });
    }

    private void ComputerConfigLine(ComputerDto c) {
        if (c.computerConfiguration() != null) {
            var cfg = c.computerConfiguration();
            print(String.format("         %s | %dGB RAM | %s | %s",
                    cfg.processor() != null ? cfg.processor().getDescription() : "-",
                    cfg.ram() != null ? cfg.ram().getCapacity() : 0,
                    cfg.storageType() != null ? cfg.storageType().getDescription() : "-",
                    cfg.graphicsCard() != null ? cfg.graphicsCard().getDescription() : "-"));
        }
    }

    private void listSmartphones() {
        List<SmartphoneDto> list = productService.getAllSmartphones();
        if (list.isEmpty()) {
            print("No smartphones in the offer.");
            return;
        }
        print("\n  SMARTPHONES:");
        print(LINE);
        list.forEach(s -> {
            print(String.format("  [ID:%d] %s", s.id(), s.name()));
            print(String.format("         Price: %.2f pln  |  Stock: %d pcs.", s.totalPrice().doubleValue(), s.quantity()));
        });
    }

    private void listElectronics() {
        List<ElectronicsDto> list = productService.getAllElectronics();
        if (list.isEmpty()) {
            print("No electronics in the offer.");
            return;
        }
        print("\n  Electronics:");
        print(LINE);
        list.forEach(e -> print(String.format("  [ID:%d] %-30s  %.2f pln  (stock: %d)",
                e.id(), e.name(), e.basePrice().doubleValue(), e.quantity())));
    }

    // ─── Cart ────────────────────────────────────────────────────────

    private void viewCart() {
        CartDto cart = cartService.getCart(currentCustomerId);
        print("\n" + LINE);
        print("  Your Cart");
        print(LINE);
        if (cart.items().isEmpty()) {
            print("  Cart is empty.");
        } else {
            cart.items().forEach(i -> print(String.format(
                    "  %-28s  x%d  =  %.2f pln",
                    i.productName(), i.quantity(), i.totalPrice().doubleValue())));
            print(LINE);
            print(String.format("  PRICE:  %.2f pln", cart.totalPrice().doubleValue()));
        }
        print(LINE);
    }

    private void addProductToCart() {
        print("\n  Choose product type:");
        print("  1. Computer   2. Smartphone   3. Electronics");
        int typeChoice = readInt();

        ProductType type = switch (typeChoice) {
            case 1 -> {
                listComputers();
                yield ProductType.COMPUTER;
            }
            case 2 -> {
                listSmartphones();
                yield ProductType.SMARTPHONE;
            }
            case 3 -> {
                listElectronics();
                yield ProductType.ELECTRONICS;
            }
            default -> {
                print("Invalid type.");
                yield null;
            }
        };
        if (type == null) return;

        // Optional: configure before adding (Computer / Smartphone)
        if (type == ProductType.COMPUTER) {
            configureComputerBeforeAdd(type);
            return;
        }
        if (type == ProductType.SMARTPHONE) {
            configureSmartphoneBeforeAdd(type);
            return;
        }

        print("Type product's ID: ");
        long productId = readLong();
        print("Type in amount: ");
        int qty = readInt();

        try {
            CartDto cart = cartService.addProduct(currentCustomerId, productId, type, qty);
            print("✅  Added to the cart! Cart's price: " + cart.totalPrice() + " pln");
        } catch (Exception e) {
            print(e.getMessage());
        }
    }

    private void configureComputerBeforeAdd(ProductType type) {
        print("Type in computer's ID: ");
        long productId = readLong();

        print("\n  Choose processor:");
        Processor[] processors = Processor.values();
        for (int i = 0; i < processors.length; i++)
            print(String.format("  %d. %s (+%.0f pln)", i + 1, processors[i].getDescription(), processors[i].getPrice().doubleValue()));
        Processor proc = processors[readInt() - 1];

        print("\n  Choose RAM:");
        Ram[] rams = Ram.values();
        for (int i = 0; i < rams.length; i++)
            print(String.format("  %d. %dGB (+%.0f pln)", i + 1, rams[i].getCapacity(), rams[i].getPrice().doubleValue()));
        Ram ram = rams[readInt() - 1];

        print("\n  Choose storage type:");
        StorageType[] storages = StorageType.values();
        for (int i = 0; i < storages.length; i++)
            print(String.format("  %d. %s (+%.0f pln)", i + 1, storages[i].getDescription(), storages[i].getPrice().doubleValue()));
        StorageType storage = storages[readInt() - 1];

        print("\n  Choose graphics card:");
        GraphicsCard[] gpus = GraphicsCard.values();
        for (int i = 0; i < gpus.length; i++)
            print(String.format("  %d. %s (+%.0f pln)", i + 1, gpus[i].getDescription(), gpus[i].getPrice().doubleValue()));
        GraphicsCard gpu = gpus[readInt() - 1];

        print("Type in amount: ");
        int qty = readInt();

        try {
            // Update configuration via ProductService, then add to cart
            productService.getComputerById(productId); // validate exists
            var updateReq = new product.dto.UpdateComputerRequest(
                    productService.getComputerById(productId).name(),
                    productService.getComputerById(productId).basePrice(),
                    productService.getComputerById(productId).quantity(),
                    proc, ram, storage, gpu
            );
            productService.updateComputer(productId, updateReq);
            CartDto cart = cartService.addProduct(currentCustomerId, productId, type, qty);
            print("Computer is configured and added to the cart! Price: " + cart.totalPrice() + " pln");
        } catch (Exception e) {
            print(e.getMessage());
        }
    }

    private void configureSmartphoneBeforeAdd(ProductType type) {
        print("Type in smartphone's id: ");
        long productId = readLong();

        print("\n  Choose color:");
        Color[] colors = Color.values();
        for (int i = 0; i < colors.length; i++)
            print(String.format("  %d. %s (+%.0f pln)", i + 1, colors[i].getDescription(), colors[i].getPrice().doubleValue()));
        Color color = colors[readInt() - 1];

        print("\n  Choose battery capacity:");
        BatteryCapacity[] batteries = BatteryCapacity.values();
        for (int i = 0; i < batteries.length; i++)
            print(String.format("  %d. %s (+%.0f pln)", i + 1, batteries[i].getDescription(), batteries[i].getPrice().doubleValue()));
        BatteryCapacity battery = batteries[readInt() - 1];

        print("\n Choose accessories (split with comma, e.g. 1,3 or 0 = blank):");
        Accessory[] accessories = Accessory.values();
        for (int i = 0; i < accessories.length; i++)
            print(String.format("  %d. %s (+%.0f pln)", i + 1, accessories[i].getDescription(), accessories[i].getPrice().doubleValue()));

        Set<Accessory> selectedAccessories = new HashSet<>();
        String input = scanner.nextLine().trim();
        if (!input.equals("0")) {
            for (String part : input.split(",")) {
                try {
                    int idx = Integer.parseInt(part.trim()) - 1;
                    if (idx >= 0 && idx < accessories.length)
                        selectedAccessories.add(accessories[idx]);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        print("Type amount: ");
        int qty = readInt();

        try {
            var existing = productService.getSmartphoneById(productId);
            var updateReq = new product.dto.UpdateSmartphoneRequest(
                    existing.name(),
                    existing.basePrice(),
                    existing.quantity(),
                    selectedAccessories,
                    battery,
                    color
            );
            productService.updateSmartphone(productId, updateReq);
            CartDto cart = cartService.addProduct(currentCustomerId, productId, type, qty);
            print("Smartphone configured and added to the cart! Price: " + cart.totalPrice() + " pln");
        } catch (Exception e) {
            print(e.getMessage());
        }
    }

    private void removeProductFromCart() {
        viewCart();
        CartDto cart = cartService.getCart(currentCustomerId);
        if (cart.items().isEmpty()) return;

        print("Type product type (COMPUTER / SMARTPHONE / ELECTRONICS): ");
        String typeStr = scanner.nextLine().trim().toUpperCase();
        ProductType type;
        try {
            type = ProductType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            print("Illegal product type");
            return;
        }

        print("Type ID of product you want to delete: ");
        long productId = readLong();

        try {
            cartService.removeProduct(currentCustomerId, productId, type);
            print("Product deleted from cart.");
        } catch (Exception e) {
            print(e.getMessage());
        }
    }

    // ─── Order ───────────────────────────────────────────────────────

    private void placeOrder() {
        viewCart();
        CartDto cart = cartService.getCart(currentCustomerId);
        if (cart.items().isEmpty()) {
            print("Cart is empty. Add product before proceeding.");
            return;
        }

        print("\nAre you sure you want to place order?(y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y")) {
            print("Canceled.");
            return;
        }

        try {
            InvoiceDto invoice = orderProcessor.processOrder(currentCustomerId);
            printInvoice(invoice);
        } catch (Exception e) {
            print("Error during placing an order: " + e.getMessage());
        }
    }

    private void printInvoice(InvoiceDto invoice) {
        print("\n" + "═".repeat(55));
        print("         INVOICE / ORDER CONFIRMATION");
        print("═".repeat(55));
        print("  Invoice No.:    " + invoice.id());
        print("  Order No.:      " + invoice.orderId());
        print("  Customer:       " + invoice.customerName() + " (ID: " + invoice.customerId() + ")");
        print("  Date:           " + invoice.issuedAt().format(DATE_FMT));
        print(LINE);

        print(String.format("  %-28s  %5s  %10s", "Product", "Qty", "Amount"));
        print(LINE);

        invoice.items().forEach(item -> print(String.format(
                "  %-28s  %5d  %10.2f PLN",
                item.productName(),
                item.quantity(),
                item.totalPrice().doubleValue()
        )));

        print(LINE);
        print(String.format(
                "  %-35s  %10.2f PLN",
                "TOTAL:",
                invoice.totalAmount().doubleValue()
        ));

        print("═".repeat(55));
        print("  Thank you for your purchase!");
        print("═".repeat(55));
    }

    private void viewOrders() {
        List<OrderDto> orders = orderService.getOrdersByCustomer(currentCustomerId);

        print("\n" + LINE);
        print("  YOUR ORDERS");
        print(LINE);

        if (orders.isEmpty()) {
            print("  No orders found.");
        } else {
            orders.forEach(o -> {
                print(String.format(
                        "  Order #%d | Status: %s | Amount: %.2f PLN | %s",
                        o.id(),
                        o.orderStatus(),
                        o.totalPrice().doubleValue(),
                        o.createdAt().format(DATE_FMT)
                ));

                o.items().forEach(item -> print(String.format(
                        "    - %-28s x%d  %.2f PLN",
                        item.productName(),
                        item.quantity(),
                        item.totalPrice().doubleValue()
                )));

                print("");
            });
        }

        print(LINE);
    }

    private void printBanner() {
        print("\n" + "═".repeat(55));
        print("               ONLINE STORE");
        print("═".repeat(55));
    }

    private void printMainMenu() {
        CustomerDto customer = customerService.getCustomerById(currentCustomerId);

        print("\n" + LINE);
        print("  Logged in as: " + customer.name() + " (ID: " + currentCustomerId + ")");
        print(LINE);
        print("  1. Browse Products");
        print("  2. View Cart");
        print("  3. Add Product to Cart");
        print("  4. Remove Product from Cart");
        print("  5. Place Order");
        print("  6. My Orders");
        print("  7. Switch Customer");
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
