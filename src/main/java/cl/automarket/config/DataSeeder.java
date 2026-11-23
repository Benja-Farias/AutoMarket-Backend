package cl.automarket.config;

import cl.automarket.model.*;
import cl.automarket.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.Locale;
@Configuration
public class DataSeeder {
    @Bean CommandLineRunner seed(UserRepository users, VehicleRepository vehicles, PasswordEncoder encoder, @Value("${app.admin.email}")String adminEmail, @Value("${app.admin.password}")String adminPassword) {
        return args-> {
            if(!adminEmail.isBlank() && !adminPassword.isBlank()) {
                String mail=adminEmail.trim().toLowerCase(Locale.ROOT);
                if(users.findByEmail(mail).isEmpty())users.save(new UserAccount("Administrador", mail, encoder.encode(adminPassword), "ADMIN"));
            }
            if(vehicles.count()==0) {
                add(vehicles, "Toyota", "RAV4", 2023, "Automático", 25000, "Bencina", 18990000, "Usado", "auto1", true);
                add(vehicles, "Mazda", "CX-5", 2022, "Mecánico", 40000, "Bencina", 16500000, "Usado", "auto2", true);
                add(vehicles, "Chevrolet", "Tracker", 2024, "Automático", 10000, "Turbo", 14200000, "Usado", "auto3", true);
                add(vehicles, "Hyundai", "Tucson", 2021, "Automático", 55000, "Diésel", 15800000, "Usado", "auto1", true);
                add(vehicles, "Ford", "Ranger", 2023, "4x4", 30000, "Diésel", 22900000, "Usado", "auto2", true);
                add(vehicles, "Nissan", "Kicks", 2022, "Mecánico", 35000, "Bencina", 12990000, "Usado", "auto3", true);
                add(vehicles, "Toyota", "RAV4", 2024, "Automático", 0, "Bencina", 24990000, "Nuevo", "auto3", false);
            }
        }
        ;
    }

    private void add(VehicleRepository repo, String marca, String modelo, int anio, String transmision, int km, String combustible, long precio, String estado, String imagen, boolean destacado) {
        Vehicle v=new Vehicle();
        v.marca=marca;
        v.modelo=modelo;
        v.anio=anio;
        v.transmision=transmision;
        v.km=km;
        v.combustible=combustible;
        v.precio=BigDecimal.valueOf(precio);
        v.estado=estado;
        v.imagen=imagen;
        v.destacado=destacado;
        repo.save(v);
    }
}
