package cl.automarket.api;

import cl.automarket.model.Customer;
import cl.automarket.repo.CustomerRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/clientes")
public class CustomerController {
    private final CustomerRepository repository;
    private final MongoTemplate mongoTemplate;

    public CustomerController(CustomerRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    public record CustomerInput(
            @NotBlank @Size(max = 20) String idCliente,
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Size(max = 100) String apellido,
            @NotBlank @Email @Size(max = 180) String correoElectronico,
            @NotBlank @Size(max = 30) String telefono,
            @NotBlank @Size(max = 250) String direccion
    ) {}

    public record CustomerView(
            String id,
            String idCliente,
            String nombre,
            String apellido,
            String correoElectronico,
            String telefono,
            String direccion
    ) {
        public static CustomerView from(Customer customer) {
            return new CustomerView(
                    customer.id,
                    customer.idCliente,
                    customer.nombre,
                    customer.apellido,
                    customer.correoElectronico,
                    customer.telefono,
                    customer.direccion
            );
        }
    }

    @GetMapping
    public List<CustomerView> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(c -> c.nombre.toLowerCase()))
                .map(CustomerView::from)
                .toList();
    }

    @GetMapping("/{id}")
    public CustomerView one(@PathVariable String id) {
        return CustomerView.from(find(id));
    }

    @PostMapping
    public ResponseEntity<CustomerView> create(@Valid @RequestBody CustomerInput input) {
        String identifier = input.idCliente().trim();

        if (repository.existsByIdCliente(identifier)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Identificador de cliente duplicado");
        }

        Customer customer = new Customer();
        fill(customer, input);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomerView.from(repository.save(customer)));
    }

    @PutMapping("/{id}")
    public CustomerView update(
            @PathVariable String id,
            @Valid @RequestBody CustomerInput input
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
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
