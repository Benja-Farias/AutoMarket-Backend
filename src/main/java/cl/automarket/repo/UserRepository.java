package cl.automarket.repo;
import cl.automarket.model.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface UserRepository extends MongoRepository<UserAccount,String> { java.util.Optional<UserAccount> findByEmail(String email); }
