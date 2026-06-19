package cli;

import customer.dto.CreateCustomerRequest;
import customer.dto.CustomerDto;
import customer.service.CustomerService;
import exception.handler.GlobalExceptionHandler;

import java.util.Scanner;

public class CustomerMenu extends BaseMenu {

    private final CustomerService customerService;

    public CustomerMenu(Scanner scanner, Session session, GlobalExceptionHandler exHandler, CustomerService customerService) {
        super(scanner, session, exHandler);
        this.customerService = customerService;
    }

    public void loginOrRegister() {
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
                session.setCurrentCustomerId(customer.id());
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
            session.setCurrentCustomerId(customer.id());
            print("Registered! Your ID: " + customer.id() + " (save this to log in later)");
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    public void switchCustomer() {
        loginOrRegister();
    }
}