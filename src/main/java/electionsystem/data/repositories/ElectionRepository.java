package electionsystem.data.repositories;
import electionsystem.data.models.Election;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface ElectionRepository extends MongoRepository<Election, String> {
    Optional<Election> findByTitle(String title);
}
