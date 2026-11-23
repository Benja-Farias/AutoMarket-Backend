package cl.automarket.repo;
import cl.automarket.model.ContactMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface ContactRepository extends MongoRepository<ContactMessage,String> {  }
