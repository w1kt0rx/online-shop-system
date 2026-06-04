package exception;

import lombok.Getter;

@Getter
public abstract class ShopException extends RuntimeException {
    private final String errorCode;
    public ShopException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
