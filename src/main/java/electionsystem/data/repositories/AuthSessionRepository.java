package electionsystem.data.repositories;

import electionsystem.data.models.AuthSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AuthSessionRepository extends MongoRepository<AuthSession, String> {
    Optional<AuthSession> findByToken(String token);
    void deleteByToken(String token);
    void deleteByUserId(String userId);
}
