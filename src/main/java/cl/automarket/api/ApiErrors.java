package cl.automarket.api;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.*;
@RestControllerAdvice
public class ApiErrors {
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<Map<String,String>> invalid(MethodArgumentNotValidException ex){
  return ResponseEntity.badRequest().body(Map.of("error",ex.getBindingResult().getFieldErrors().stream().findFirst()
   .map(e->e.getField()+": "+e.getDefaultMessage()).orElse("Datos inválidos")));
 }
 @ExceptionHandler(ResponseStatusException.class)
 public ResponseEntity<Map<String,String>> status(ResponseStatusException ex){return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error",Objects.toString(ex.getReason(),"Error")));}
 @ExceptionHandler(DataIntegrityViolationException.class)
 public ResponseEntity<Map<String,String>> conflict(DataIntegrityViolationException ex){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","Registro duplicado o inválido"));}
}
