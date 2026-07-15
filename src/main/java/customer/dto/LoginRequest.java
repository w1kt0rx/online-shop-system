package customer.dto;

/** Login payload: email + plaintext password supplied at the prompt. */
public record LoginRequest(String email, String password) {}
