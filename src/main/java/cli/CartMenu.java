package cli;

import cart.dto.CartDto;
import cart.service.CartService;
import exception.handler.GlobalExceptionHandler;
import java.util.Scanner;
import product.model.ProductType;

public class CartMenu extends BaseMenu {

    private final CartService cartService;

    public CartMenu(Scanner scanner, Session session, GlobalExceptionHandler exHandler, CartService cartService) {
        super(scanner, session, exHandler);
        this.cartService = cartService;
    }

    public void viewCart() {
        try {
            CartDto cart = cartService.getCart(session.getCurrentCustomerId());
            print(NL + LINE);
            print("  YOUR CART");
            print(LINE);
            if (cart.items().isEmpty()) {
                print("  Cart is empty.");
            } else {
                cart
                    .items()
                    .forEach(i ->
                        print(
                            String.format(
                                "  %-30s  x%-3d  %10.2f PLN",
                                i.productName(),
                                i.quantity(),
                                i.totalPrice().doubleValue()
                            )
                        )
                    );
                print(LINE);
                print(String.format("  %-34s  %10.2f PLN", "TOTAL:", cart.totalPrice().doubleValue()));
            }
            print(LINE);
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    public void removeProductFromCart() {
        viewCart();
        CartDto cart = cartService.getCart(session.getCurrentCustomerId());
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
            cartService.removeProduct(session.getCurrentCustomerId(), productId, type);
            print("Product removed from cart.");
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }
}
