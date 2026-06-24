package product.service;

import exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import product.dto.computer.ComputerDto;
import product.dto.computer.CreateComputerRequest;
import product.dto.computer.UpdateComputerRequest;
import product.mapper.computer.ComputerMapper;
import product.model.Product;
import product.model.ProductType;
import product.model.computer.Computer;
import product.model.computer.configuration.ComputerConfiguration;
import product.repository.ComputerRepository;

import java.util.List;

@RequiredArgsConstructor
public class ComputerService {

    private final ComputerRepository computerRepository;

    public ComputerDto create(CreateComputerRequest request) {
        ComputerConfiguration configuration = new ComputerConfiguration();
        configuration.configure(
                request.processor(), request.ram(),
                request.storageType(), request.graphicsCard());

        Computer computer = new Computer(
                null,
                request.name(),
                request.basePrice(),
                request.quantity(),
                configuration);

        return ComputerMapper.toDTO(computerRepository.save(computer));
    }

    public ComputerDto update(Long id, UpdateComputerRequest request) {
        Computer computer = computerRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(ProductType.COMPUTER, id));

        computer.updateName(request.name());
        computer.updatePrice(request.basePrice());
        computer.updateQuantity(request.quantity());
        computer.getComputerConfiguration().configure(
                request.processor(),
                request.ram(),
                request.storageType(),
                request.graphicsCard()
        );

        return ComputerMapper.toDTO(computerRepository.save(computer));
    }

    public ComputerDto getById(Long id) {
        return computerRepository.findById(id)
                .map(ComputerMapper::toDTO)
                .orElseThrow(() -> new ProductNotFoundException(ProductType.COMPUTER, id));
    }

    public void delete(Long id) {
        computerRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(ProductType.COMPUTER, id));

        computerRepository.delete(id);
    }

    public List<ComputerDto> getAll() {
        return computerRepository.getAll().stream()
                .map(ComputerMapper::toDTO)
                .toList();
    }
}
