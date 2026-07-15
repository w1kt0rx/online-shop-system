package product.mapper.computer.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import product.dto.computer.configuration.ComputerConfigurationDto;
import product.model.computer.configuration.ComputerConfiguration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComputerConfigurationMapper {

    public static ComputerConfigurationDto toDto(ComputerConfiguration computerConfiguration) {
        return new ComputerConfigurationDto(
            computerConfiguration.getProcessor(),
            computerConfiguration.getRam(),
            computerConfiguration.getStorageType(),
            computerConfiguration.getGraphicsCard()
        );
    }

    public static ComputerConfiguration toEntity(ComputerConfigurationDto computerConfigurationDto) {
        ComputerConfiguration configuration = new ComputerConfiguration();
        configuration.configure(
            computerConfigurationDto.processor(),
            computerConfigurationDto.ram(),
            computerConfigurationDto.storageType(),
            computerConfigurationDto.graphicsCard()
        );
        return configuration;
    }
}
