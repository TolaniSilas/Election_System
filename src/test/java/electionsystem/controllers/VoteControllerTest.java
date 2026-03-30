package electionsystem.controllers;
import electionsystem.data.models.*;
import electionsystem.data.repositories.*;
import electionsystem.dtos.requests.VoteRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;


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

    private WebTestClient webTestClient;
    private String userId;
    private String candidateId;
    private String electionId;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        candidateRepository.deleteAll();
        electionRepository.deleteAll();
        userRepository.deleteAll();

        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        User voter = new User();
        voter.setUsername("voter");
        voter.setEmail("voter@gmail.com");
        voter.setPassword("pass");
        voter.setRole(Role.VOTER);
        userId = userRepository.save(voter).getId();

        Election election = new Election();
        election.setTitle("Ongoing Election");
        election.setDescription("Test");
        election.setStartTime(LocalDateTime.now().minusHours(1));
        election.setEndTime(LocalDateTime.now().plusHours(1));
        election.setStatus(ElectionStatus.ONGOING);
        electionId = electionRepository.save(election).getId();

        Candidate candidate = new Candidate();
        candidate.setName("John Doe");
        candidate.setElectionId(electionId);
        candidateId = candidateRepository.save(candidate).getId();
    }

    private VoteRequest buildVoteRequest() {
        VoteRequest request = new VoteRequest();
        request.setUserId(userId);
        request.setCandidateId(candidateId);
        request.setElectionId(electionId);

        return request;
    }

    @Test
    void castVoteSuccessTest() {
        webTestClient.post()
                .uri("/api/votes")
                .bodyValue(buildVoteRequest())
                .exchange()
                .expectStatus().isOk();

        assertEquals(1L, voteRepository.count());
    }

    @Test
    void castVoteTwiceReturnsBadRequestTest() {
        webTestClient.post()
                .uri("/api/votes")
                .bodyValue(buildVoteRequest())
                .exchange();

        webTestClient.post()
                .uri("/api/votes")
                .bodyValue(buildVoteRequest())
                .exchange()
                .expectStatus().isBadRequest();

        assertEquals(1L, voteRepository.count());
    }

    @Test
    void castVoteForNonExistingCandidateReturnsBadRequestTest() {
        VoteRequest request = buildVoteRequest();
        request.setCandidateId("nonexistent_candidate");

        webTestClient.post().uri("/api/votes").bodyValue(request).exchange().expectStatus().isBadRequest();

        assertEquals(0L, voteRepository.count());
    }

    @Test
    void castVoteForNonExistingElectionReturnsBadRequestTest() {
        VoteRequest request = buildVoteRequest();
        request.setElectionId("nonexistent_election");

        webTestClient.post().uri("/api/votes").bodyValue(request).exchange().expectStatus().isBadRequest();

        assertEquals(0L, voteRepository.count());
    }
}