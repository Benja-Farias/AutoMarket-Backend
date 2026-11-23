package cl.automarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
@Document(collection = "consultas")
public class ContactMessage {
    @Id public String id;
    public String nombre;
    public String correo;
    public String comentario;
    public Instant creadoEn = Instant.now();
}
