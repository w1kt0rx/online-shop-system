package customer.service;

import customer.dto.CreateCustomerRequest;
import customer.dto.CustomerDto;
import customer.dto.UpdateCustomerRequest;
import customer.mapper.CustomerMapper;
import customer.model.Customer;
import customer.repository.CustomerRepository;
import exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerDto createCustomer(CreateCustomerRequest request) {
        Customer customer = new Customer(
                customerRepository.getNextId(),
                request.name()
        );
        return CustomerMapper.toDto(customerRepository.save(customer));
    }

    public CustomerDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return CustomerMapper.toDto(customer);
    }

    public List<CustomerDto> getAllCustomers() {
        return customerRepository.getAll().stream()
                .map(CustomerMapper::toDto)
                .toList();
    }

    public CustomerDto updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        customer.updateName(request.name());
        return CustomerMapper.toDto(customerRepository.save(customer));
    }

    public void deleteCustomer(Long id) {
        customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        customerRepository.delete(id);
    }
}