package ru.itmo.blps.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.blps.app.models.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
