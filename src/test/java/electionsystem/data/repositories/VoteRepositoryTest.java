package electionsystem.data.repositories;
import electionsystem.data.models.Vote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class VoteRepositoryTest {

    @Autowired
    private VoteRepository voteRepository;

    @BeforeEach
    public void setUp() {
        voteRepository.deleteAll();
    }

    @Test
    public void newRepositoryCountIsZeroTest() {
        assertEquals(0L, voteRepository.count());
    }

    @Test
    public void saveVoteCountIsOneTest() {
        Vote vote = new Vote();
        vote.setUserId("voter1");
        vote.setCandidateId("candidate1");
        vote.setElectionId("election1");
        voteRepository.save(vote);
        assertEquals(1L, voteRepository.count());
    }

    @Test
    public void existsByUserIdAndElectionIdReturnsTrueTest() {
        Vote vote = new Vote();
        vote.setUserId("voter1");
        vote.setCandidateId("candidate1");
        vote.setElectionId("election1");
        voteRepository.save(vote);
        assertTrue(voteRepository.existsByUserIdAndElectionId("voter1", "election1"));
    }

    @Test
    public void existsByUserIdAndElectionIdReturnsFalseTest() {
        assertFalse(voteRepository.existsByUserIdAndElectionId("voter1", "election1"));
    }

    @Test
    public void findByElectionIdReturnsCorrectVotesTest() {
        Vote vote1 = new Vote();
        vote1.setUserId("voter1");
        vote1.setCandidateId("candidate1");
        vote1.setElectionId("election1");

        Vote vote2 = new Vote();
        vote2.setUserId("voter2");
        vote2.setCandidateId("candidate1");
        vote2.setElectionId("election1");

        Vote vote3 = new Vote();
        vote3.setUserId("voter3");
        vote3.setCandidateId("candidate2");
        vote3.setElectionId("election2");

        voteRepository.save(vote1);
        voteRepository.save(vote2);
        voteRepository.save(vote3);

        List<Vote> election1Votes = voteRepository.findByElectionId("election1");
        assertEquals(2, election1Votes.size());
    }

    @Test
    public void countByCandidateIdReturnsCorrectCountTest() {
        Vote vote1 = new Vote();
        vote1.setUserId("voter1");
        vote1.setCandidateId("candidate1");
        vote1.setElectionId("election1");

        Vote vote2 = new Vote();
        vote2.setUserId("voter2");
        vote2.setCandidateId("candidate1");
        vote2.setElectionId("election1");

        voteRepository.save(vote1);
        voteRepository.save(vote2);

        assertEquals(2L, voteRepository.countByCandidateId("candidate1"));
    }
}