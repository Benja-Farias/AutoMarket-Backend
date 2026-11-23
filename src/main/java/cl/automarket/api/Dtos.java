package cl.automarket.api;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import cl.automarket.model.*;
public final class Dtos {
 private Dtos() {}
 public record Login(@NotBlank @Email String correo,@NotBlank String password) {}
 public record Register(@NotBlank String nombre,@NotBlank @Email String correo,@NotBlank @Size(min=8) String password) {}
 public record UserView(String id,String nombre,String correo,String rol) { public static UserView from(UserAccount u){return new UserView(u.id,u.nombre,u.email,u.rol);} }
 public record AuthResponse(String accessToken,UserView user) {}
 public record VehicleInput(@NotBlank String marca,@NotBlank String modelo,@NotNull @Min(1900) @Max(2100) Integer anio,
   @NotBlank String transmision,@NotNull @Min(0) Integer km,@NotBlank String combustible,
   @NotNull @DecimalMin("1") BigDecimal precio,@NotBlank String estado,
   @Pattern(regexp="auto[123]") String imagen,Boolean destacado) {}
 public record VehicleView(String id,String marca,String modelo,Integer anio,String transmision,Integer km,
   String combustible,BigDecimal precio,String estado,String imagen,Boolean destacado) {
  public static VehicleView from(Vehicle v){return new VehicleView(v.id,v.marca,v.modelo,v.anio,v.transmision,v.km,v.combustible,v.precio,v.estado,v.imagen,v.destacado);}
 }
 public record ContactInput(@NotBlank String nombre,@NotBlank @Email String correo,@NotBlank @Size(max=2000) String comentario) {}
 public record ContactView(String id,String nombre,String correo,String comentario,java.time.Instant creadoEn){
  public static ContactView from(ContactMessage c){return new ContactView(c.id,c.nombre,c.correo,c.comentario,c.creadoEn);}
 }
}
