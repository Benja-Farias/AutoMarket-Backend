package cl.automarket.service;

import cl.automarket.api.Dtos;
import cl.automarket.model.UserAccount;
import cl.automarket.repo.RefreshRepository;
import cl.automarket.repo.UserRepository;
import cl.automarket.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @Test
    void loginEmiteCookieCompatibleConRutasVersionadas() {
        UserRepository users = mock(UserRepository.class);
        RefreshRepository sessions = mock(RefreshRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        JwtService jwt = mock(JwtService.class);
        UserAccount user = new UserAccount("Administrador", "admin@example.com", "hash", "ADMIN");
        user.id = "123";

        when(users.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("clave-prueba", "hash")).thenReturn(true);
        when(jwt.create(user)).thenReturn("jwt-ejemplo");

        AuthService auth = new AuthService(users, sessions, encoder, jwt, false, 7);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Dtos.AuthResponse result = auth.login(new Dtos.Login("admin@example.com", "clave-prueba"), response);

        assertEquals("jwt-ejemplo", result.accessToken());
        List<String> cookies = response.getHeaders("Set-Cookie").stream().toList();
        assertTrue(cookies.stream().anyMatch(value -> value.contains("Path=/api;")));
        assertTrue(cookies.stream().anyMatch(value -> value.contains("Path=/api/auth;") && value.contains("Max-Age=0")));
        assertTrue(cookies.stream().anyMatch(value -> value.contains("HttpOnly")));
        verify(sessions).save(any());
    }

    @Test
    void loginRechazaCredencialesInvalidas() {
        UserRepository users = mock(UserRepository.class);
        RefreshRepository sessions = mock(RefreshRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        JwtService jwt = mock(JwtService.class);

        when(users.findByEmail("cliente@example.com")).thenReturn(Optional.empty());
        AuthService auth = new AuthService(users, sessions, encoder, jwt, false, 7);
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> auth.login(new Dtos.Login("cliente@example.com", "incorrecta"), new MockHttpServletResponse()));
    }
}
