package ru.itmo.se.is.cw.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.se.is.cw.dto.MaterialBalanceHistoryResponseDto;
import ru.itmo.se.is.cw.dto.MaterialConsumptionResponseDto;
import ru.itmo.se.is.cw.dto.MaterialRequestDto;
import ru.itmo.se.is.cw.dto.MaterialResponseDto;
import ru.itmo.se.is.cw.dto.filter.MaterialFilter;
import ru.itmo.se.is.cw.dto.specification.MaterialSpecification;
import ru.itmo.se.is.cw.exception.EntityNotFoundException;
import ru.itmo.se.is.cw.mapper.MaterialBalanceHistoryMapper;
import ru.itmo.se.is.cw.mapper.MaterialConsumptionMapper;
import ru.itmo.se.is.cw.mapper.MaterialMapper;
import ru.itmo.se.is.cw.model.MaterialBalanceEntity;
import ru.itmo.se.is.cw.model.MaterialConsumptionEntity;
import ru.itmo.se.is.cw.model.MaterialEntity;
import ru.itmo.se.is.cw.repository.MaterialBalanceRepository;
import ru.itmo.se.is.cw.repository.MaterialConsumptionRepository;
import ru.itmo.se.is.cw.repository.MaterialRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialsService {

    private final MaterialRepository materialRepository;
    private final MaterialBalanceRepository materialBalanceRepository;
    private final MaterialConsumptionRepository materialConsumptionRepository;

    private final EntityManager em;

    private final MaterialMapper materialMapper;
    private final MaterialConsumptionMapper materialConsumptionMapper;
    private final MaterialBalanceHistoryMapper materialBalanceHistoryMapper;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<MaterialResponseDto> getMaterials(Pageable pageable, MaterialFilter filter) {
        return materialRepository
                .findAll(MaterialSpecification.byFilter(filter), pageable)
                .map(materialMapper::toDto);
    }

    @Transactional
    public MaterialResponseDto createMaterial(MaterialRequestDto request) {
        MaterialEntity material = materialRepository.save(
                materialMapper.toEntity(request)
        );
        return materialMapper.toDto(
                setMaterialBalance(material.getId(), 0.0)
        );
    }

    @Transactional(readOnly = true)
    public MaterialResponseDto getMaterialById(Long id) {
        return materialMapper.toDto(getById(id));
    }

    @Transactional(readOnly = true)
    public MaterialEntity getById(Long id) {
        return materialRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Material with id " + id + " not found"));
    }

    @Transactional
    public MaterialResponseDto updateMaterial(Long id, MaterialRequestDto request) {
        MaterialEntity material = getById(id);
        materialMapper.updateEntity(material, request);
        return materialMapper.toDto(
                materialRepository.save(material)
        );
    }

    @Transactional
    public void deleteMaterial(Long id) {
        materialRepository.delete(getById(id));
    }

    @Transactional
    public MaterialEntity setMaterialBalance(Long materialId, Double newBalance) {
        if (newBalance == null) {
            throw new IllegalArgumentException("newBalance must not be null");
        }
        if (newBalance < 0) {
            throw new IllegalArgumentException("newBalance must be >= 0");
        }

        MaterialEntity material = getById(materialId);

        materialRepository.updateBalanceAndSetCurrent(
                materialId,
                toBigDecimal(newBalance),
                currentUserService.getAccountId()
        );

        em.refresh(material);
        return material;
    }

    @Transactional
    public MaterialResponseDto updateMaterialBalance(Long materialId, Double newBalance) {
        return materialMapper.toDto(setMaterialBalance(materialId, newBalance));
    }

    @Transactional(readOnly = true)
    public MaterialBalanceHistoryResponseDto getMaterialBalanceHistory(Long materialId) {
        MaterialEntity material = getById(materialId);

        List<MaterialBalanceEntity> balances = materialBalanceRepository
                .findByMaterialIdOrderByChangedAtDesc(materialId)
                .stream()
                .toList();

        List<MaterialConsumptionEntity> consumptions = materialConsumptionRepository
                .findByMaterialIdOrderByCreatedAtDesc(materialId)
                .stream()
                .toList();

        return materialBalanceHistoryMapper.toDto(material, balances, consumptions);
    }

    @Transactional(readOnly = true)
    public List<MaterialConsumptionResponseDto> getMaterialsConsumptionByOrder(Long orderId) {
        return materialConsumptionRepository.findByClientOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(materialConsumptionMapper::toDto)
                .toList();
    }

    @Transactional
    public void recordMaterialConsumptionForOrder(ru.itmo.se.is.cw.model.ClientOrderEntity order) {
        if (order.getProductDesign() == null) {
            return;
        }

        var requiredMaterials = order.getProductDesign().getRequiredMaterials();
        if (requiredMaterials == null || requiredMaterials.isEmpty()) {
            return;
        }

        for (var requiredMaterial : requiredMaterials) {
            var material = requiredMaterial.getMaterial();
            var amount = requiredMaterial.getAmount();

            MaterialConsumptionEntity consumption = new MaterialConsumptionEntity();
            consumption.setClientOrder(order);
            consumption.setMaterial(material);
            consumption.setAmount(amount);
            consumption.setCreatedAt(java.time.ZonedDateTime.now());
            materialConsumptionRepository.save(consumption);

            MaterialEntity materialEntity = getById(material.getId());
            BigDecimal currentBalance = materialEntity.getCurrentBalance() != null
                    ? materialEntity.getCurrentBalance().getBalance()
                    : BigDecimal.ZERO;

            BigDecimal newBalance = currentBalance.subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException(
                        String.format("Insufficient material balance. Material: %s, Required: %s, Available: %s",
                                material.getName(), amount, currentBalance)
                );
            }

            setMaterialBalance(material.getId(), newBalance.doubleValue());
        }
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
