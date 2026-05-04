package electionsystem.controllers;

import electionsystem.data.models.ApprovalStatus;
import electionsystem.data.models.AuthSession;
import electionsystem.data.models.Candidate;
import electionsystem.data.models.Election;
import electionsystem.data.models.ElectionStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.AuthSessionRepository;
import electionsystem.data.repositories.CandidateRepository;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.UserRepository;
import electionsystem.data.repositories.VoteRepository;
import electionsystem.dtos.requests.VoteRequest;
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
public class VoteControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private ElectionRepository electionRepository;
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthSessionRepository authSessionRepository;

    private WebTestClient webTestClient;
    private String voterToken;
    private String candidateId;
    private String electionId;

    @BeforeEach
    void setUp() {
        authSessionRepository.deleteAll();
        voteRepository.deleteAll();
        candidateRepository.deleteAll();
        electionRepository.deleteAll();
        userRepository.deleteAll();

        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        User voter = userRepository.save(buildUser("voter", "voter@gmail.com", Role.VOTER));
        voterToken = createSession(voter);

        Election election = new Election();
        election.setTitle("Ongoing Election");
        election.setDescription("Test");
        election.setStartTime(LocalDateTime.now().minusHours(1));
        election.setEndTime(LocalDateTime.now().plusHours(1));
        election.setStatus(ElectionStatus.ONGOING);
        election.setCreatedByUserId("admin-id");
        election.setCreatedAt(LocalDateTime.now());
        election.setUpdatedAt(LocalDateTime.now());
        electionId = electionRepository.save(election).getId();

        Candidate candidate = new Candidate();
        candidate.setName("John Doe");
        candidate.setNormalizedName("john doe");
        candidate.setElectionId(electionId);
        candidateId = candidateRepository.save(candidate).getId();
    }

    @Test
    void castVoteSuccessTest() {
        webTestClient.post()
                .uri("/api/votes")
                .header(AuthContext.AUTH_TOKEN_HEADER, voterToken)
                .bodyValue(buildVoteRequest())
                .exchange()
                .expectStatus().isOk();

        assertEquals(1L, voteRepository.count());
    }

    @Test
    void castVoteTwiceReturnsConflictTest() {
        webTestClient.post()
                .uri("/api/votes")
                .header(AuthContext.AUTH_TOKEN_HEADER, voterToken)
                .bodyValue(buildVoteRequest())
                .exchange();

        webTestClient.post()
                .uri("/api/votes")
                .header(AuthContext.AUTH_TOKEN_HEADER, voterToken)
                .bodyValue(buildVoteRequest())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        assertEquals(1L, voteRepository.count());
    }

    @Test
    void castVoteWithoutTokenReturnsUnauthorizedTest() {
        webTestClient.post()
                .uri("/api/votes")
                .bodyValue(buildVoteRequest())
                .exchange()
                .expectStatus().isUnauthorized();

        assertEquals(0L, voteRepository.count());
    }

    private VoteRequest buildVoteRequest() {
        VoteRequest request = new VoteRequest();
        request.setCandidateId(candidateId);
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
