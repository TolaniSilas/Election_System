package electionsystem.controllers;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.UserRepository;
import electionsystem.dtos.requests.ElectionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ElectionControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ElectionRepository electionRepository;

    @Autowired
    private UserRepository userRepository;

    private WebTestClient webTestClient;
    private String adminId;
    private String voterId;

    @BeforeEach
    void setUp() {
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
    }

    private ElectionRequest buildElectionRequest() {
        ElectionRequest request = new ElectionRequest();
        request.setTitle("General Election");
        request.setDescription("Test Election");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(2));
        
        return request;
    }

    @Test
    void adminCreatesElectionSuccessTest() {
        webTestClient.post()
                .uri("/api/elections/" + adminId)
                .bodyValue(buildElectionRequest())
                .exchange()
                .expectStatus().isOk();

        assertEquals(1L, electionRepository.count());
    }

    @Test
    void nonAdminCreatesElectionReturnsBadRequestTest() {
        webTestClient.post()
                .uri("/api/elections/" + voterId)
                .bodyValue(buildElectionRequest())
                .exchange()
                .expectStatus().isBadRequest();

        assertEquals(0L, electionRepository.count());
    }

    @Test
    void getAllElectionsReturnsAllTest() {
        webTestClient.post()
                .uri("/api/elections/" + adminId)
                .bodyValue(buildElectionRequest())
                .exchange();

        webTestClient.get()
                .uri("/api/elections")
                .exchange()
                .expectStatus().isOk();

        assertEquals(1L, electionRepository.count());
    }

    @Test
    void deleteElectionSuccessTest() {
        webTestClient.post()
                .uri("/api/elections/" + adminId)
                .bodyValue(buildElectionRequest())
                .exchange();

        assertEquals(1L, electionRepository.count());


        String electionId = electionRepository.findAll().get(0).getId();
        webTestClient.delete()
                .uri("/api/elections/" + adminId + "/" + electionId)
                .exchange()
                .expectStatus().isOk();

        assertEquals(0L, electionRepository.count());
    }
}