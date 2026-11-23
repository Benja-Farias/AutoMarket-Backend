package cl.automarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection = "usuarios")
public class UserAccount {
    @Id public String id;
    public String nombre;
    @Indexed(unique = true) public String email;
    public String passwordHash;
    public String rol = "CLIENTE";
    public UserAccount() {
    }

    public UserAccount(String nombre, String email, String hash, String rol) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = hash;
        this.rol = rol;
    }
}
