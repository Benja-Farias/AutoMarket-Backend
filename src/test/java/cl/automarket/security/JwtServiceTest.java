package cl.automarket.security;

import cl.automarket.model.UserAccount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    @Test void creaYVerificaToken() {
        JwtService jwt = new JwtService("12345678901234567890123456789012345678901234567890", 15);
        UserAccount u = new UserAccount("Test", "test@automarket.cl", "hash", "CLIENTE");
        String token = jwt.create(u);
        assertEquals(u.email, jwt.email(token));
    }

    @Test void rechazaTokenManipulado() {
        JwtService jwt = new JwtService("12345678901234567890123456789012345678901234567890", 15);
        assertThrows(Exception.class, () -> jwt.email("no-es-un-token"));
    }
}
