package cl.automarket.service;

import cl.automarket.api.Dtos;
import cl.automarket.model.RefreshSession;
import cl.automarket.model.UserAccount;
import cl.automarket.repo.RefreshRepository;
import cl.automarket.repo.UserRepository;
import cl.automarket.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class AuthService {
    private static final String COOKIE_NAME = "am_refresh";
    private static final String COOKIE_PATH = "/api";
    private static final String LEGACY_COOKIE_PATH = "/api/auth";

    private final UserRepository users;
    private final RefreshRepository sessions;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final boolean cookieSecure;
    private final int refreshDays;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users, RefreshRepository sessions,
                       PasswordEncoder encoder, JwtService jwt,
                       @Value("${app.cookie.secure}") boolean cookieSecure,
                       @Value("${app.refresh.days}") int refreshDays) {
        this.users = users;
        this.sessions = sessions;
        this.encoder = encoder;
        this.jwt = jwt;
        this.cookieSecure = cookieSecure;
        this.refreshDays = refreshDays;
    }

    public Dtos.AuthResponse register(Dtos.Register input, HttpServletResponse response) {
        String email = input.correo().trim().toLowerCase(Locale.ROOT);
        if (users.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Correo ya registrado");
        }
        UserAccount user = users.save(new UserAccount(input.nombre().trim(), email,
                encoder.encode(input.password()), "CLIENTE"));
        return issue(user, response);
    }

    public Dtos.AuthResponse login(Dtos.Login input, HttpServletResponse response) {
        String email = input.correo().trim().toLowerCase(Locale.ROOT);
        UserAccount user = users.findByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));
        if (!encoder.matches(input.password(), user.passwordHash)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }
        return issue(user, response);
    }

    public Dtos.AuthResponse refresh(String cookie, HttpServletResponse response) {
        if (cookie == null || cookie.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sin sesión de renovación");
        }
        RefreshSession session = sessions.findByTokenHash(hash(cookie)).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
        if (!session.expiresAt.isAfter(Instant.now())) {
            sessions.delete(session);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión vencida");
        }
        UserAccount user = users.findById(session.usuarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        sessions.delete(session); // Rotación de refresh token, invalida el anterior.
        return issue(user, response);
    }

    public void logout(String cookie, HttpServletResponse response) {
        if (cookie != null && !cookie.isBlank()) {
            sessions.findByTokenHash(hash(cookie)).ifPresent(sessions::delete);
        }
        cookie(response, "", 0, LEGACY_COOKIE_PATH);
        cookie(response, "", 0, COOKIE_PATH);
    }

    public Dtos.UserView me(String email) {
        return users.findByEmail(email).map(Dtos.UserView::from).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
    }

    private Dtos.AuthResponse issue(UserAccount user, HttpServletResponse response) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshSession session = new RefreshSession();
        session.usuarioId = user.id;
        session.tokenHash = hash(token);
        session.expiresAt = Instant.now().plusSeconds(refreshDays * 86400L);
        sessions.save(session);

        cookie(response, "", 0, LEGACY_COOKIE_PATH);
        cookie(response, token, refreshDays * 86400L, COOKIE_PATH);
        return new Dtos.AuthResponse(jwt.create(user), Dtos.UserView.from(user));
    }

    private void cookie(HttpServletResponse response, String value, long age, String path) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path(path)
                .maxAge(age)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
