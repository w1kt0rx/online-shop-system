package product.mapper.smartphone.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import product.dto.smartphone.configuration.SmartphoneConfigurationDto;
import product.model.smartphone.configuration.SmartphoneConfiguration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SmartphoneConfigurationMapper {

    public static SmartphoneConfigurationDto toDto(SmartphoneConfiguration smartphoneConfiguration) {
        return new SmartphoneConfigurationDto(
            smartphoneConfiguration.getColor(),
            smartphoneConfiguration.getBatteryCapacity(),
            smartphoneConfiguration.getAccessories()
        );
    }

    public static SmartphoneConfiguration toEntity(SmartphoneConfigurationDto smartphoneConfigurationDto) {
        SmartphoneConfiguration configuration = new SmartphoneConfiguration();
        configuration.configure(
            smartphoneConfigurationDto.color(),
            smartphoneConfigurationDto.batteryCapacity(),
            smartphoneConfigurationDto.accessories()
        );
        return configuration;
    }
}
