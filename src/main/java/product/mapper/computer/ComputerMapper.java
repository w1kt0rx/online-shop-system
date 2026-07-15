package product.mapper.computer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import product.dto.computer.ComputerDto;
import product.mapper.computer.configuration.ComputerConfigurationMapper;
import product.model.computer.Computer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComputerMapper {

    public static ComputerDto toDTO(Computer computer) {
        return new ComputerDto(
            computer.getId(),
            computer.getName(),
            computer.getBasePrice(),
            computer.getPrice(),
            computer.getQuantity(),
            computer.getProductType(),
            ComputerConfigurationMapper.toDto(computer.getComputerConfiguration())
        );
    }

    public static Computer toEntity(ComputerDto computerDto) {
        return new Computer(
            computerDto.id(),
            computerDto.name(),
            computerDto.basePrice(),
            computerDto.quantity(),
            ComputerConfigurationMapper.toEntity(computerDto.computerConfiguration())
        );
    }
}
