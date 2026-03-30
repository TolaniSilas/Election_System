package electionsystem.data.repositories;
import electionsystem.data.models.Candidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class CandidateRepositoryTest {

    @Autowired
    private CandidateRepository candidateRepository;

    @BeforeEach
    public void setUp() {
        candidateRepository.deleteAll();
    }

    @Test
    public void newRepositoryCountIsZeroTest() {
        assertEquals(0L, candidateRepository.count());
    }

    @Test
    public void saveCandidateCountIsOneTest() {
        Candidate candidate = new Candidate();
        candidate.setName("John Doe");
        candidate.setElectionId("election1");
        candidateRepository.save(candidate);
        
        assertEquals(1L, candidateRepository.count());
    }

    @Test
    public void findByElectionIdReturnsCorrectCandidatesTest() {
        Candidate candidate1 = new Candidate();
        candidate1.setName("Silas Osunba");
        candidate1.setElectionId("election1");

        Candidate candidate2 = new Candidate();
        candidate2.setName("Jane Wisdom");
        candidate2.setElectionId("election1");

        Candidate candidate3 = new Candidate();
        candidate3.setName("Bob Smith");
        candidate3.setElectionId("election2");

        candidateRepository.save(candidate1);
        candidateRepository.save(candidate2);
        candidateRepository.save(candidate3);

        List<Candidate> election1Candidates = candidateRepository.findByElectionId("election1");
        assertEquals(2, election1Candidates.size());
    }

    @Test
    public void existsByNameAndElectionIdReturnsTrueTest() {
        Candidate candidate = new Candidate();
        candidate.setName("John Doe");
        candidate.setElectionId("election1");
        candidateRepository.save(candidate);
        assertTrue(candidateRepository.existsByNameAndElectionId("John Doe", "election1"));
    }

    @Test
    public void existsByNameAndElectionIdReturnsFalseTest() {
        assertFalse(candidateRepository.existsByNameAndElectionId("John Doe", "election1"));
    }

    @Test
    public void deleteCandidateCountIsZeroTest() {
        Candidate candidate = new Candidate();
        candidate.setName("John Doe");
        candidate.setElectionId("election1");
        Candidate saved = candidateRepository.save(candidate);

        assertEquals(1L, candidateRepository.count());
        candidateRepository.deleteById(saved.getId());

        assertEquals(0L, candidateRepository.count());
    }
}