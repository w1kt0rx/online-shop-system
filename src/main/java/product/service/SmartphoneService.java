package product.service;

import exception.ProductNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import product.dto.smartphone.CreateSmartphoneRequest;
import product.dto.smartphone.SmartphoneDto;
import product.dto.smartphone.UpdateSmartphoneRequest;
import product.mapper.smartphone.SmartphoneMapper;
import product.model.ProductType;
import product.model.smartphone.Smartphone;
import product.model.smartphone.configuration.SmartphoneConfiguration;
import product.repository.SmartphoneRepository;

@RequiredArgsConstructor
public class SmartphoneService {

    private final SmartphoneRepository smartphoneRepository;

    public SmartphoneDto create(CreateSmartphoneRequest request) {
        SmartphoneConfiguration configuration = new SmartphoneConfiguration();
        configuration.configure(request.color(), request.batteryCapacity(), request.accessory());

        Smartphone smartphone = new Smartphone(
            null,
            request.name(),
            request.basePrice(),
            request.quantity(),
            configuration
        );

        return SmartphoneMapper.toDTO(smartphoneRepository.save(smartphone));
    }

    public SmartphoneDto update(Long id, UpdateSmartphoneRequest request) {
        Smartphone smartphone = findSmartphone(id);

        smartphone.updateName(request.name());
        smartphone.updatePrice(request.basePrice());
        smartphone.updateQuantity(request.quantity());
        smartphone
            .getSmartphoneConfiguration()
            .configure(request.color(), request.batteryCapacity(), request.accessory());

        return SmartphoneMapper.toDTO(smartphoneRepository.save(smartphone));
    }

    public SmartphoneDto getById(Long id) {
        return SmartphoneMapper.toDTO(findSmartphone(id));
    }

    public List<SmartphoneDto> getAll() {
        return smartphoneRepository.getAll().stream().map(SmartphoneMapper::toDTO).toList();
    }

    public void delete(Long id) {
        findSmartphone(id);
        smartphoneRepository.delete(id);
    }

    private Smartphone findSmartphone(Long id) {
        return smartphoneRepository
            .findById(id)
            .orElseThrow(() -> new ProductNotFoundException(ProductType.SMARTPHONE, id));
    }
}
