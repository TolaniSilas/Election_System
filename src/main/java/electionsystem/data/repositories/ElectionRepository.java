package electionsystem.data.repositories;
import electionsystem.data.models.Election;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface ElectionRepository extends MongoRepository<Election, String> {
}