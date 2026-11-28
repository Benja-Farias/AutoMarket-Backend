package cl.automarket.config;

import cl.automarket.model.Customer;
import cl.automarket.model.UserAccount;
import cl.automarket.repo.CustomerRepository;
import cl.automarket.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@Configuration
public class DemoDataSeeder {
    @Bean
    CommandLineRunner seedDemoData(
            CustomerRepository customers,
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${app.demo.enabled:false}") boolean enabled,
            @Value("${app.demo.customer.password:}") String customerPassword
    ) {
        return args -> {
            if (!enabled) {
                return;
            }

            addCustomer(customers, "DEMO-001", "Camila", "Rojas", "camila.demo@example.com");
            addCustomer(customers, "DEMO-002", "Martín", "Soto", "martin.demo@example.com");
            addCustomer(customers, "DEMO-003", "Valentina", "Pérez", "valentina.demo@example.com");

            if (!customerPassword.isBlank()) {
                String email = "cliente.demo@example.com";
                if (users.findByEmail(email).isEmpty()) {
                    users.save(new UserAccount("Cliente Demo", email,
                            encoder.encode(customerPassword), "CLIENTE"));
                }
            }
        };
    }

    private void addCustomer(CustomerRepository customers, String id, String nombre,
                             String apellido, String email) {
        if (customers.existsByIdCliente(id)) {
            return;
        }
        Customer customer = new Customer();
        customer.idCliente = id;
        customer.nombre = nombre;
        customer.apellido = apellido;
        customer.correoElectronico = email.toLowerCase(Locale.ROOT);
        customer.telefono = "+56900000000";
        customer.direccion = "Dirección ficticia - Demostración";
        customers.save(customer);
    }
}
