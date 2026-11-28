package cl.automarket.api;

import cl.automarket.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/vehiculos", "/api/v1/vehiculos"})
public class VehicleController {
    private final VehicleService service;

    public VehicleController(VehicleService service) {
        this.service = service;
    }

    @GetMapping
    public List<Dtos.VehicleView> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public Dtos.VehicleView one(@PathVariable String id) {
        return service.one(id);
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Registrar vehículo (ADMIN)")
    public ResponseEntity<Dtos.VehicleView> create(@Valid @RequestBody Dtos.VehicleInput input) {
        return service.create(input);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Modificar vehículo (ADMIN)")
    public Dtos.VehicleView update(@PathVariable String id,
                                   @Valid @RequestBody Dtos.VehicleInput input) {
        return service.update(id, input);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar vehículo (ADMIN)")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return service.delete(id);
    }
}
