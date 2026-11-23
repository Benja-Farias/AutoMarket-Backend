package cl.automarket.api;

import cl.automarket.model.*;
import cl.automarket.repo.*;
import cl.automarket.security.JwtService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/auth") public class AuthController {
    private final UserRepository users;
    private final RefreshRepository sessions;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final boolean cookieSecure;
    private final int days;
    private final SecureRandom random=new SecureRandom();
    public AuthController(UserRepository users, RefreshRepository sessions, PasswordEncoder encoder, JwtService jwt, @Value("${app.cookie.secure}")boolean cookieSecure, @Value("${app.refresh.days}")int days) {
        this.users=users;
        this.sessions=sessions;
        this.encoder=encoder;
        this.jwt=jwt;
        this.cookieSecure=cookieSecure;
        this.days=days;
    }

    @PostMapping("/register") public ResponseEntity<Dtos.AuthResponse> register(@Valid @RequestBody Dtos.Register dto, HttpServletResponse response) {
        String email=dto.correo().trim().toLowerCase(Locale.ROOT);
        if(users.findByEmail(email).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT, "Correo ya registrado");
        UserAccount u=users.save(new UserAccount(dto.nombre().trim(), email, encoder.encode(dto.password()), "CLIENTE"));
        return ResponseEntity.status(HttpStatus.CREATED).body(issue(u, response));
    }

    @PostMapping("/login") public Dtos.AuthResponse login(@Valid @RequestBody Dtos.Login dto, HttpServletResponse response) {
        var u=users.findByEmail(dto.correo().trim().toLowerCase(Locale.ROOT)) .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));
        if(!encoder.matches(dto.password(), u.passwordHash))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        return issue(u, response);
    }

    @PostMapping("/refresh") public Dtos.AuthResponse refresh(@CookieValue(name="am_refresh", required=false)String cookie, HttpServletResponse response) {
        RefreshSession session=validateCookie(cookie);
        sessions.delete(session);
        UserAccount user=users.findById(session.usuarioId) .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        return issue(user, response);
    }

    @PostMapping("/logout") public Map<String, String> logout(@CookieValue(name="am_refresh", required=false)String cookie, HttpServletResponse response) {
        if(cookie!=null)sessions.findByTokenHash(hash(cookie)).ifPresent(sessions::delete);
        setCookie(response, "", 0);
        return Map.of("mensaje", "Sesión cerrada");
    }

    @GetMapping("/me") public Dtos.UserView me(Authentication authentication) {
        return users.findByEmail(authentication.getName()).map(Dtos.UserView::from) .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
    }

    private RefreshSession validateCookie(String cookie) {
        if(cookie==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sin sesión de renovación");
        var s=sessions.findByTokenHash(hash(cookie)).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
        if(s.expiresAt.isBefore(Instant.now())) {
            sessions.delete(s);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión vencida");
        }
        return s;
    }

    private Dtos.AuthResponse issue(UserAccount u, HttpServletResponse response) {
        byte[] bytes=new byte[32];
        random.nextBytes(bytes);
        String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshSession refresh=new RefreshSession();
        refresh.usuarioId=u.id;
        refresh.tokenHash=hash(token);
        refresh.expiresAt=Instant.now().plusSeconds(days*86400L);
        sessions.save(refresh);
        setCookie(response, token, days*86400L);
        return new Dtos.AuthResponse(jwt.create(u), Dtos.UserView.from(u));
    }

    private void setCookie(HttpServletResponse response, String value, long maxAge) {
        ResponseCookie c=ResponseCookie.from("am_refresh", value).httpOnly(true).secure(cookieSecure).sameSite("Strict") .path("/api/auth").maxAge(maxAge).build();
        response.addHeader(HttpHeaders.SET_COOKIE, c.toString());
    }

    private String hash(String s) {
        try {
            byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(b);
        }
        catch(NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
