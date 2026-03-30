package electionsystem.services;
import electionsystem.data.models.*;
import electionsystem.data.repositories.*;
import electionsystem.dtos.requests.CandidateRequest;
import electionsystem.dtos.requests.ElectionRequest;
import electionsystem.dtos.requests.VoteRequest;
import electionsystem.exceptions.ElectionSystemException;
import electionsystem.exceptions.VotingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VoteServiceImplTest {

    @Autowired
    private VoteService voteService;

    @Autowired
    private ElectionService electionService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private ElectionRepository electionRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private UserRepository userRepository;

    private String userId;
    private String candidateId;
    private String electionId;
    private Election ongoingElection;
    private VoteRequest voteRequest;

    @BeforeEach
    public void setUp() {
        voteRepository.deleteAll();
        candidateRepository.deleteAll();
        electionRepository.deleteAll();
        userRepository.deleteAll();

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@gmail.com");
        admin.setPassword("pass");
        admin.setRole(Role.ADMIN);
        String adminId = userRepository.save(admin).getId();

        User voter = new User();
        voter.setUsername("voter");
        voter.setEmail("voter@gmail.com");
        voter.setPassword("pass");
        voter.setRole(Role.VOTER);
        userId = userRepository.save(voter).getId();

        ongoingElection = new Election();
        ongoingElection.setTitle("Ongoing Election");
        ongoingElection.setDescription("Test");
        ongoingElection.setStartTime(LocalDateTime.now().minusHours(1));
        ongoingElection.setEndTime(LocalDateTime.now().plusHours(1));
        ongoingElection.setStatus(ElectionStatus.ONGOING);
        electionId = electionRepository.save(ongoingElection).getId();

        CandidateRequest candidateRequest = new CandidateRequest();
        candidateRequest.setName("John Doe");
        candidateRequest.setElectionId(electionId);
        candidateId = candidateService.addCandidate(adminId, candidateRequest).getId();

        voteRequest = new VoteRequest();
        voteRequest.setUserId(userId);
        voteRequest.setCandidateId(candidateId);
        voteRequest.setElectionId(electionId);
    }

    @Test
    public void castVoteSuccessTest() {
        assertEquals(0L, voteRepository.count());
        voteService.castVote(voteRequest);
        assertEquals(1L, voteRepository.count());
    }

    @Test
    public void castVoteTwiceThrowsExceptionTest() {
        voteService.castVote(voteRequest);
        assertThrows(VotingException.class, () -> voteService.castVote(voteRequest));
        assertEquals(1L, voteRepository.count());
    }

    @Test
    public void castVoteBeforeElectionStartsThrowsExceptionTest() {
        ongoingElection.setStartTime(LocalDateTime.now().plusHours(2));
        electionRepository.save(ongoingElection);
        assertThrows(VotingException.class, () -> voteService.castVote(voteRequest));
    }

    @Test
    public void castVoteAfterElectionEndsThrowsExceptionTest() {
        ongoingElection.setEndTime(LocalDateTime.now().minusHours(1));
        electionRepository.save(ongoingElection);
        assertThrows(VotingException.class, () -> voteService.castVote(voteRequest));
    }

    @Test
    public void castVoteForNonExistingCandidateThrowsExceptionTest() {
        voteRequest.setCandidateId("nonexistent_candidate");
        assertThrows(Exception.class, () -> voteService.castVote(voteRequest));
    }

    @Test
    public void castVoteForNonExistingElectionThrowsExceptionTest() {
        voteRequest.setElectionId("nonexistent_election");
        assertThrows(ElectionSystemException.class, () -> voteService.castVote(voteRequest));
    }
}