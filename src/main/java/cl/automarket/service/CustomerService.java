package cl.automarket.service;

import cl.automarket.api.CustomerController.CustomerInput;
import cl.automarket.api.CustomerController.CustomerView;
import cl.automarket.model.Customer;
import cl.automarket.repo.CustomerRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository repository;
    private final MongoTemplate mongoTemplate;

    public CustomerService(CustomerRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<CustomerView> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(c -> c.nombre.toLowerCase()))
                .map(CustomerView::from)
                .toList();
    }

    public CustomerView one(String id) {
        return CustomerView.from(find(id));
    }

    public ResponseEntity<CustomerView> create(CustomerInput input) {
        String identifier = input.idCliente().trim();

        if (repository.existsByIdCliente(identifier)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Identificador de cliente duplicado");
        }

        Customer customer = new Customer();
        fill(customer, input);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomerView.from(repository.save(customer)));
    }

    public CustomerView update(
            String id,
            CustomerInput input
    ) {
        Customer customer = find(id);
        String newIdentifier = input.idCliente().trim();

        repository.findByIdCliente(newIdentifier).ifPresent(existing -> {
            if (!existing.id.equals(customer.id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Identificador de cliente duplicado");
            }
        });

        fill(customer, input);
        return CustomerView.from(repository.save(customer));
    }

    public ResponseEntity<Void> delete(String id) {
        Customer customer = find(id);
        Query reference = Query.query(Criteria.where("id_cliente").is(customer.idCliente));

        if (isReferenced("ventas", reference) || isReferenced("reservas", reference)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar un cliente asociado a ventas o reservas"
            );
        }

        // Solo elimina el registro comercial; las cuentas de acceso no se modifican.
        repository.delete(customer);
        return ResponseEntity.noContent().build();
    }

    private boolean isReferenced(String collection, Query query) {
        return mongoTemplate.collectionExists(collection)
                && mongoTemplate.exists(query, collection);
    }

    private Customer find(String id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado")
        );
    }

    private void fill(Customer customer, CustomerInput input) {
        customer.idCliente = input.idCliente().trim();
        customer.nombre = input.nombre().trim();
        customer.apellido = input.apellido().trim();
        customer.correoElectronico = input.correoElectronico().trim().toLowerCase();
        customer.telefono = input.telefono().trim();
        customer.direccion = input.direccion().trim();
    }
}
