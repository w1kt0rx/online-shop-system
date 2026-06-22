package exception;

import lombok.Getter;

@Getter
public abstract class ShopException extends RuntimeException {
    private String errorCode = "";
    public ShopException(String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
