package product.service;

import exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import product.dto.CreateSmartphoneRequest;
import product.dto.SmartphoneDto;
import product.dto.UpdateSmartphoneRequest;
import product.mapper.SmartphoneMapper;
import product.model.smartphone.Smartphone;
import product.model.smartphone.configuration.SmartphoneConfiguration;
import product.repository.SmartphoneRepository;

import java.util.List;


@RequiredArgsConstructor
public class SmartphoneService {

    private final SmartphoneRepository smartphoneRepository;

    public SmartphoneDto create(CreateSmartphoneRequest request) {
        SmartphoneConfiguration configuration = new SmartphoneConfiguration();
        configuration.configure(
                request.color(), request.batteryCapacity(), request.accessory());

        Smartphone smartphone = new Smartphone(
                smartphoneRepository.getNextId(),
                request.name(),
                request.basePrice(),
                request.quantity(),
                configuration);

        return SmartphoneMapper.toDTO(smartphoneRepository.save(smartphone));
    }

    public SmartphoneDto update(Long id, UpdateSmartphoneRequest request) {
        Smartphone smartphone = smartphoneRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Smartphone with id " + id + " not found"));

        smartphone.updateName(request.name());
        smartphone.updatePrice(request.basePrice());
        smartphone.updateQuantity(request.quantity());
        smartphone.getSmartphoneConfiguration().configure(
                request.color(), request.batteryCapacity(), request.accessory());

        return SmartphoneMapper.toDTO(smartphoneRepository.save(smartphone));
    }

    public SmartphoneDto getById(Long id) {
        return smartphoneRepository.findById(id)
                .map(SmartphoneMapper::toDTO)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Smartphone with id " + id + " not found"));
    }

    public List<SmartphoneDto> getAll() {
        return smartphoneRepository.getAll().stream()
                .map(SmartphoneMapper::toDTO)
                .toList();
    }

    public void delete(Long id) {
        smartphoneRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Smartphone with id " + id + " not found"));
        smartphoneRepository.delete(id);
    }
}
