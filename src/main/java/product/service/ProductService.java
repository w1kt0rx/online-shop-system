package product.service;

import exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import product.dto.*;
import product.mapper.ComputerMapper;
import product.mapper.ElectronicsMapper;
import product.mapper.SmartphoneMapper;
import product.model.computer.Computer;
import product.model.computer.configuration.ComputerConfiguration;
import product.model.electronics.Electronics;
import product.model.smartphone.Smartphone;
import product.model.smartphone.configuration.SmartphoneConfiguration;
import product.repository.impl.InMemoryComputerRepository;
import product.repository.impl.InMemoryElectronicsRepository;
import product.repository.impl.InMemorySmartphoneRepository;

import java.util.List;

@RequiredArgsConstructor
public class ProductService {

    private final InMemoryComputerRepository computerRepository;
    private final InMemorySmartphoneRepository smartphoneRepository;
    private final InMemoryElectronicsRepository electronicsRepository;

    public ComputerDto createComputer(CreateComputerRequest request) {
        Computer computer = new Computer(
                computerRepository.getNextId(),
                request.name(),
                request.basePrice(),
                request.quantity(),
                new ComputerConfiguration());

        return ComputerMapper.toDTO(computerRepository.save(computer));
    }

    public ComputerDto updateComputer(Long id, UpdateComputerRequest request) {
        computerRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Computer with id " + id + " not found"));

        ComputerConfiguration configuration = new ComputerConfiguration();
        configuration.configure(
                request.processor(),
                request.ram(),
                request.storageType(),
                request.graphicsCard()
        );

        Computer updatedComputer = new Computer(
                id,
                request.name(),
                request.basePrice(),
                request.quantity(),
                configuration
        );
        return ComputerMapper.toDTO(computerRepository.save(updatedComputer));
    }

    public ComputerDto getComputerById(Long id) {
        Computer computer = computerRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Computer not found"));
        return ComputerMapper.toDTO(computer);
    }

    public List<ComputerDto> getAllComputers() {
        return computerRepository.getAll()
                .stream()
                .map(ComputerMapper::toDTO)
                .toList();
    }

    public void deleteComputer(Long id) {
        computerRepository.delete(id);
    }

    public SmartphoneDto createSmartphone(CreateSmartphoneRequest request) {
        Smartphone smartphone = new Smartphone(
                smartphoneRepository.getNextId(),
                request.name(),
                request.basePrice(),
                request.quantity(),
                new SmartphoneConfiguration());

        return SmartphoneMapper.toDTO(smartphoneRepository.save(smartphone));
    }

    public SmartphoneDto updateSmartphone(Long id, UpdateSmartphoneRequest request) {
        smartphoneRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Smartphone with id " + id + " not found"));

        SmartphoneConfiguration configuration = new SmartphoneConfiguration();
        configuration.configure(
                request.color(),
                request.batteryCapacity(),
                request.accessory()
        );
        Smartphone updatedSmartphone = new Smartphone(
                id,
                request.name(),
                request.basePrice(),
                request.quantity(),
                configuration
        );
        return SmartphoneMapper.toDTO(smartphoneRepository.save(updatedSmartphone));
    }

    public SmartphoneDto getSmartphoneById(Long id) {
        Smartphone smartphone = smartphoneRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Smartphone not found"));
        return SmartphoneMapper.toDTO(smartphone);
    }

    public List<SmartphoneDto> getAllSmartphones() {
        return smartphoneRepository.getAll()
                .stream()
                .map(SmartphoneMapper::toDTO)
                .toList();
    }

    public void deleteSmartphone(Long id) {
        smartphoneRepository.delete(id);
    }

    public ElectronicsDto createElectronics(CreateElectronicsRequest request) {
        Electronics electronics = new Electronics(
                electronicsRepository.getNextId(),
                request.name(),
                request.basePrice(),
                request.quantity());

        return ElectronicsMapper.toDTO(electronicsRepository.save(electronics));
    }

    public ElectronicsDto updateElectronics(Long id, UpdateElectronicsRequest request) {
        electronicsRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Electronics with id " + id + " not found"));

        Electronics updatedElectronics = new Electronics(
                id,
                request.name(),
                request.basePrice(),
                request.quantity()
        );
        return ElectronicsMapper.toDTO(electronicsRepository.save(updatedElectronics));
    }

    public ElectronicsDto getElectronicsById(Long id) {
        Electronics electronics = electronicsRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Electronics not found"));
        return ElectronicsMapper.toDTO(electronics);
    }

    public List<ElectronicsDto> getAllElectronics() {
        return electronicsRepository.getAll()
                .stream()
                .map(ElectronicsMapper::toDTO)
                .toList();
    }

    public void deleteElectronics(Long id) {
        electronicsRepository.delete(id);
    }
}