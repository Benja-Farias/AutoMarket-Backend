package cl.automarket.repo;

import cl.automarket.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    Optional<Customer> findByIdCliente(String idCliente);
    boolean existsByIdCliente(String idCliente);
}
