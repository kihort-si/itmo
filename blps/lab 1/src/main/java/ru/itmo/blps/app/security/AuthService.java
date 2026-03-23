package ru.itmo.blps.app.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.blps.app.exceptions.BusinessException;
import ru.itmo.blps.app.exceptions.NotFoundException;
import ru.itmo.blps.app.models.AppUser;
import ru.itmo.blps.app.models.Courier;
import ru.itmo.blps.app.models.Customer;
import ru.itmo.blps.app.models.Shop;
import ru.itmo.blps.app.models.ShopAssistant;
import ru.itmo.blps.app.models.enums.UserRole;
import ru.itmo.blps.app.repositories.AppUserRepository;
import ru.itmo.blps.app.repositories.CourierRepository;
import ru.itmo.blps.app.repositories.CustomerRepository;
import ru.itmo.blps.app.repositories.ShopAssistantRepository;
import ru.itmo.blps.app.repositories.ShopRepository;
import ru.itmo.blps.app.security.dto.AuthResponse;
import ru.itmo.blps.app.security.dto.LoginRequest;
import ru.itmo.blps.app.security.dto.RegisterCourierRequest;
import ru.itmo.blps.app.security.dto.RegisterCustomerRequest;
import ru.itmo.blps.app.security.dto.RegisterShopAssistantRequest;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final ShopRepository shopRepository;
    private final ShopAssistantRepository shopAssistantRepository;
    private final CourierRepository courierRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository,
                       CustomerRepository customerRepository,
                       ShopRepository shopRepository,
                       ShopAssistantRepository shopAssistantRepository,
                       CourierRepository courierRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.customerRepository = customerRepository;
        this.shopRepository = shopRepository;
        this.shopAssistantRepository = shopAssistantRepository;
        this.courierRepository = courierRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AuthResponse registerCustomer(RegisterCustomerRequest request) {
        ensureUsernameFree(request.username());

        Customer customer = customerRepository.save(new Customer(null, request.fullName(), request.email()));

        AppUser user = appUserRepository.save(new AppUser(
                null,
                request.username(),
                passwordEncoder.encode(request.password()),
                UserRole.CUSTOMER,
                customer.getId()
        ));

        return issueToken(user);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AuthResponse registerShopAssistant(RegisterShopAssistantRequest request) {
        ensureUsernameFree(request.username());
        Shop shop = shopRepository.findById(request.shopId())
                .orElseThrow(() -> new NotFoundException("Салон связи не найден: id=" + request.shopId()));

        ShopAssistant assistant = new ShopAssistant();
        assistant.setName(request.name());
        assistant.setShop(shop);
        assistant = shopAssistantRepository.save(assistant);

        AppUser user = appUserRepository.save(new AppUser(
                null,
                request.username(),
                passwordEncoder.encode(request.password()),
                UserRole.SHOP_ASSISTANT,
                assistant.getId()
        ));

        return issueToken(user);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AuthResponse registerCourier(RegisterCourierRequest request) {
        ensureUsernameFree(request.username());

        Courier courier = courierRepository.save(new Courier(null, request.name(), request.passportId()));

        AppUser user = appUserRepository.save(new AppUser(
                null,
                request.username(),
                passwordEncoder.encode(request.password()),
                UserRole.COURIER,
                courier.getId()
        ));

        return issueToken(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("Неверный логин или пароль"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("Неверный логин или пароль");
        }

        return issueToken(user);
    }

    @Transactional
    public void logout(String tokenValue) {
    }

    private void ensureUsernameFree(String username) {
        if (appUserRepository.findByUsername(username).isPresent()) {
            throw new BusinessException("Пользователь с таким username уже существует");
        }
    }

    private AuthResponse issueToken(AppUser user) {
        return new AuthResponse(jwtService.generateToken(user), user.getId(), user.getRole(), user.getParticipantId());
    }
}
