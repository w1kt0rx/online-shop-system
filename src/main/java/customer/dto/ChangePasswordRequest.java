package customer.dto;

/** Payload for changing an authenticated customer's password. */
public record ChangePasswordRequest(String currentPassword, String newPassword) {}
