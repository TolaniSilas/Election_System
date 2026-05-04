package electionsystem.data.repositories;

import electionsystem.data.models.Vote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
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
        voteRepository.save(buildVote("voter1", "candidate1", "election1"));
        assertEquals(1L, voteRepository.count());
    }

    @Test
    public void existsByUserIdAndElectionIdReturnsTrueTest() {
        voteRepository.save(buildVote("voter1", "candidate1", "election1"));
        assertTrue(voteRepository.existsByUserIdAndElectionId("voter1", "election1"));
    }

    @Test
    public void existsByUserIdAndElectionIdReturnsFalseTest() {
        assertFalse(voteRepository.existsByUserIdAndElectionId("voter1", "election1"));
    }

    @Test
    public void findByElectionIdReturnsCorrectVotesTest() {
        voteRepository.save(buildVote("voter1", "candidate1", "election1"));
        voteRepository.save(buildVote("voter2", "candidate1", "election1"));
        voteRepository.save(buildVote("voter3", "candidate2", "election2"));

        List<Vote> election1Votes = voteRepository.findByElectionId("election1");
        assertEquals(2, election1Votes.size());
    }

    @Test
    public void countByCandidateIdReturnsCorrectCountTest() {
        voteRepository.save(buildVote("voter1", "candidate1", "election1"));
        voteRepository.save(buildVote("voter2", "candidate1", "election1"));

        assertEquals(2L, voteRepository.countByCandidateId("candidate1"));
    }

    private Vote buildVote(String userId, String candidateId, String electionId) {
        Vote vote = new Vote();
        vote.setUserId(userId);
        vote.setCandidateId(candidateId);
        vote.setElectionId(electionId);
        vote.setCreatedAt(LocalDateTime.now());
        return vote;
    }
}
