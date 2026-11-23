package cl.automarket.api;

import cl.automarket.model.Vehicle;
import cl.automarket.repo.VehicleRepository;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/vehiculos") public class VehicleController {
    private final VehicleRepository repo;
    public VehicleController(VehicleRepository repo) {
        this.repo=repo;
    }

    @GetMapping public List<Dtos.VehicleView> all() {
        return repo.findAll().stream().map(Dtos.VehicleView::from).toList();
    }

    @GetMapping("/{id}") public Dtos.VehicleView one(@PathVariable String id) {
        return Dtos.VehicleView.from(lookup(id));
    }

    @PostMapping public ResponseEntity<Dtos.VehicleView> create(@Valid @RequestBody Dtos.VehicleInput d) {
        Vehicle v=new Vehicle();
        fill(v, d);
        return ResponseEntity.status(HttpStatus.CREATED).body(Dtos.VehicleView.from(repo.save(v)));
    }

    @PutMapping("/{id}") public Dtos.VehicleView update(@PathVariable String id, @Valid @RequestBody Dtos.VehicleInput d) {
        Vehicle v=lookup(id);
        fill(v, d);
        return Dtos.VehicleView.from(repo.save(v));
    }

    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable String id) {
        repo.delete(lookup(id));
        return ResponseEntity.noContent().build();
    }

    private Vehicle lookup(String id) {
        return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehículo no encontrado"));
    }

    private void fill(Vehicle v, Dtos.VehicleInput d) {
        if(!Set.of("Nuevo", "Usado").contains(d.estado()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
        v.marca=d.marca().trim();
        v.modelo=d.modelo().trim();
        v.anio=d.anio();
        v.transmision=d.transmision().trim();
        v.km=d.km();
        v.combustible=d.combustible().trim();
        v.precio=d.precio();
        v.estado=d.estado();
        v.imagen=d.imagen()==null?"auto1":d.imagen();
        v.destacado=Boolean.TRUE.equals(d.destacado());
    }
}
