package exception.handler;

import exception.ShopException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GlobalExceptionHandler {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String handler(ShopException ex) {
        log(ex);
        return formatMessage(ex.getErrorCode(), ex.getMessage());
    }

    public String handleUnexpected(Exception ex) {
        logUnexpected(ex);
        return formatMessage("UNEXPECTED_ERROR", "An unexpected error occured. Please try again");
    }

    public String handleAny(Exception ex) {
        if (ex instanceof ShopException shopException) {
            return handler(shopException);
        } else {
            return handleUnexpected(ex);
        }
    }

    private String formatMessage(String code, String message) {
        return String.format("[%s] %s", code, message);
    }

    private void log(ShopException ex) {
        System.err.printf("[%s] ERROR [%s]: %s%n",
                LocalDateTime.now().format(FMT),
                ex.getErrorCode(),
                ex.getMessage());
    }

    private void logUnexpected(Exception ex) {
        System.err.printf("[%s] UNEXPECTED ERROR: %s%n",
                LocalDateTime.now().format(FMT),
                ex.getMessage());
        ex.printStackTrace(System.err);
    }
}
