package cl.automarket.api;

import cl.automarket.model.ContactMessage;
import cl.automarket.model.UserAccount;
import cl.automarket.model.Vehicle;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public final class Dtos {
    private Dtos() {}

    public record Login(@NotBlank @Email @Size(max = 100) String correo, @NotBlank String password) {}

    public record Register(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Email @Size(max = 100) String correo,
            @NotBlank @Size(min = 8) String password
    ) {}

    public record UserView(String id, String nombre, String correo, String rol) {
        public static UserView from(UserAccount user) {
            return new UserView(user.id, user.nombre, user.email, user.rol);
        }
    }

    public record AuthResponse(String accessToken, UserView user) {}

    public record VehicleInput(
            @NotBlank String marca,
            @NotBlank String modelo,
            @NotNull @Min(1900) @Max(2100) Integer anio,
            @NotBlank String transmision,
            @NotNull @Min(0) Integer km,
            @NotBlank String combustible,
            @NotNull @DecimalMin("1") BigDecimal precio,
            @NotBlank String estado,
            @Pattern(regexp = "auto[123]") String imagen,
            Boolean destacado,
            @Size(max = 40) String vin,
            @Size(max = 60) String color,
            @Size(max = 80) String tipoVehiculo,
            @Min(1) Integer idSucursal,
            @Pattern(regexp = "DISPONIBLE|RESERVADO|VENDIDO") String estadoInventario
    ) {}

    public record VehicleView(
            String id,
            String marca,
            String modelo,
            Integer anio,
            String transmision,
            Integer km,
            String combustible,
            BigDecimal precio,
            String estado,
            String imagen,
            Boolean destacado,
            String vin,
            String color,
            String tipoVehiculo,
            Integer idSucursal,
            String estadoInventario
    ) {
        public static VehicleView from(Vehicle vehicle) {
            return new VehicleView(
                    vehicle.id,
                    vehicle.marca,
                    vehicle.modelo,
                    vehicle.anio,
                    vehicle.transmision,
                    vehicle.km,
                    vehicle.combustible,
                    vehicle.precio,
                    vehicle.estado,
                    vehicle.imagen,
                    vehicle.destacado,
                    vehicle.vin,
                    vehicle.color,
                    vehicle.tipoVehiculo,
                    vehicle.idSucursal,
                    vehicle.estadoInventario
            );
        }
    }

    public record ContactInput(
            @NotBlank String nombre,
            @NotBlank @Email String correo,
            @NotBlank @Size(max = 2000) String comentario
    ) {}

    public record ContactView(
            String id,
            String nombre,
            String correo,
            String comentario,
            Instant creadoEn
    ) {
        public static ContactView from(ContactMessage message) {
            return new ContactView(
                    message.id,
                    message.nombre,
                    message.correo,
                    message.comentario,
                    message.creadoEn
            );
        }
    }
}
