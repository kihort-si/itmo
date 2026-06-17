package ru.itmo.blps.app.security;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.itmo.blps.app.exceptions.BusinessException;
import ru.itmo.blps.app.exceptions.NotFoundException;
import ru.itmo.blps.app.models.Courier;
import ru.itmo.blps.app.models.Customer;
import ru.itmo.blps.app.models.Shop;
import ru.itmo.blps.app.models.ShopAssistant;
import ru.itmo.blps.app.models.enums.UserRole;
import ru.itmo.blps.app.repositories.CourierRepository;
import ru.itmo.blps.app.repositories.CustomerRepository;
import ru.itmo.blps.app.repositories.ShopAssistantRepository;
import ru.itmo.blps.app.repositories.ShopRepository;
import ru.itmo.blps.app.security.access.RolePrivileges;
import ru.itmo.blps.app.security.dto.AuthResponse;
import ru.itmo.blps.app.security.dto.RegisterCourierRequest;
import ru.itmo.blps.app.security.dto.RegisterCustomerRequest;
import ru.itmo.blps.app.security.dto.RegisterShopAssistantRequest;
import ru.itmo.blps.app.security.xml.StoredUserAccount;
import ru.itmo.blps.app.security.xml.XmlUserStore;

@Service
public class AuthService {
    private final CustomerRepository customerRepository;
    private final ShopRepository shopRepository;
    private final ShopAssistantRepository shopAssistantRepository;
    private final CourierRepository courierRepository;
    private final PasswordEncoder passwordEncoder;
    private final XmlUserStore xmlUserStore;
    private final TransactionTemplate serializableTransactionTemplate;

    public AuthService(CustomerRepository customerRepository,
                       ShopRepository shopRepository,
                       ShopAssistantRepository shopAssistantRepository,
                       CourierRepository courierRepository,
                       PasswordEncoder passwordEncoder,
                       XmlUserStore xmlUserStore,
                       @Qualifier("serializableTransactionTemplate")
                       TransactionTemplate serializableTransactionTemplate) {
        this.customerRepository = customerRepository;
        this.shopRepository = shopRepository;
        this.shopAssistantRepository = shopAssistantRepository;
        this.courierRepository = courierRepository;
        this.passwordEncoder = passwordEncoder;
        this.xmlUserStore = xmlUserStore;
        this.serializableTransactionTemplate = serializableTransactionTemplate;
    }

    public AuthResponse registerCustomer(RegisterCustomerRequest request) {
        ensureUsernameFree(request.username());
        return serializableTransactionTemplate.execute(status -> {
            Customer customer = customerRepository.save(new Customer(null, request.fullName().trim(), request.email().trim()));
            StoredUserAccount account = new StoredUserAccount(
                    request.username().trim(),
                    passwordEncoder.encode(request.password()),
                    UserRole.CUSTOMER,
                    customer.getId()
            );
            persistUserAfterCommit(account);
            return toResponse(account);
        });
    }

    public AuthResponse registerShopAssistant(RegisterShopAssistantRequest request) {
        ensureUsernameFree(request.username());
        return serializableTransactionTemplate.execute(status -> {
            Shop shop = shopRepository.findById(request.shopId())
                    .orElseThrow(() -> new NotFoundException("Салон связи не найден: id=" + request.shopId()));

            ShopAssistant assistant = new ShopAssistant();
            assistant.setName(request.name().trim());
            assistant.setShop(shop);
            assistant = shopAssistantRepository.save(assistant);

            StoredUserAccount account = new StoredUserAccount(
                    request.username().trim(),
                    passwordEncoder.encode(request.password()),
                    UserRole.SHOP_ASSISTANT,
                    assistant.getId()
            );
            persistUserAfterCommit(account);
            return toResponse(account);
        });
    }

    public AuthResponse registerCourier(RegisterCourierRequest request) {
        ensureUsernameFree(request.username());
        return serializableTransactionTemplate.execute(status -> {
            Courier courier = courierRepository.save(new Courier(null, request.name().trim(), request.passportId().trim()));
            StoredUserAccount account = new StoredUserAccount(
                    request.username().trim(),
                    passwordEncoder.encode(request.password()),
                    UserRole.COURIER,
                    courier.getId()
            );
            persistUserAfterCommit(account);
            return toResponse(account);
        });
    }

    private void ensureUsernameFree(String username) {
        if (xmlUserStore.findByUsername(username.trim()).isPresent()) {
            throw new BusinessException("Пользователь с таким username уже существует");
        }
    }

    private void persistUserAfterCommit(StoredUserAccount account) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                xmlUserStore.save(account);
            }
        });
    }

    private AuthResponse toResponse(StoredUserAccount account) {
        List<String> privileges = RolePrivileges.get(account.role()).stream()
                .map(Enum::name)
                .toList();
        return new AuthResponse(
                account.username(),
                account.role(),
                account.participantId(),
                privileges,
                "BASIC"
        );
    }
}
