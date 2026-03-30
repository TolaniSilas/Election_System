package electionsystem.controllers;
import electionsystem.data.models.*;
import electionsystem.data.repositories.*;
import electionsystem.dtos.requests.CandidateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CandidateControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ElectionRepository electionRepository;

    @Autowired
    private UserRepository userRepository;

    private WebTestClient webTestClient;
    private String adminId;
    private String voterId;
    private String electionId;

    @BeforeEach
    void setUp() {
        candidateRepository.deleteAll();
        electionRepository.deleteAll();
        userRepository.deleteAll();

        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

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

        Election election = new Election();
        election.setTitle("General Election");
        election.setDescription("Test");
        election.setStartTime(LocalDateTime.now().plusDays(1));
        election.setEndTime(LocalDateTime.now().plusDays(2));
        election.setStatus(ElectionStatus.UPCOMING);
        electionId = electionRepository.save(election).getId();
    }

    @Test
    void adminAddsCandidateSuccessTest() {
        CandidateRequest request = new CandidateRequest();
        request.setName("Silas Osunba");
        request.setElectionId(electionId);

        webTestClient.post()
                .uri("/api/candidates/" + adminId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
        assertEquals(1L, candidateRepository.count());
    }

    @Test
    void nonAdminAddsCandidateReturnsBadRequestTest() {
        CandidateRequest request = new CandidateRequest();
        request.setName("Silas Osunba");
        request.setElectionId(electionId);

        webTestClient.post()
                .uri("/api/candidates/" + voterId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
        assertEquals(0L, candidateRepository.count());
    }

    @Test
    void addDuplicateCandidateReturnsBadRequestTest() {
        CandidateRequest request = new CandidateRequest();
        request.setName("Silas Osunba");
        request.setElectionId(electionId);

        webTestClient.post()
                .uri("/api/candidates/" + adminId)
                .bodyValue(request)
                .exchange();

        webTestClient.post()
                .uri("/api/candidates/" + adminId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
        assertEquals(1L, candidateRepository.count());
    }

    @Test
    void getCandidatesByElectionReturnsCorrectCandidatesTest() {
        CandidateRequest request1 = new CandidateRequest();
        request1.setName("Silas Osunba");
        request1.setElectionId(electionId);

        CandidateRequest request2 = new CandidateRequest();
        request2.setName("Jane Wisdom");
        request2.setElectionId(electionId);

        webTestClient.post()
                .uri("/api/candidates/" + adminId)
                .bodyValue(request1)
                .exchange();

        webTestClient.post()
                .uri("/api/candidates/" + adminId)
                .bodyValue(request2)
                .exchange();

        webTestClient.get()
                .uri("/api/candidates/election/" + electionId)
                .exchange()
                .expectStatus().isOk();
        assertEquals(2L, candidateRepository.count());
    }

    @Test
    void removeCandidateSuccessTest() {
        CandidateRequest request = new CandidateRequest();
        request.setName("Silas Osunba");
        request.setElectionId(electionId);

        webTestClient.post()
                .uri("/api/candidates/" + adminId)
                .bodyValue(request)
                .exchange();

        String candidateId = candidateRepository.findAll().get(0).getId();

        webTestClient.delete()
                .uri("/api/candidates/" + adminId + "/" + candidateId)
                .exchange()
                .expectStatus().isOk();
        assertEquals(0L, candidateRepository.count());
    }
}