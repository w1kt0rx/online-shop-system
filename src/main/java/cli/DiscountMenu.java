package cli;

import discount.dto.DiscountDto;
import discount.service.DiscountService;
import exception.handler.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class DiscountMenu extends BaseMenu {
    private final DiscountService discountService;

    public DiscountMenu(
            Scanner scanner,
            Session session,
            GlobalExceptionHandler exHandler,
            DiscountService discountService
    ) {
        super(scanner, session, exHandler);
        this.discountService = discountService;
    }

    public void showDiscounts() {
        print(NL + LINE);
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
}