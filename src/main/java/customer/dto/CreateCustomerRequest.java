package customer.dto;

/**
 * Registration payload. {@code password} is plaintext here only
 * transiently — it is hashed immediately inside {@link customer.model.Customer}
 * and never persisted or logged as-is.
 */
public record CreateCustomerRequest(String name, String email, String password) {}
