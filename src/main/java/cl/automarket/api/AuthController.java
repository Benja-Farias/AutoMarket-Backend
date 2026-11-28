package cl.automarket.api;

import cl.automarket.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<Dtos.AuthResponse> register(
            @Valid @RequestBody Dtos.Register input, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(input, response));
    }

    @PostMapping("/login")
    public Dtos.AuthResponse login(@Valid @RequestBody Dtos.Login input,
                                   HttpServletResponse response) {
        return service.login(input, response);
    }

    @PostMapping("/refresh")
    public Dtos.AuthResponse refresh(
            @CookieValue(name = "am_refresh", required = false) String cookie,
            HttpServletResponse response) {
        return service.refresh(cookie, response);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(
            @CookieValue(name = "am_refresh", required = false) String cookie,
            HttpServletResponse response) {
        service.logout(cookie, response);
        return Map.of("mensaje", "Sesión cerrada");
    }

    @GetMapping("/me")
    public Dtos.UserView me(Authentication authentication) {
        return service.me(authentication.getName());
    }
}
