package cl.automarket.security;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;

@EnableWebSecurity @Configuration public class SecurityConfig {
    @Bean PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean SecurityFilterChain chain(HttpSecurity http, JwtFilter filter) throws Exception {
        return http.csrf(csrf->csrf.disable()) .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) .authorizeHttpRequests(a->a .requestMatchers("/error", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll() .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh", "/api/auth/logout").permitAll() .requestMatchers(HttpMethod.GET, "/api/vehiculos/**").permitAll() .requestMatchers(HttpMethod.POST, "/api/contacto").permitAll() .requestMatchers("/api/admin/**").hasRole("ADMIN") .requestMatchers(HttpMethod.POST, "/api/vehiculos/**").hasRole("ADMIN") .requestMatchers(HttpMethod.PUT, "/api/vehiculos/**").hasRole("ADMIN") .requestMatchers(HttpMethod.DELETE, "/api/vehiculos/**").hasRole("ADMIN") .anyRequest().authenticated()) .exceptionHandling(e->e.authenticationEntryPoint((req, res, ex)->res.sendError(HttpStatus.UNAUTHORIZED.value(), "No autenticado")) .accessDeniedHandler((req, res, ex)->res.sendError(HttpStatus.FORBIDDEN.value(), "Sin permisos"))) .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
    }
}
