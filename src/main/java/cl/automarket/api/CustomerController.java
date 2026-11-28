package cl.automarket.api;

import cl.automarket.model.Customer;
import cl.automarket.service.CustomerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping({"/api/clientes", "/api/v1/clientes"})
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
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
        return service.list();
    }

    @GetMapping("/{id}")
    public CustomerView one(@PathVariable String id) {
        return service.one(id);
    }

    @PostMapping
    public ResponseEntity<CustomerView> create(@Valid @RequestBody CustomerInput input) {
        return service.create(input);
    }

    @PutMapping("/{id}")
    public CustomerView update(@PathVariable String id, @Valid @RequestBody CustomerInput input) {
        return service.update(id, input);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return service.delete(id);
    }
}
