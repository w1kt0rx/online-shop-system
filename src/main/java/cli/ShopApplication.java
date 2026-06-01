package cli;

import cart.service.CartService;
import customer.repository.impl.InMemoryCustomerRepository;
import customer.service.CustomerService;
import invoice.repository.impl.InMemoryInvoiceRepository;
import order.repository.impl.InMemoryOrderRepository;
import order.service.OrderProcessor;
import order.service.OrderService;
import product.dto.CreateComputerRequest;
import product.dto.CreateElectronicsRequest;
import product.dto.CreateSmartphoneRequest;
import product.model.computer.configuration.GraphicsCard;
import product.model.computer.configuration.Processor;
import product.model.computer.configuration.Ram;
import product.model.computer.configuration.StorageType;
import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;
import product.repository.impl.InMemoryComputerRepository;
import product.repository.impl.InMemoryElectronicsRepository;
import product.repository.impl.InMemorySmartphoneRepository;
import product.service.ProductService;

import java.math.BigDecimal;
import java.util.Set;

public class ShopApplication {

    public static void main(String[] args) {
        var computerRepo    = new InMemoryComputerRepository();
        var smartphoneRepo  = new InMemorySmartphoneRepository();
        var electronicsRepo = new InMemoryElectronicsRepository();
        var customerRepo    = new InMemoryCustomerRepository();
        var orderRepo       = new InMemoryOrderRepository();
        var invoiceRepo     = new InMemoryInvoiceRepository();

        var productService  = new ProductService(computerRepo, smartphoneRepo, electronicsRepo);
        var cartService     = new CartService(customerRepo, computerRepo, smartphoneRepo, electronicsRepo);
        var customerService = new CustomerService(customerRepo);
        var orderService    = new OrderService(orderRepo, customerRepo);
        var orderProcessor  = new OrderProcessor(orderRepo, customerRepo, invoiceRepo);

        seedProducts(productService);

        new ShopCLI(productService, cartService, customerService, orderService, orderProcessor).start();
    }

    private static void seedProducts(ProductService service) {
        service.createComputer(new CreateComputerRequest(
                "Dell XPS 15", new BigDecimal("4500"), 10,
                Processor.INTEL_I7, Ram.RAM_16GB, StorageType.SSD_1TB, GraphicsCard.RTX_3050));

        service.createComputer(new CreateComputerRequest(
                "MacBook Pro M3", new BigDecimal("7000"), 5,
                Processor.INTEL_I9, Ram.RAM_32GB, StorageType.SSD_2TB, GraphicsCard.INTEGRATED));

        service.createComputer(new CreateComputerRequest(
                "Lenovo ThinkPad", new BigDecimal("3200"), 15,
                Processor.AMD_RYZEN_5, Ram.RAM_8GB, StorageType.SSD_512GB, GraphicsCard.INTEGRATED));

        service.createSmartphone(new CreateSmartphoneRequest(
                "iPhone 15 Pro", new BigDecimal("5500"), 20,
                Set.of(Accessory.CHARGER), BatteryCapacity.BATTERY_4000, Color.BLACK));

        service.createSmartphone(new CreateSmartphoneRequest(
                "Samsung Galaxy S24", new BigDecimal("4200"), 30,
                Set.of(), BatteryCapacity.BATTERY_5000, Color.WHITE));

        service.createSmartphone(new CreateSmartphoneRequest(
                "Xiaomi 14", new BigDecimal("3000"), 25,
                Set.of(Accessory.CABLE), BatteryCapacity.BATTERY_6000, Color.BLUE));

        service.createElectronics(new CreateElectronicsRequest(
                "Monitor", new BigDecimal("1800"), 12));

        service.createElectronics(new CreateElectronicsRequest(
                "Keyboard", new BigDecimal("350"), 40));

        service.createElectronics(new CreateElectronicsRequest(
                "Headphones", new BigDecimal("1200"), 18));
    }
}