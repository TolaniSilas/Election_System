package electionsystem.data.repositories;
import electionsystem.data.models.Candidate;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;


public interface CandidateRepository extends MongoRepository<Candidate, String> {
    List<Candidate> findByElectionId(String electionId);
    boolean existsByNormalizedNameAndElectionId(String normalizedName, String electionId);
    long countByElectionId(String electionId);
    void deleteByElectionId(String electionId);
}
