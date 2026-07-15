package customer.service;

import customer.dto.ChangePasswordRequest;
import customer.dto.CreateCustomerRequest;
import customer.dto.CustomerDto;
import customer.dto.LoginRequest;
import customer.dto.UpdateCustomerRequest;
import customer.mapper.CustomerMapper;
import customer.model.Customer;
import customer.repository.CustomerRepository;
import customer.validator.CustomerValidator;
import exception.CustomerNotFoundException;
import exception.EmailAlreadyInUseException;
import exception.InvalidCredentialsException;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Registers a new customer. The email must be unique across all
     * customers; the password is hashed before it ever touches storage.
     *
     * @param request name, email and plaintext password
     * @return the created customer (no password hash exposed)
     * @throws EmailAlreadyInUseException if the email is already registered
     */
    public CustomerDto createCustomer(CreateCustomerRequest request) {
        customerRepository
            .findByEmail(request.email())
            .ifPresent(existing -> {
                throw new EmailAlreadyInUseException(request.email());
            });

        Customer customer = new Customer(null, request.name(), request.email(), request.password());
        return CustomerMapper.toDto(customerRepository.save(customer));
    }

    /**
     * Authenticates a customer by email and password.
     *
     * <p>Deliberately returns the same {@link InvalidCredentialsException}
     * whether the email is unknown or the password is wrong — this avoids
     * leaking which emails are registered (user enumeration).</p>
     *
     * @param request email + plaintext password supplied at login
     * @return the authenticated customer
     * @throws InvalidCredentialsException if the email is unknown or the password is wrong
     */
    public CustomerDto login(LoginRequest request) {
        Customer customer = customerRepository
            .findByEmail(request.email())
            .orElseThrow(InvalidCredentialsException::new);

        if (!customer.checkPassword(request.password())) {
            throw new InvalidCredentialsException();
        }
        return CustomerMapper.toDto(customer);
    }

    public CustomerDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
        return CustomerMapper.toDto(customer);
    }

    public List<CustomerDto> getAllCustomers() {
        return customerRepository.getAll().stream().map(CustomerMapper::toDto).toList();
    }

    /**
     * Updates name and/or email. If the email changes, the new value must
     * not already belong to another customer.
     */
    public CustomerDto updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));

        if (!customer.getEmail().equalsIgnoreCase(request.email())) {
            customerRepository
                .findByEmail(request.email())
                .ifPresent(existing -> {
                    throw new EmailAlreadyInUseException(request.email());
                });
        }

        customer.updateName(request.name());
        customer.updateEmail(request.email());
        return CustomerMapper.toDto(customerRepository.save(customer));
    }

    /**
     * Changes a customer's password after verifying the current one.
     *
     * @param id      the customer changing their password
     * @param request current password (for verification) + new password
     * @throws InvalidCredentialsException if currentPassword does not match
     */
    public CustomerDto changePassword(Long id, ChangePasswordRequest request) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));

        if (!customer.checkPassword(request.currentPassword())) {
            throw new InvalidCredentialsException();
        }
        customer.changePassword(request.newPassword());
        return CustomerMapper.toDto(customerRepository.save(customer));
    }

    public void deleteCustomer(Long id) {
        customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
        customerRepository.delete(id);
    }
}
