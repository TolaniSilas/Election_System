package electionsystem.controllers;

import electionsystem.data.models.ApprovalStatus;
import electionsystem.data.models.AuthSession;
import electionsystem.data.models.Election;
import electionsystem.data.models.ElectionStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.AuthSessionRepository;
import electionsystem.data.repositories.CandidateRepository;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.UserRepository;
import electionsystem.data.repositories.VoteRepository;
import electionsystem.dtos.requests.CandidateRequest;
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
public class CandidateControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private ElectionRepository electionRepository;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthSessionRepository authSessionRepository;

    private WebTestClient webTestClient;
    private String adminToken;
    private String voterToken;
    private String electionId;

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
    }

    @Test
    void adminAddsCandidateSuccessTest() {
        webTestClient.post()
                .uri("/api/candidates")
                .header(AuthContext.AUTH_TOKEN_HEADER, adminToken)
                .bodyValue(buildRequest("Silas Osunba"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED);
        assertEquals(1L, candidateRepository.count());
    }

    @Test
    void nonAdminAddsCandidateReturnsForbiddenTest() {
        webTestClient.post()
                .uri("/api/candidates")
                .header(AuthContext.AUTH_TOKEN_HEADER, voterToken)
                .bodyValue(buildRequest("Silas Osunba"))
                .exchange()
                .expectStatus().isForbidden();
        assertEquals(0L, candidateRepository.count());
    }

    @Test
    void addDuplicateCandidateReturnsConflictTest() {
        webTestClient.post()
                .uri("/api/candidates")
                .header(AuthContext.AUTH_TOKEN_HEADER, adminToken)
                .bodyValue(buildRequest("Silas Osunba"))
                .exchange();

        webTestClient.post()
                .uri("/api/candidates")
                .header(AuthContext.AUTH_TOKEN_HEADER, adminToken)
                .bodyValue(buildRequest("Silas Osunba"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
        assertEquals(1L, candidateRepository.count());
    }

    @Test
    void removeCandidateSuccessTest() {
        webTestClient.post()
                .uri("/api/candidates")
                .header(AuthContext.AUTH_TOKEN_HEADER, adminToken)
                .bodyValue(buildRequest("Silas Osunba"))
                .exchange();

        String candidateId = candidateRepository.findAll().get(0).getId();
        webTestClient.delete()
                .uri("/api/candidates/" + candidateId)
                .header(AuthContext.AUTH_TOKEN_HEADER, adminToken)
                .exchange()
                .expectStatus().isOk();
        assertEquals(0L, candidateRepository.count());
    }

    private CandidateRequest buildRequest(String name) {
        CandidateRequest request = new CandidateRequest();
        request.setName(name);
        request.setElectionId(electionId);
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
