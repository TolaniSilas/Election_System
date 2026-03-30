package electionsystem.services;

import electionsystem.data.models.ElectionStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.UserRepository;
import electionsystem.dtos.requests.ElectionRequest;
import electionsystem.exceptions.ElectionSystemException;
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
    private UserRepository userRepository;

    private String adminId;
    private String voterId;
    private ElectionRequest electionRequest;

    @BeforeEach
    public void setUp() {
        electionRepository.deleteAll();
        userRepository.deleteAll();

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@gmail.com");
        admin.setPassword("pass");
        admin.setRole(Role.ADMIN);
        adminId = userRepository.save(admin).getId();

        User voter = new User();
        voter.setUsername("voter");
        voter.setEmail("voter@gmail.com");
        voter.setPassword("pass");
        voter.setRole(Role.VOTER);
        voterId = userRepository.save(voter).getId();

        electionRequest = new ElectionRequest();
        electionRequest.setTitle("General Election");
        electionRequest.setDescription("2024 General Election");
        electionRequest.setStartTime(LocalDateTime.now().plusDays(1));
        electionRequest.setEndTime(LocalDateTime.now().plusDays(2));
    }

    @Test
    public void adminCreatesElectionSuccessTest() {
        assertEquals(0L, electionRepository.count());
        electionService.createElection(adminId, electionRequest);
        assertEquals(1L, electionRepository.count());
    }

    @Test
    public void adminCreatesElectionStatusIsUpcomingTest() {
        var election = electionService.createElection(adminId, electionRequest);
        assertEquals(ElectionStatus.UPCOMING, election.getStatus());
    }

    @Test
    public void nonAdminCreatesElectionThrowsExceptionTest() {
        assertThrows(ElectionSystemException.class, () -> electionService.createElection(voterId, electionRequest));
        assertEquals(0L, electionRepository.count());
    }

    @Test
    public void createElectionWithInvalidDatesThrowsExceptionTest() {
        electionRequest.setStartTime(LocalDateTime.now().plusDays(5));
        electionRequest.setEndTime(LocalDateTime.now().plusDays(1));
        assertThrows(ElectionSystemException.class, () -> electionService.createElection(adminId, electionRequest));
    }

    @Test
    public void adminUpdatesElectionSuccessTest() {
        var election = electionService.createElection(adminId, electionRequest);
        electionRequest.setTitle("Updated Election");
        var updated = electionService.updateElection(adminId, election.getId(), electionRequest);
        assertEquals("Updated Election", updated.getTitle());
    }

    @Test
    public void adminDeletesElectionSuccessTest() {
        var election = electionService.createElection(adminId, electionRequest);
        assertEquals(1L, electionRepository.count());
        electionService.deleteElection(adminId, election.getId());
        assertEquals(0L, electionRepository.count());
    }

    @Test
    public void getAllElectionsReturnsAllTest() {
        electionService.createElection(adminId, electionRequest);
        electionRequest.setTitle("Second Election");
        electionService.createElection(adminId, electionRequest);
        assertEquals(2, electionService.getAllElections().size());
    }
}