package cli;

import cart.service.CartService;
import customer.repository.impl.InMemoryCustomerRepository;
import customer.service.CustomerService;
import discount.dto.CreateDiscountRequest;
import discount.model.DiscountType;
import discount.repositoy.impl.InMemoryDiscountRepository;
import discount.service.DiscountService;
import exception.handler.GlobalExceptionHandler;
import invoice.repository.impl.InMemoryInvoiceRepository;
import java.util.List;
import order.repository.OrderRepository;
import order.repository.impl.InMemoryOrderRepository;
import order.repository.impl.file.FileOrderRepository;
import order.service.ConcurrentOrderProcessor;
import order.service.OrderProcessor;
import order.service.OrderService;
import product.facade.ProductFacade;
import product.dto.*;
import product.model.computer.configuration.*;
import product.model.smartphone.configuration.*;
import product.repository.impl.InMemoryComputerRepository;
import product.repository.impl.InMemoryElectronicsRepository;
import product.repository.impl.InMemorySmartphoneRepository;
import product.service.ComputerService;
import product.service.ElectronicsService;
import product.service.SmartphoneService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public class ShopApplication {


    public static void main(String[] args) {


        // ── Repozytoria ────────────────────────────────────────────────
        var computerRepo    = new InMemoryComputerRepository();
        var smartphoneRepo  = new InMemorySmartphoneRepository();
        var electronicsRepo = new InMemoryElectronicsRepository();
        var customerRepo    = new InMemoryCustomerRepository();
        var invoiceRepo     = new InMemoryInvoiceRepository();
        var discountRepo    = new InMemoryDiscountRepository();

        OrderRepository orderRepo = new FileOrderRepository("orders.json");

        // ── 3 dedykowane serwisy produktów (SRP) ──────────────────────
        var computerService    = new ComputerService(computerRepo);
        var smartphoneService  = new SmartphoneService(smartphoneRepo);
        var electronicsService = new ElectronicsService(electronicsRepo);

        // ── Pozostałe serwisy ──────────────────────────────────────────
        var cartService     = new CartService(customerRepo, computerRepo, smartphoneRepo, electronicsRepo);
        var customerService = new CustomerService(customerRepo);
        var orderService    = new OrderService(orderRepo, customerRepo);
        var discountService = new DiscountService(discountRepo);

        // Task 11 — ConcurrentOrderProcessor (pool 4 wątków)
        var orderProcessor      = new OrderProcessor(orderRepo, customerRepo, invoiceRepo, discountService);
        var concurrentProcessor = new ConcurrentOrderProcessor(orderProcessor, 4);

        // Task 9 — GlobalExceptionHandler
        var exHandler = new GlobalExceptionHandler();

        // Fasada — łączy 3 serwisy produktów + discount + concurrent
        var productFacade = new ProductFacade(
                computerService, smartphoneService, electronicsService,
                discountService, concurrentProcessor);

        // ── Dane przykładowe ───────────────────────────────────────────
        seedProducts(computerService, smartphoneService, electronicsService);
        seedDiscounts(discountService);

        // ── CLI ────────────────────────────────────────────────────────
        new ShopCLI(productFacade, cartService, customerService,
                orderService, orderProcessor, exHandler).start();
    }

    private static void seedProducts(ComputerService cs, SmartphoneService ss, ElectronicsService es) {
        cs.create(new CreateComputerRequest("Dell XPS 15",     new BigDecimal("4500"), 10,
                Processor.INTEL_I7,    Ram.RAM_16GB, StorageType.SSD_1TB,   GraphicsCard.RTX_3050));
        cs.create(new CreateComputerRequest("MacBook Pro M3",  new BigDecimal("7000"),  5,
                Processor.INTEL_I9,    Ram.RAM_32GB, StorageType.SSD_2TB,   GraphicsCard.INTEGRATED));
        cs.create(new CreateComputerRequest("Lenovo ThinkPad", new BigDecimal("3200"), 15,
                Processor.AMD_RYZEN_5, Ram.RAM_8GB,  StorageType.SSD_512GB, GraphicsCard.INTEGRATED));

        ss.create(new CreateSmartphoneRequest("iPhone 15 Pro",      new BigDecimal("5500"), 20,
                Set.of(Accessory.CHARGER), BatteryCapacity.BATTERY_4000, Color.BLACK));
        ss.create(new CreateSmartphoneRequest("Samsung Galaxy S24", new BigDecimal("4200"), 30,
                Set.of(),                  BatteryCapacity.BATTERY_5000, Color.WHITE));
        ss.create(new CreateSmartphoneRequest("Xiaomi 14",          new BigDecimal("3000"), 25,
                Set.of(Accessory.CABLE),   BatteryCapacity.BATTERY_6000, Color.BLUE));

        es.create(new CreateElectronicsRequest("Monitor LG 27\"",    new BigDecimal("1800"), 12));
        es.create(new CreateElectronicsRequest("Mechanical Keyboard", new BigDecimal("350"),  40));
        es.create(new CreateElectronicsRequest("Sony WH-1000XM5",    new BigDecimal("1200"), 18));
    }

    private static void seedDiscounts(DiscountService ds) {
        LocalDateTime now = LocalDateTime.now();
        ds.createDiscount(new CreateDiscountRequest(
                "WELCOME10", "10% welcome discount",
                DiscountType.PERCENTAGE,   new BigDecimal("10"),
                BigDecimal.ZERO, now, now.plusYears(1)));
        ds.createDiscount(new CreateDiscountRequest(
                "SAVE200", "200 PLN off orders over 2000 PLN",
                DiscountType.FIXED_AMOUNT, new BigDecimal("200"),
                new BigDecimal("2000"), now, now.plusMonths(6)));
        ds.createDiscount(new CreateDiscountRequest(
                "TECH25", "25% off — limited offer",
                DiscountType.PERCENTAGE,   new BigDecimal("25"),
                BigDecimal.ZERO, now, now.plusWeeks(2)));
    }
}
