package ru.itmo.se.is.cw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.se.is.cw.dto.AccountRequestDto;
import ru.itmo.se.is.cw.dto.AccountResponseDto;
import ru.itmo.se.is.cw.dto.ClientRegistrationRequestDto;
import ru.itmo.se.is.cw.dto.ClientResponseDto;
import ru.itmo.se.is.cw.dto.filter.ClientFilter;
import ru.itmo.se.is.cw.dto.specification.ClientSpecification;
import ru.itmo.se.is.cw.exception.EntityNotFoundException;
import ru.itmo.se.is.cw.feign.AccountClient;
import ru.itmo.se.is.cw.mapper.ClientMapper;
import ru.itmo.se.is.cw.model.ClientEntity;
import ru.itmo.se.is.cw.model.value.AccountRole;
import ru.itmo.se.is.cw.repository.ClientRepository;

@Service
@RequiredArgsConstructor
public class ClientsService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final AccountClient accountClient;

    @Transactional(readOnly = true)
    public Page<ClientResponseDto> getClients(Pageable pageable, ClientFilter filter) {
        return clientRepository
                .findAll(ClientSpecification.byFilter(filter), pageable)
                .map(clientMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ClientResponseDto getClientById(Long id) {
        return clientMapper.toDto(getById(id));
    }

    public void registerClient(ClientRegistrationRequestDto request) {
        ClientEntity client = clientMapper.toEntity(request);

        AccountRequestDto accountRequestDto = new AccountRequestDto();
        accountRequestDto.setPassword(request.getPassword());
        accountRequestDto.setUsername(request.getUsername());
        accountRequestDto.setRole(AccountRole.CLIENT);

        AccountResponseDto responseDto = accountClient.createAccount(accountRequestDto);

        client.setAccountId(responseDto.getAccountId());
        accountClient.enableAccount(client.getAccountId());

        clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    public ClientEntity getById(Long id) {
        return clientRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public ClientEntity getByAccountId(Long accountId) {
        return clientRepository
                .findByAccountId(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Client with accountId " + accountId + " not found"));
    }

    @Transactional(readOnly = true)
    public ClientResponseDto getClientByAccountId(Long accountId) {
        ClientEntity client = getByAccountId(accountId);
        return clientMapper.toDto(client);
    }
}
