package electionsystem.services;

import electionsystem.data.models.ApprovalStatus;
import electionsystem.data.models.Candidate;
import electionsystem.data.models.Election;
import electionsystem.data.models.ElectionStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.CandidateRepository;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.UserRepository;
import electionsystem.data.repositories.VoteRepository;
import electionsystem.dtos.requests.VoteRequest;
import electionsystem.exceptions.AuthorizationException;
import electionsystem.exceptions.ResourceNotFoundException;
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
    private VoteRepository voteRepository;
    @Autowired
    private ElectionRepository electionRepository;
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private UserRepository userRepository;

    private User user;
    private User admin;
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

        admin = userRepository.save(buildUser("admin", "admin@gmail.com", Role.ADMIN));
        user = userRepository.save(buildUser("voter", "voter@gmail.com", Role.VOTER));

        ongoingElection = new Election();
        ongoingElection.setTitle("Ongoing Election");
        ongoingElection.setDescription("Test");
        ongoingElection.setStartTime(LocalDateTime.now().minusHours(1));
        ongoingElection.setEndTime(LocalDateTime.now().plusHours(1));
        ongoingElection.setStatus(ElectionStatus.ONGOING);
        ongoingElection.setCreatedByUserId(admin.getId());
        ongoingElection.setCreatedAt(LocalDateTime.now());
        ongoingElection.setUpdatedAt(LocalDateTime.now());
        electionId = electionRepository.save(ongoingElection).getId();

        Candidate candidate = new Candidate();
        candidate.setName("John Doe");
        candidate.setNormalizedName("john doe");
        candidate.setElectionId(electionId);
        candidateId = candidateRepository.save(candidate).getId();

        voteRequest = new VoteRequest();
        voteRequest.setCandidateId(candidateId);
        voteRequest.setElectionId(electionId);
    }

    @Test
    public void castVoteSuccessTest() {
        assertEquals(0L, voteRepository.count());
        voteService.castVote(user, voteRequest);
        assertEquals(1L, voteRepository.count());
    }

    @Test
    public void castVoteTwiceThrowsExceptionTest() {
        voteService.castVote(user, voteRequest);
        assertThrows(VotingException.class, () -> voteService.castVote(user, voteRequest));
        assertEquals(1L, voteRepository.count());
    }

    @Test
    public void adminCannotVoteTest() {
        assertThrows(AuthorizationException.class, () -> voteService.castVote(admin, voteRequest));
    }

    @Test
    public void castVoteBeforeElectionStartsThrowsExceptionTest() {
        ongoingElection.setStartTime(LocalDateTime.now().plusHours(2));
        electionRepository.save(ongoingElection);
        assertThrows(VotingException.class, () -> voteService.castVote(user, voteRequest));
    }

    @Test
    public void castVoteAfterElectionEndsThrowsExceptionTest() {
        ongoingElection.setEndTime(LocalDateTime.now().minusHours(1));
        electionRepository.save(ongoingElection);
        assertThrows(VotingException.class, () -> voteService.castVote(user, voteRequest));
    }

    @Test
    public void castVoteForNonExistingCandidateThrowsExceptionTest() {
        voteRequest.setCandidateId("nonexistent_candidate");
        assertThrows(ResourceNotFoundException.class, () -> voteService.castVote(user, voteRequest));
    }

    @Test
    public void castVoteForNonExistingElectionThrowsExceptionTest() {
        voteRequest.setElectionId("nonexistent_election");
        assertThrows(ResourceNotFoundException.class, () -> voteService.castVote(user, voteRequest));
    }

    private User buildUser(String username, String email, Role role) {
        User builtUser = new User();
        builtUser.setUsername(username);
        builtUser.setEmail(email);
        builtUser.setPasswordHash("hashed");
        builtUser.setRole(role);
        builtUser.setApprovalStatus(ApprovalStatus.APPROVED);
        builtUser.setActive(true);
        builtUser.setCreatedAt(LocalDateTime.now());
        builtUser.setApprovedAt(LocalDateTime.now());
        return builtUser;
    }
}
