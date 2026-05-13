package ru.itmo.blps.app.services;

import org.springframework.stereotype.Service;
import ru.itmo.blps.app.exceptions.NotFoundException;
import ru.itmo.blps.app.models.Customer;
import ru.itmo.blps.app.repositories.CustomerRepository;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer getById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Клиент не найден: id=" + customerId));
    }
}
