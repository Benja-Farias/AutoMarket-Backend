package cl.automarket.api;
import cl.automarket.model.ContactMessage;
import cl.automarket.repo.ContactRepository;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api")
public class ContactController {
 private final ContactRepository repo;
 public ContactController(ContactRepository repo){this.repo=repo;}
 @PostMapping("/contacto") public ResponseEntity<Map<String,Object>> send(@Valid @RequestBody Dtos.ContactInput d){
  ContactMessage c=new ContactMessage();c.nombre=d.nombre().trim();c.correo=d.correo().trim();c.comentario=d.comentario().trim();
  repo.save(c);return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id",c.id,"mensaje","Consulta recibida"));
 }
 @GetMapping("/admin/contactos") public List<Dtos.ContactView> all(){return repo.findAll().stream().map(Dtos.ContactView::from).toList();}
}
