package customer.repository;

import customer.model.Customer;
import product.repository.Repository;

import java.util.Optional;

public interface CustomerRepository extends Repository<Customer> {

    /**
     * Looks up a customer by their login email (case-insensitive —
     * implementations should normalise the same way {@link Customer} does).
     *
     * @param email email address to search for
     * @return the matching customer, or empty if no customer has that email
     */
    Optional<Customer> findByEmail(String email);
}