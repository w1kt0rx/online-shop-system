package product.service;

import exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import product.dto.electronics.CreateElectronicsRequest;
import product.dto.electronics.ElectronicsDto;
import product.dto.electronics.UpdateElectronicsRequest;
import product.mapper.electronics.ElectronicsMapper;
import product.model.electronics.Electronics;
import product.repository.ElectronicsRepository;

import java.util.List;

@RequiredArgsConstructor
public class ElectronicsService {

    private final ElectronicsRepository electronicsRepository;

    public ElectronicsDto create(CreateElectronicsRequest request) {
        Electronics electronics = new Electronics(
                electronicsRepository.getNextId(),
                request.name(),
                request.basePrice(),
                request.quantity());

        return ElectronicsMapper.toDTO(electronicsRepository.save(electronics));
    }

    public ElectronicsDto update(Long id, UpdateElectronicsRequest request) {
        Electronics electronics = electronicsRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Electronics with id " + id + " not found"));

        electronics.updateName(request.name());
        electronics.updatePrice(request.basePrice());
        electronics.updateQuantity(request.quantity());

        return ElectronicsMapper.toDTO(electronicsRepository.save(electronics));
    }

    public ElectronicsDto getById(Long id) {
        return electronicsRepository.findById(id)
                .map(ElectronicsMapper::toDTO)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Electronics with id " + id + " not found"));
    }

    public List<ElectronicsDto> getAll() {
        return electronicsRepository.getAll().stream()
                .map(ElectronicsMapper::toDTO)
                .toList();
    }

    public void delete(Long id) {
        electronicsRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Electronics with id " + id + " not found"));
        electronicsRepository.delete(id);
    }
}
