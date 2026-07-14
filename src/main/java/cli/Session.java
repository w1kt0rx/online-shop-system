package cli;

import lombok.Getter;

@Getter
public class Session {

    private Long currentCustomerId;

    public void setCurrentCustomerId(Long currentCustomerId) {
        this.currentCustomerId = currentCustomerId;
    }
}
