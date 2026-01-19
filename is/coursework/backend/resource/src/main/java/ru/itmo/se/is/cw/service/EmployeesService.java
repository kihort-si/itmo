package ru.itmo.se.is.cw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.se.is.cw.dto.AccountRequestDto;
import ru.itmo.se.is.cw.dto.AccountResponseDto;
import ru.itmo.se.is.cw.dto.EmployeeRequestDto;
import ru.itmo.se.is.cw.dto.EmployeeResponseDto;
import ru.itmo.se.is.cw.dto.filter.EmployeeFilter;
import ru.itmo.se.is.cw.dto.specification.EmployeeSpecification;
import ru.itmo.se.is.cw.exception.EntityNotFoundException;
import ru.itmo.se.is.cw.feign.AccountClient;
import ru.itmo.se.is.cw.mapper.EmployeeMapper;
import ru.itmo.se.is.cw.model.EmployeeEntity;
import ru.itmo.se.is.cw.model.value.AccountRole;
import ru.itmo.se.is.cw.repository.EmployeeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeesService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final AccountClient accountClient;

    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeRequestDto request) {
        EmployeeEntity entity = employeeMapper.toEntity(request);

        AccountRequestDto accountRequestDto = new AccountRequestDto();
        accountRequestDto.setPassword(request.getPassword());
        accountRequestDto.setUsername(request.getUsername());
        accountRequestDto.setRole(AccountRole.valueOf(request.getRole().name()));

        AccountResponseDto responseDto = accountClient.createAccount(accountRequestDto);

        entity.setAccountId(responseDto.getAccountId());

        EmployeeEntity savedEntity = employeeRepository.save(entity);
        EmployeeResponseDto dto = employeeMapper.toDto(savedEntity);
        dto.setUsername(responseDto.getUsername());
        dto.setEnabled(responseDto.getEnabled());
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponseDto> getEmployees(Pageable pageable, EmployeeFilter filter) {
        return employeeRepository
                .findAll(EmployeeSpecification.byFilter(filter), pageable)
                .map(employee -> {
                    EmployeeResponseDto dto = employeeMapper.toDto(employee);
                    try {
                        AccountResponseDto account = accountClient.getAccount(employee.getAccountId());
                        if (account != null) {
                            dto.setUsername(account.getUsername());
                            dto.setEnabled(account.getEnabled());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(Long id) {
        EmployeeEntity employee = getById(id);
        EmployeeResponseDto dto = employeeMapper.toDto(employee);
        try {
            AccountResponseDto account = accountClient.getAccount(employee.getAccountId());
            dto.setUsername(account.getUsername());
            dto.setEnabled(account.getEnabled());
        } catch (Exception e) {
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public EmployeeEntity getById(Long id) {
        return employeeRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public EmployeeEntity getByAccountId(Long accountId) {
        return employeeRepository
                .findByAccountId(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Employee with accountId " + accountId + " not found"));
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeByAccountId(Long accountId) {
        EmployeeEntity employee = getByAccountId(accountId);
        EmployeeResponseDto dto = employeeMapper.toDto(employee);
        try {
            AccountResponseDto account = accountClient.getAccount(accountId);
            dto.setUsername(account.getUsername());
            dto.setEnabled(account.getEnabled());
        } catch (Exception e) {
        }
        return dto;
    }

    public void enableEmployee(Long id) {
        accountClient.enableAccount(id);
    }

    public void disableEmployee(Long id) {
        accountClient.disableAccount(id);
    }

    @Transactional(readOnly = true)
    public List<EmployeeEntity> getEmployeesByRole(ru.itmo.se.is.cw.model.value.EmployeeRole role) {
        return employeeRepository.findAll().stream()
                .filter(employee -> employee.getRole() == role)
                .toList();
    }
}
