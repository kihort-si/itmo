package ru.itmo.se.is.cw.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.se.is.cw.dto.ProductionTaskResponseDto;
import ru.itmo.se.is.cw.exception.EntityNotFoundException;
import ru.itmo.se.is.cw.mapper.ProductionTaskMapper;
import ru.itmo.se.is.cw.model.ClientOrderEntity;
import ru.itmo.se.is.cw.model.EmployeeEntity;
import ru.itmo.se.is.cw.model.ProductionTaskEntity;
import ru.itmo.se.is.cw.model.value.ClientOrderStatus;
import ru.itmo.se.is.cw.model.value.ProductionTaskStatus;
import ru.itmo.se.is.cw.repository.ClientOrderRepository;
import ru.itmo.se.is.cw.repository.ProductionTaskRepository;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionService {

    private final ProductionTaskRepository productionTaskRepository;
    private final ProductionTaskMapper productionTaskMapper;
    private final EmployeesService employeesService;
    private final CurrentUserService currentUserService;
    private final ClientOrderRepository clientOrderRepository;
    private final MaterialsService materialsService;
    private final EntityManager em;

    @Transactional
    public ProductionTaskEntity assignTask() {
        ProductionTaskEntity task = productionTaskRepository
                .findFirstByCurrentStatusStatusOrderByCreatedAtAsc(ProductionTaskStatus.QUEUED)
                .orElse(null);
        if (task == null) {
            return null;
        }
        EmployeeEntity operator = employeesService.getByAccountId(currentUserService.getAccountId());
        task.setCncOperator(operator);
        task = productionTaskRepository.save(task);
        productionTaskRepository.updateStatusAndSetCurrent(task.getId(), ProductionTaskStatus.PENDING.name());
        em.refresh(task);
        return task;
    }

    @Transactional
    public ProductionTaskResponseDto getCurrentTask() {
        EmployeeEntity operator = employeesService.getByAccountId(currentUserService.getAccountId());
        return productionTaskMapper.toDto(
                productionTaskRepository
                        .findFirstByCncOperatorIdAndCurrentStatusStatusIn(
                                operator.getId(),
                                List.of(ProductionTaskStatus.IN_PROGRESS, ProductionTaskStatus.PENDING)
                        )
                        .orElse(
                                assignTask()
                        )
        );
    }

    @Transactional(readOnly = true)
    public ProductionTaskEntity getById(Long id) {
        return productionTaskRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Production task with id " + id + " not found"));
    }

    @Transactional
    public void createForOrder(ClientOrderEntity order) {
        if (productionTaskRepository.existsByClientOrderId(order.getId())) {
            throw new IllegalStateException("Production task already exists for order " + order.getId());
        }
        ProductionTaskEntity task = new ProductionTaskEntity();
        task.setClientOrder(order);
        task = productionTaskRepository.save(task);
        productionTaskRepository.updateStatusAndSetCurrent(task.getId(), ProductionTaskStatus.QUEUED.name());
    }

    @Transactional
    public void startProductionTask(Long id) {
        changeStatus(id, ProductionTaskStatus.IN_PROGRESS);
        ProductionTaskEntity task = getById(id);
        task.setStartedAt(ZonedDateTime.now());

        materialsService.recordMaterialConsumptionForOrder(task.getClientOrder());

        clientOrderRepository.updateStatusAndSetCurrent(
                task.getClientOrder().getId(),
                ClientOrderStatus.IN_PRODUCTION.name()
        );
    }

    @Transactional
    public void finishProductionTask(Long id) {
        changeStatus(id, ProductionTaskStatus.COMPLETED);
        ProductionTaskEntity task = getById(id);
        task.setStartedAt(ZonedDateTime.now());
        clientOrderRepository.updateStatusAndSetCurrent(
                task.getClientOrder().getId(),
                ClientOrderStatus.READY_FOR_PICKUP.name()
        );
    }

    @Transactional
    public void changeStatus(Long id, ProductionTaskStatus newStatus) {
        getById(id);
        productionTaskRepository.updateStatusAndSetCurrent(id, newStatus.name());
    }
}

