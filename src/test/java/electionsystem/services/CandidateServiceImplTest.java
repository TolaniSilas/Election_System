package electionsystem.services;

import electionsystem.data.models.ApprovalStatus;
import electionsystem.data.models.Election;
import electionsystem.data.models.ElectionStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.CandidateRepository;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.UserRepository;
import electionsystem.data.repositories.VoteRepository;
import electionsystem.dtos.requests.CandidateRequest;
import electionsystem.exceptions.AuthorizationException;
import electionsystem.exceptions.ConflictException;
import electionsystem.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CandidateServiceImplTest {

    @Autowired
    private CandidateService candidateService;
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private ElectionRepository electionRepository;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private UserRepository userRepository;

    private User admin;
    private User voter;
    private String electionId;
    private CandidateRequest candidateRequest;

    @BeforeEach
    public void setUp() {
        voteRepository.deleteAll();
        candidateRepository.deleteAll();
        electionRepository.deleteAll();
        userRepository.deleteAll();

        admin = userRepository.save(buildUser("admin", "admin@gmail.com", Role.ADMIN));
        voter = userRepository.save(buildUser("voter", "voter@gmail.com", Role.VOTER));

        Election election = new Election();
        election.setTitle("General Election");
        election.setDescription("Test");
        election.setStartTime(LocalDateTime.now().plusDays(1));
        election.setEndTime(LocalDateTime.now().plusDays(2));
        election.setStatus(ElectionStatus.UPCOMING);
        election.setCreatedByUserId(admin.getId());
        election.setCreatedAt(LocalDateTime.now());
        election.setUpdatedAt(LocalDateTime.now());
        electionId = electionRepository.save(election).getId();

        candidateRequest = new CandidateRequest();
        candidateRequest.setName("John Doe");
        candidateRequest.setElectionId(electionId);
    }

    @Test
    public void addCandidateSuccessTest() {
        assertEquals(0L, candidateRepository.count());
        candidateService.addCandidate(admin, candidateRequest);
        assertEquals(1L, candidateRepository.count());
    }

    @Test
    public void addDuplicateCandidateThrowsExceptionTest() {
        candidateService.addCandidate(admin, candidateRequest);
        assertThrows(ConflictException.class, () -> candidateService.addCandidate(admin, candidateRequest));
        assertEquals(1L, candidateRepository.count());
    }

    @Test
    public void nonAdminAddsCandidateThrowsExceptionTest() {
        assertThrows(AuthorizationException.class, () -> candidateService.addCandidate(voter, candidateRequest));
        assertEquals(0L, candidateRepository.count());
    }

    @Test
    public void addCandidateToNonExistingElectionThrowsExceptionTest() {
        candidateRequest.setElectionId("nonexistent_id");
        assertThrows(ResourceNotFoundException.class, () -> candidateService.addCandidate(admin, candidateRequest));
    }

    @Test
    public void removeCandidateSuccessTest() {
        var candidate = candidateService.addCandidate(admin, candidateRequest);
        assertEquals(1L, candidateRepository.count());
        candidateService.removeCandidate(admin, candidate.getId());
        assertEquals(0L, candidateRepository.count());
    }

    @Test
    public void getCandidatesByElectionReturnsCorrectCandidatesTest() {
        candidateService.addCandidate(admin, candidateRequest);
        candidateRequest.setName("Jane Doe");
        candidateService.addCandidate(admin, candidateRequest);
        assertEquals(2, candidateService.getCandidatesByElection(electionId).size());
    }

    private User buildUser(String username, String email, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setRole(role);
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setApprovedAt(LocalDateTime.now());
        return user;
    }
}
