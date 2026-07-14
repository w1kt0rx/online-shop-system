package product.service;

import exception.ProductNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import product.dto.electronics.CreateElectronicsRequest;
import product.dto.electronics.ElectronicsDto;
import product.dto.electronics.UpdateElectronicsRequest;
import product.mapper.electronics.ElectronicsMapper;
import product.model.ProductType;
import product.model.electronics.Electronics;
import product.repository.ElectronicsRepository;

@RequiredArgsConstructor
public class ElectronicsService {

    private final ElectronicsRepository electronicsRepository;

    public ElectronicsDto create(CreateElectronicsRequest request) {
        Electronics electronics = new Electronics(null, request.name(), request.basePrice(), request.quantity());

        return ElectronicsMapper.toDTO(electronicsRepository.save(electronics));
    }

    public ElectronicsDto update(Long id, UpdateElectronicsRequest request) {
        Electronics electronics = findElectronics(id);

        electronics.updateName(request.name());
        electronics.updatePrice(request.basePrice());
        electronics.updateQuantity(request.quantity());

        return ElectronicsMapper.toDTO(electronicsRepository.save(electronics));
    }

    public ElectronicsDto getById(Long id) {
        return ElectronicsMapper.toDTO(findElectronics(id));
    }

    public List<ElectronicsDto> getAll() {
        return electronicsRepository.getAll().stream().map(ElectronicsMapper::toDTO).toList();
    }

    public void delete(Long id) {
        findElectronics(id);
        electronicsRepository.delete(id);
    }

    private Electronics findElectronics(Long id) {
        return electronicsRepository
            .findById(id)
            .orElseThrow(() -> new ProductNotFoundException(ProductType.ELECTRONICS, id));
    }
}
