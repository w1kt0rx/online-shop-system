package exception.handler;

import common.time.TimeUtils;
import exception.ShopException;
import java.time.format.DateTimeFormatter;

/**
 * Centralised exception handler for the shop application.
 * Converts both domain ShopException instances and unexpected Exception
 * instances into a formatted, user-facing string. All errors are also logged to
 * System.err with a timestamp for diagnostics.
 * </p>
 * <p>
 * This class is designed to be used as a singleton and does not hold any mutable state.
 * </p>
 */
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Handles a known domain exception, logging it and returning a formatted error message.
     *
     * @param ex the domain exception; must not be null
     * @return formatted string in the form ERROR_CODE message
     */
    public String handler(ShopException ex) {
        log(ex);
        return formatMessage(ex.getErrorCode(), ex.getMessage());
    }

    /**
     * Handles an unexpected exception by logging the stack trace and
     * returning a generic user-facing message.
     *
     * @param ex the unexpected exception; must not be null
     * @return a generic error string to avoid leaking internal details
     */
    public String handleUnexpected(Exception ex) {
        logUnexpected(ex);
        return formatMessage("UNEXPECTED_ERROR", "An unexpected error occurred. Please try again");
    }

    /**
     * Dispatches to handler(ShopException) for domain exceptions or
     * handleUnexpected(Exception) for everything else.
     *
     * @param ex any exception
     * @return formatted error string appropriate for the exception type
     */
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
        System.err.printf("[%s] ERROR [%s]: %s%n", TimeUtils.now().format(FMT), ex.getErrorCode(), ex.getMessage());
    }

    private void logUnexpected(Exception ex) {
        System.err.printf("[%s] UNEXPECTED ERROR: %s%n", TimeUtils.now().format(FMT), ex.getMessage());
        ex.printStackTrace(System.err);
    }
}
