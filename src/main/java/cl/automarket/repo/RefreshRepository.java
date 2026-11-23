package cl.automarket.repo;

import cl.automarket.model.RefreshSession;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefreshRepository extends MongoRepository<RefreshSession, String> {
    java.util.Optional<RefreshSession> findByTokenHash(String tokenHash);
}
