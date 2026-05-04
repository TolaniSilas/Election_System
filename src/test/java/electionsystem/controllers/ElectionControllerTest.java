package electionsystem.controllers;

import electionsystem.data.models.ApprovalStatus;
import electionsystem.data.models.AuthSession;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.AuthSessionRepository;
import electionsystem.data.repositories.CandidateRepository;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.UserRepository;
import electionsystem.data.repositories.VoteRepository;
import electionsystem.dtos.requests.ElectionRequest;
import electionsystem.security.AuthContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ElectionControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ElectionRepository electionRepository;
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthSessionRepository authSessionRepository;

    private WebTestClient webTestClient;
    private String adminToken;
    private String voterToken;

    @BeforeEach
    void setUp() {
        authSessionRepository.deleteAll();
        voteRepository.deleteAll();
        candidateRepository.deleteAll();
        electionRepository.deleteAll();
        userRepository.deleteAll();

        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        User admin = userRepository.save(buildUser("admin", "admin@gmail.com", Role.ADMIN));
        User voter = userRepository.save(buildUser("voter", "voter@gmail.com", Role.VOTER));
        adminToken = createSession(admin);
        voterToken = createSession(voter);
    }

    @Test
    void adminCreatesElectionSuccessTest() {
        webTestClient.post()
                .uri("/api/elections")
                .header(AuthContext.AUTH_TOKEN_HEADER, adminToken)
                .bodyValue(buildElectionRequest())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED);

        assertEquals(1L, electionRepository.count());
    }

    @Test
    void nonAdminCreatesElectionReturnsForbiddenTest() {
        webTestClient.post()
                .uri("/api/elections")
                .header(AuthContext.AUTH_TOKEN_HEADER, voterToken)
                .bodyValue(buildElectionRequest())
                .exchange()
                .expectStatus().isForbidden();

        assertEquals(0L, electionRepository.count());
    }

    @Test
    void getAllElectionsReturnsAllTest() {
        webTestClient.post()
                .uri("/api/elections")
                .header(AuthContext.AUTH_TOKEN_HEADER, adminToken)
                .bodyValue(buildElectionRequest())
                .exchange();

        webTestClient.get()
                .uri("/api/elections")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void deleteElectionSuccessTest() {
        webTestClient.post()
                .uri("/api/elections")
                .header(AuthContext.AUTH_TOKEN_HEADER, adminToken)
                .bodyValue(buildElectionRequest())
                .exchange();

        String electionId = electionRepository.findAll().get(0).getId();
        webTestClient.delete()
                .uri("/api/elections/" + electionId)
                .header(AuthContext.AUTH_TOKEN_HEADER, adminToken)
                .exchange()
                .expectStatus().isOk();

        assertEquals(0L, electionRepository.count());
    }

    private ElectionRequest buildElectionRequest() {
        ElectionRequest request = new ElectionRequest();
        request.setTitle("General Election");
        request.setDescription("Test Election");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(2));
        return request;
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

    private String createSession(User user) {
        AuthSession session = new AuthSession();
        session.setToken(UUID.randomUUID().toString());
        session.setUserId(user.getId());
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusHours(2));
        return authSessionRepository.save(session).getToken();
    }
}
