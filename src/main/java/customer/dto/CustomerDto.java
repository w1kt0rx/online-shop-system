package customer.dto;

import cart.dto.CartDto;

/**
 * Customer view returned to callers. Deliberately excludes the password
 * hash — it is never serialised or sent back to the client.
 */
public record CustomerDto(
        Long id,
        String name,
        String email,
        CartDto cart
) {
}