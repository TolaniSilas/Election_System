package electionsystem.services;

import electionsystem.data.models.ApprovalStatus;
import electionsystem.data.models.ElectionStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.CandidateRepository;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.UserRepository;
import electionsystem.data.repositories.VoteRepository;
import electionsystem.dtos.requests.ElectionRequest;
import electionsystem.exceptions.AuthorizationException;
import electionsystem.exceptions.InvalidStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ElectionServiceImplTest {

    @Autowired
    private ElectionService electionService;
    @Autowired
    private ElectionRepository electionRepository;
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private UserRepository userRepository;

    private User admin;
    private User voter;
    private ElectionRequest electionRequest;

    @BeforeEach
    public void setUp() {
        voteRepository.deleteAll();
        candidateRepository.deleteAll();
        electionRepository.deleteAll();
        userRepository.deleteAll();

        admin = userRepository.save(buildUser("admin", "admin@gmail.com", Role.ADMIN));
        voter = userRepository.save(buildUser("voter", "voter@gmail.com", Role.VOTER));

        electionRequest = new ElectionRequest();
        electionRequest.setTitle("General Election");
        electionRequest.setDescription("2024 General Election");
        electionRequest.setStartTime(LocalDateTime.now().plusDays(1));
        electionRequest.setEndTime(LocalDateTime.now().plusDays(2));
    }

    @Test
    public void adminCreatesElectionSuccessTest() {
        assertEquals(0L, electionRepository.count());
        electionService.createElection(admin, electionRequest);
        assertEquals(1L, electionRepository.count());
    }

    @Test
    public void adminCreatesElectionStatusIsUpcomingTest() {
        var election = electionService.createElection(admin, electionRequest);
        assertEquals(ElectionStatus.UPCOMING, election.getStatus());
    }

    @Test
    public void nonAdminCreatesElectionThrowsExceptionTest() {
        assertThrows(AuthorizationException.class, () -> electionService.createElection(voter, electionRequest));
        assertEquals(0L, electionRepository.count());
    }

    @Test
    public void createElectionWithInvalidDatesThrowsExceptionTest() {
        electionRequest.setStartTime(LocalDateTime.now().plusDays(5));
        electionRequest.setEndTime(LocalDateTime.now().plusDays(1));
        assertThrows(InvalidStateException.class, () -> electionService.createElection(admin, electionRequest));
    }

    @Test
    public void adminUpdatesElectionSuccessTest() {
        var election = electionService.createElection(admin, electionRequest);
        electionRequest.setTitle("Updated Election");
        var updated = electionService.updateElection(admin, election.getId(), electionRequest);
        assertEquals("Updated Election", updated.getTitle());
    }

    @Test
    public void adminDeletesUpcomingElectionSuccessTest() {
        var election = electionService.createElection(admin, electionRequest);
        assertEquals(1L, electionRepository.count());
        electionService.deleteElection(admin, election.getId());
        assertEquals(0L, electionRepository.count());
    }

    @Test
    public void getAllElectionsReturnsAllTest() {
        electionService.createElection(admin, electionRequest);
        electionRequest.setTitle("Second Election");
        electionRequest.setStartTime(LocalDateTime.now().plusDays(3));
        electionRequest.setEndTime(LocalDateTime.now().plusDays(4));
        electionService.createElection(admin, electionRequest);
        assertEquals(2, electionService.getAllElections().size());
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
