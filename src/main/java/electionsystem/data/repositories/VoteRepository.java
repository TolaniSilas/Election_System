package electionsystem.data.repositories;
import electionsystem.data.models.Vote;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;


public interface VoteRepository extends MongoRepository<Vote, String> {
    boolean existsByUserIdAndElectionId(String userId, String electionId);
    List<Vote> findByElectionId(String electionId);
    long countByCandidateId(String candidateId);
}