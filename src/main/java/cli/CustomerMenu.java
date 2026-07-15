package cli;

import customer.dto.CreateCustomerRequest;
import customer.dto.CustomerDto;
import customer.dto.LoginRequest;
import customer.service.CustomerService;
import exception.handler.GlobalExceptionHandler;
import java.util.Scanner;

public class CustomerMenu extends BaseMenu {

    private final CustomerService customerService;

    public CustomerMenu(
        Scanner scanner,
        Session session,
        GlobalExceptionHandler exHandler,
        CustomerService customerService
    ) {
        super(scanner, session, exHandler);
        this.customerService = customerService;
    }

    public void loginOrRegister() {
        print("\n" + LINE);
        print("  1. Login (email + password)");
        print("  2. Sign up as new customer");
        print(LINE);

        int choice = readInt();
        if (choice == 1) {
            login();
        } else {
            registerCustomer();
        }
    }

    private void login() {
        print("Email: ");
        String email = scanner.nextLine().trim();
        print("Password: ");
        String password = scanner.nextLine();

        try {
            CustomerDto customer = customerService.login(new LoginRequest(email, password));
            session.setCurrentCustomerId(customer.id());
            print("Logged in as: " + customer.name());
        } catch (Exception e) {
            print(exHandler.handleAny(e));
            print("\nNot registered yet?");
            print("  1. Try again");
            print("  2. Sign up instead");
            if (readInt() == 2) {
                registerCustomer();
            } else {
                login();
            }
        }
    }

    private void registerCustomer() {
        print("Name: ");
        String name = scanner.nextLine().trim();
        print("Email: ");
        String email = scanner.nextLine().trim();
        print("Password (min. 8 characters): ");
        String password = scanner.nextLine();

        try {
            CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest(name, email, password));
            session.setCurrentCustomerId(customer.id());
            print("Registered! Welcome, " + customer.name() + ".");
        } catch (Exception e) {
            print(exHandler.handleAny(e));
        }
    }

    public void switchCustomer() {
        loginOrRegister();
    }
}
