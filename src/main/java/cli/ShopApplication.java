package cli;

import cart.service.CartService;
import common.time.TimeUtils;
import customer.repository.impl.InMemoryCustomerRepository;
import customer.service.CustomerService;
import discount.dto.CreateDiscountRequest;
import discount.model.DiscountType;
import discount.repository.impl.InMemoryDiscountRepository;
import discount.service.DiscountService;
import exception.handler.GlobalExceptionHandler;
import invoice.repository.impl.InMemoryInvoiceRepository;

import invoice.service.InvoiceService;
import order.repository.OrderRepository;
import order.repository.impl.file.FileOrderRepository;
import order.facade.OrderFacade;
import order.service.AsyncOrderProcessor;
import order.service.ConcurrentOrderProcessor;
import order.service.OrderProcessor;
import order.service.OrderService;
import product.dto.computer.CreateComputerRequest;
import product.dto.electronics.CreateElectronicsRequest;
import product.dto.smartphone.CreateSmartphoneRequest;
import product.service.ProductService;
import product.model.computer.configuration.*;
import product.model.smartphone.configuration.*;
import product.repository.impl.InMemoryComputerRepository;
import product.repository.impl.InMemoryElectronicsRepository;
import product.repository.impl.InMemorySmartphoneRepository;
import product.service.ComputerService;
import product.service.ElectronicsService;
import product.service.SmartphoneService;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

public class ShopApplication {

    public static void main(String[] args) {

        var computerRepo = new InMemoryComputerRepository();
        var smartphoneRepo = new InMemorySmartphoneRepository();
        var electronicsRepo = new InMemoryElectronicsRepository();
        var customerRepo = new InMemoryCustomerRepository();
        var invoiceRepo = new InMemoryInvoiceRepository();
        var discountRepo = new InMemoryDiscountRepository();
        OrderRepository orderRepo = new FileOrderRepository("orders.json");

        var computerService = new ComputerService(computerRepo);
        var smartphoneService = new SmartphoneService(smartphoneRepo);
        var electronicsService = new ElectronicsService(electronicsRepo);
        var invoiceService = new InvoiceService(invoiceRepo);

        var cartService = new CartService(customerRepo, computerRepo, smartphoneRepo, electronicsRepo);
        var customerService = new CustomerService(customerRepo);
        var orderService = new OrderService(orderRepo, customerRepo);
        var discountService = new DiscountService(discountRepo);

        var orderProcessor = new OrderProcessor(orderRepo, customerRepo, discountService, invoiceService);
        var concurrentProcessor = new ConcurrentOrderProcessor(orderProcessor, 4);

        var asyncProcessor = new AsyncOrderProcessor(orderProcessor, 4);
        var exHandler = new GlobalExceptionHandler();

        var productFacade = new ProductService(computerService, smartphoneService, electronicsService);
        var orderFacade = new OrderFacade(concurrentProcessor, asyncProcessor);

        seedProducts(computerService, smartphoneService, electronicsService);
        seedDiscounts(discountService);

        new ShopCLI(productFacade, cartService, customerService,
                orderService, orderFacade, discountService, exHandler).start();

        orderFacade.shutdown();
    }

    private static void seedProducts(ComputerService cs, SmartphoneService ss, ElectronicsService es) {
        cs.create(CreateComputerRequest.of("Dell XPS 15", new BigDecimal("4500"), 10,
                Processor.INTEL_I7, Ram.RAM_16GB, StorageType.SSD_1TB, GraphicsCard.RTX_3050));
        cs.create(CreateComputerRequest.of("MacBook Pro M3", new BigDecimal("7000"), 5,
                Processor.INTEL_I9, Ram.RAM_32GB, StorageType.SSD_2TB, GraphicsCard.INTEGRATED));
        cs.create(CreateComputerRequest.of("Lenovo ThinkPad", new BigDecimal("3200"), 15,
                Processor.AMD_RYZEN_5, Ram.RAM_8GB, StorageType.SSD_512GB, GraphicsCard.INTEGRATED));

        ss.create(CreateSmartphoneRequest.of("iPhone 15 Pro", new BigDecimal("5500"), 20,
                Set.of(Accessory.CHARGER), BatteryCapacity.BATTERY_4000, Color.BLACK));
        ss.create(CreateSmartphoneRequest.of("Samsung Galaxy S24", new BigDecimal("4200"), 30,
                Set.of(), BatteryCapacity.BATTERY_5000, Color.WHITE));
        ss.create(CreateSmartphoneRequest.of("Xiaomi 14", new BigDecimal("3000"), 25,
                Set.of(Accessory.CABLE), BatteryCapacity.BATTERY_6000, Color.BLUE));

        es.create(CreateElectronicsRequest.of("Monitor LG 27\"", new BigDecimal("1800"), 12));
        es.create(CreateElectronicsRequest.of("Mechanical Keyboard", new BigDecimal("350"), 40));
        es.create(CreateElectronicsRequest.of("Sony WH-1000XM5", new BigDecimal("1200"), 18));
    }

    private static void seedDiscounts(DiscountService ds) {
        ZonedDateTime now = TimeUtils.now();
        ds.createDiscount(CreateDiscountRequest.of(
                "WELCOME10", "10% welcome discount",
                DiscountType.PERCENTAGE, new BigDecimal("10"),
                BigDecimal.ZERO, now, now.plusYears(1)));
        ds.createDiscount(CreateDiscountRequest.of(
                "SAVE200", "200 PLN off orders over 2000 PLN",
                DiscountType.FIXED_AMOUNT, new BigDecimal("200"),
                new BigDecimal("2000"), now, now.plusMonths(6)));
        ds.createDiscount(CreateDiscountRequest.of(
                "TECH25", "25% off — limited offer",
                DiscountType.PERCENTAGE, new BigDecimal("25"),
                BigDecimal.ZERO, now, now.plusWeeks(2)));
    }
}
