package product.model.computer.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum StorageType {
    SSD_512GB("SSD 512GB", new BigDecimal("300")),
    SSD_1TB("SSD 1TB", new BigDecimal("500")),
    SSD_2TB("SSD 2TB", new BigDecimal("900")),
    HDD_1TB("HDD 1TB", new BigDecimal("100")),
    HDD_2TB("HDD 2TB", new BigDecimal("180"));

    private final String description;
    private final BigDecimal price;

}
