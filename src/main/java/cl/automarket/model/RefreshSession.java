package cl.automarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "sesiones_refresh")
public class RefreshSession {
    @Id public String id;
    @Indexed(unique = true) public String tokenHash;
    public String usuarioId;
    @Indexed(expireAfterSeconds = 0) public Instant expiresAt;
}
