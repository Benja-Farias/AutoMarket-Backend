package cl.automarket.api;

import cl.automarket.model.Vehicle;
import cl.automarket.repo.VehicleRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/api/vehiculos")
public class VehicleController {
    private final VehicleRepository repository;
    private final MongoTemplate mongoTemplate;

    public VehicleController(VehicleRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping
    public List<Dtos.VehicleView> all() {
        return repository.findAll().stream()
                .map(Dtos.VehicleView::from)
                .toList();
    }

    @GetMapping("/{id}")
    public Dtos.VehicleView one(@PathVariable String id) {
        return Dtos.VehicleView.from(find(id));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Registrar vehículo (ADMIN)")
    @PostMapping
    public ResponseEntity<Dtos.VehicleView> create(@Valid @RequestBody Dtos.VehicleInput input) {
        Vehicle vehicle = new Vehicle();
        fill(vehicle, input);
        verifyVin(vehicle.vin, null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Dtos.VehicleView.from(repository.save(vehicle)));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Modificar vehículo (ADMIN)")
    @PutMapping("/{id}")
    public Dtos.VehicleView update(
            @PathVariable String id,
            @Valid @RequestBody Dtos.VehicleInput input
    ) {
        Vehicle vehicle = find(id);
        fill(vehicle, input);
        verifyVin(vehicle.vin, vehicle.id);

        return Dtos.VehicleView.from(repository.save(vehicle));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar vehículo (ADMIN)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        Vehicle vehicle = find(id);

        if (vehicle.vin != null && !vehicle.vin.isBlank()) {
            Query reference = Query.query(Criteria.where("vin").is(vehicle.vin));

            if (isReferenced("ventas", reference) || isReferenced("reservas", reference)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No se puede eliminar un vehículo asociado a ventas o reservas"
                );
            }
        }

        repository.delete(vehicle);
        return ResponseEntity.noContent().build();
    }

    private boolean isReferenced(String collection, Query query) {
        return mongoTemplate.collectionExists(collection)
                && mongoTemplate.exists(query, collection);
    }

    private Vehicle find(String id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehículo no encontrado")
        );
    }

    private void verifyVin(String vin, String currentId) {
        if (vin == null || vin.isBlank()) {
            return;
        }

        repository.findByVin(vin).ifPresent(existing -> {
            if (!Objects.equals(existing.id, currentId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "VIN duplicado");
            }
        });
    }

    private void fill(Vehicle vehicle, Dtos.VehicleInput input) {
        if (!Set.of("Nuevo", "Usado").contains(input.estado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Condición inválida");
        }

        vehicle.marca = input.marca().trim();
        vehicle.modelo = input.modelo().trim();
        vehicle.anio = input.anio();
        vehicle.transmision = input.transmision().trim();
        vehicle.km = input.km();
        vehicle.combustible = input.combustible().trim();
        vehicle.precio = input.precio();
        vehicle.estado = input.estado();
        vehicle.imagen = input.imagen() == null ? "auto1" : input.imagen();
        vehicle.destacado = Boolean.TRUE.equals(input.destacado());

        vehicle.vin = input.vin() == null || input.vin().isBlank()
                ? null : input.vin().trim();
        vehicle.color = input.color() == null ? null : input.color().trim();
        vehicle.tipoVehiculo = input.tipoVehiculo() == null
                ? null : input.tipoVehiculo().trim();
        vehicle.idSucursal = input.idSucursal();
        vehicle.estadoInventario = input.estadoInventario() == null
                ? "DISPONIBLE" : input.estadoInventario();
    }
}
