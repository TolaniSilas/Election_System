package electionsystem.services;

import electionsystem.data.models.*;
import electionsystem.data.repositories.*;
import electionsystem.dtos.requests.CandidateRequest;
import electionsystem.dtos.requests.ElectionRequest;
import electionsystem.exceptions.ElectionSystemException;
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
    private ElectionService electionService;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ElectionRepository electionRepository;

    @Autowired
    private UserRepository userRepository;

    private String adminId;
    private String voterId;
    private String electionId;
    private CandidateRequest candidateRequest;

    @BeforeEach
    public void setUp() {
        candidateRepository.deleteAll();
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

        ElectionRequest electionRequest = new ElectionRequest();
        electionRequest.setTitle("General Election");
        electionRequest.setDescription("Test");
        electionRequest.setStartTime(LocalDateTime.now().plusDays(1));
        electionRequest.setEndTime(LocalDateTime.now().plusDays(2));
        electionId = electionService.createElection(adminId, electionRequest).getId();

        candidateRequest = new CandidateRequest();
        candidateRequest.setName("John Doe");
        candidateRequest.setElectionId(electionId);
    }

    @Test
    public void addCandidateSuccessTest() {
        assertEquals(0L, candidateRepository.count());
        candidateService.addCandidate(adminId, candidateRequest);
        assertEquals(1L, candidateRepository.count());
    }

    @Test
    public void addDuplicateCandidateThrowsExceptionTest() {
        candidateService.addCandidate(adminId, candidateRequest);
        assertThrows(ElectionSystemException.class, () -> candidateService.addCandidate(adminId, candidateRequest));
        assertEquals(1L, candidateRepository.count());
    }

    @Test
    public void nonAdminAddsCandidateThrowsExceptionTest() {
        assertThrows(ElectionSystemException.class, () -> candidateService.addCandidate(voterId, candidateRequest));
        assertEquals(0L, candidateRepository.count());
    }

    @Test
    public void addCandidateToNonExistingElectionThrowsExceptionTest() {
        candidateRequest.setElectionId("nonexistent_id");
        assertThrows(ElectionSystemException.class, () -> candidateService.addCandidate(adminId, candidateRequest));
    }

    @Test
    public void removeCandidateSuccessTest() {
        var candidate = candidateService.addCandidate(adminId, candidateRequest);
        assertEquals(1L, candidateRepository.count());
        candidateService.removeCandidate(adminId, candidate.getId());
        assertEquals(0L, candidateRepository.count());
    }

    @Test
    public void getCandidatesByElectionReturnsCorrectCandidatesTest() {
        candidateService.addCandidate(adminId, candidateRequest);
        candidateRequest.setName("Jane Doe");
        candidateService.addCandidate(adminId, candidateRequest);
        assertEquals(2, candidateService.getCandidatesByElection(electionId).size());
    }
}