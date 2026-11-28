package cl.automarket.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class ApiErrors {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException ex,
                                                       HttpServletRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .orElse("Datos inválidos");
        return respond(HttpStatus.BAD_REQUEST.value(), detail, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST.value(), "El cuerpo JSON no es válido", request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> status(ResponseStatusException ex,
                                                       HttpServletRequest request) {
        return respond(ex.getStatusCode().value(),
                Objects.toString(ex.getReason(), "Solicitud no válida"), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> conflict(DataIntegrityViolationException ex,
                                                         HttpServletRequest request) {
        return respond(HttpStatus.CONFLICT.value(), "Registro duplicado o inválido", request);
    }

    private ResponseEntity<Map<String, Object>> respond(int status, String detail,
                                                         HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", Instant.now().toString());
        result.put("status", status);
        result.put("error", detail); // Conserva contrato anterior para React/CRUD.
        result.put("path", request.getRequestURI());
        return ResponseEntity.status(status).body(result);
    }
}
