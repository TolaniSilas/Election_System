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
        candidateRepository.save(buildCandidate("John Doe", "election1"));
        assertEquals(1L, candidateRepository.count());
    }

    @Test
    public void findByElectionIdReturnsCorrectCandidatesTest() {
        candidateRepository.save(buildCandidate("Silas Osunba", "election1"));
        candidateRepository.save(buildCandidate("Jane Wisdom", "election1"));
        candidateRepository.save(buildCandidate("Bob Smith", "election2"));

        List<Candidate> election1Candidates = candidateRepository.findByElectionId("election1");
        assertEquals(2, election1Candidates.size());
    }

    @Test
    public void existsByNormalizedNameAndElectionIdReturnsTrueTest() {
        candidateRepository.save(buildCandidate("John Doe", "election1"));
        assertTrue(candidateRepository.existsByNormalizedNameAndElectionId("john doe", "election1"));
    }

    @Test
    public void existsByNormalizedNameAndElectionIdReturnsFalseTest() {
        assertFalse(candidateRepository.existsByNormalizedNameAndElectionId("john doe", "election1"));
    }

    @Test
    public void deleteCandidateCountIsZeroTest() {
        Candidate saved = candidateRepository.save(buildCandidate("John Doe", "election1"));
        assertEquals(1L, candidateRepository.count());

        candidateRepository.deleteById(saved.getId());

        assertEquals(0L, candidateRepository.count());
    }

    private Candidate buildCandidate(String name, String electionId) {
        Candidate candidate = new Candidate();
        candidate.setName(name);
        candidate.setNormalizedName(name.toLowerCase());
        candidate.setElectionId(electionId);
        return candidate;
    }
}
