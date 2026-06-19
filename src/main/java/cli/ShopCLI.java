package cli;

import cart.service.CartService;
import customer.dto.CustomerDto;
import customer.service.CustomerService;
import discount.service.DiscountService;
import exception.handler.GlobalExceptionHandler;
import order.facade.OrderFacade;
import order.service.OrderService;
import product.service.ProductService;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class ShopCLI {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String LINE = "─".repeat(55);
    private static final String DLINE = "═".repeat(55);
    private static final String NL = "\n";

    private final Scanner scanner = new Scanner(System.in);
    private final Session session;

    private final CustomerService customerService;
    private final CustomerMenu customerMenu;
    private final ProductMenu productMenu;
    private final CartMenu cartMenu;
    private final OrderMenu orderMenu;
    private final DiscountMenu discountMenu;

    public ShopCLI(ProductService productFacade,
                   CartService cartService,
                   CustomerService customerService,
                   OrderService orderService,
                   OrderFacade orderFacade,
                   DiscountService discountService,
                   GlobalExceptionHandler exHandler) {
        this.customerService = customerService;
        session = new Session();
        this.customerMenu =
                new CustomerMenu(
                        scanner,
                        session,
                        exHandler,
                        customerService
                );
        this.productMenu =
                new ProductMenu(
                        scanner,
                        session,
                        exHandler,
                        productFacade,
                        cartService
                );

        this.cartMenu =
                new CartMenu(
                        scanner,
                        session,
                        exHandler,
                        cartService
                );

        this.orderMenu =
                new OrderMenu(
                        scanner,
                        session,
                        exHandler,
                        orderService,
                        orderFacade,
                        discountService,
                        cartService,
                        cartMenu
                );

        this.discountMenu =
                new DiscountMenu(
                        scanner,
                        session,
                        exHandler,
                        discountService
                );

    }


    public void start() {
        printBanner();
        customerMenu.loginOrRegister();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt();
            switch (choice) {
                case 1 -> productMenu.browseProducts();
                case 2 -> cartMenu.viewCart();
                case 3 -> productMenu.addProductToCart();
                case 4 -> cartMenu.removeProductFromCart();
                case 5 -> orderMenu.placeOrder();
                case 6 -> orderMenu.viewOrders();
                case 7 -> discountMenu.showDiscounts();
                case 8 -> customerMenu.switchCustomer();
                case 9 -> orderMenu.batchCheckout();
                case 0 -> running = false;
                default -> print("Wrong option, try again.");
            }
        }
        print(NL + "Thank you. Goodbye!");
    }

    private void printBanner() {
        print(NL + DLINE);
        print("                   ONLINE STORE");
        print(DLINE);
    }

    private void printMainMenu() {
        try {
            CustomerDto customer = customerService.getCustomerById(session.getCurrentCustomerId());
            print(NL + LINE);
            print("  Logged in as: " + customer.name() + "  (ID: " + session.getCurrentCustomerId() + ")");
        } catch (Exception e) {
            print(NL + LINE);
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
    private void print(String text) {
        System.out.println(text);
    }
}
