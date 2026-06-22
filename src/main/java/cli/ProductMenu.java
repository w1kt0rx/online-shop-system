package cli;

import cart.dto.CartDto;
import cart.service.CartService;
import exception.handler.GlobalExceptionHandler;
import product.dto.computer.ComputerDto;
import product.dto.computer.UpdateComputerRequest;
import product.dto.electronics.ElectronicsDto;
import product.dto.smartphone.SmartphoneDto;
import product.dto.smartphone.UpdateSmartphoneRequest;
import product.service.ProductService;
import product.model.ProductType;
import product.model.computer.configuration.GraphicsCard;
import product.model.computer.configuration.Processor;
import product.model.computer.configuration.Ram;
import product.model.computer.configuration.StorageType;
import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;

import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class ProductMenu extends BaseMenu {

    private final ProductService productFacade;
    private final CartService cartService;

    public ProductMenu(
            Scanner scanner,
            Session session,
            GlobalExceptionHandler exHandler,
            ProductService productFacade,
            CartService cartService
    ) {
        super(scanner, session, exHandler);

        this.productFacade = productFacade;
        this.cartService = cartService;
    }

    public void listElectronics() {
        try {
            List<ElectronicsDto> list = productFacade.getAllElectronics();
            if (list.isEmpty()) {
                print("No electronics in the offer.");
                return;
            }
            print(NL + "  ELECTRONICS:");
            print(LINE);
            list.forEach(e -> print(String.format("  [ID:%-2d]  %-30s  %.2f PLN  (stock: %d)",
                    e.id(), e.name(), e.basePrice().doubleValue(), e.quantity())));
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    public void listSmartphones() {
        try {
            List<SmartphoneDto> list = productFacade.getAllSmartphones();
            if (list.isEmpty()) {
                print("No smartphones in the offer.");
                return;
            }
            print(NL + "  SMARTPHONES:");
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

    private void listComputers() {
        try {
            List<ComputerDto> list = productFacade.getAllComputers();
            if (list.isEmpty()) {
                print("No computers in the offer.");
                return;
            }
            print(NL + "  COMPUTERS:");
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

    public void browseProducts() {
        print(NL + LINE);
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

    public void addProductToCart() {
        print(NL + "  Choose product type:");
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

    public void configureComputerAndAdd() {
        print("Enter computer ID: ");
        long productId = readLong();

        print(NL + "  Choose processor:");
        Processor[] processors = Processor.values();
        for (int i = 0; i < processors.length; i++)
            print(String.format("  %d. %-30s  +%.0f PLN", i + 1,
                    processors[i].getDescription(), processors[i].getPrice().doubleValue()));
        int pIdx = readInt() - 1;

        print(NL + "  Choose RAM:");
        Ram[] rams = Ram.values();
        for (int i = 0; i < rams.length; i++)
            print(String.format("  %d. %-4dGB  +%.0f PLN", i + 1,
                    rams[i].getCapacity(), rams[i].getPrice().doubleValue()));
        int rIdx = readInt() - 1;

        print(NL + "  Choose storage:");
        StorageType[] storages = StorageType.values();
        for (int i = 0; i < storages.length; i++)
            print(String.format("  %d. %-30s  +%.0f PLN", i + 1,
                    storages[i].getDescription(), storages[i].getPrice().doubleValue()));
        int sIdx = readInt() - 1;

        print(NL + "  Choose graphics card:");
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

            CartDto cart = cartService.addProduct(session.getCurrentCustomerId(), productId, ProductType.COMPUTER, qty);
            print(String.format("Added to cart! Cart total: %.2f PLN", cart.totalPrice().doubleValue()));
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    public void configureSmartphoneAndAdd() {
        print("Enter smartphone ID: ");
        long productId = readLong();

        print(NL + "  Choose color:");
        Color[] colors = Color.values();
        for (int i = 0; i < colors.length; i++)
            print(String.format("  %d. %-15s  +%.0f PLN", i + 1,
                    colors[i].getDescription(), colors[i].getPrice().doubleValue()));
        int cIdx = readInt() - 1;

        print(NL + "  Choose battery capacity:");
        BatteryCapacity[] batteries = BatteryCapacity.values();
        for (int i = 0; i < batteries.length; i++)
            print(String.format("  %d. %-20s  +%.0f PLN", i + 1,
                    batteries[i].getDescription(), batteries[i].getPrice().doubleValue()));
        int bIdx = readInt() - 1;

        print(NL + "  Choose accessories (comma-separated numbers, 0 = none):");
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

            CartDto cart = cartService.addProduct(session.getCurrentCustomerId(), productId, ProductType.SMARTPHONE, qty);
            print(String.format("Added to cart! Cart total: %.2f PLN", cart.totalPrice().doubleValue()));
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    public void addElectronicsToCart() {
        print("Enter product ID: ");
        long productId = readLong();
        print("Quantity: ");
        int qty = readInt();
        try {
            CartDto cart = cartService.addProduct(session.getCurrentCustomerId(), productId, ProductType.ELECTRONICS, qty);
            print(String.format("Added to cart! Cart total: %.2f PLN", cart.totalPrice().doubleValue()));
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }
}