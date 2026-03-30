package electionsystem.controllers;

import electionsystem.data.repositories.UserRepository;
import electionsystem.dtos.requests.LoginRequest;
import electionsystem.dtos.requests.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void register_successTest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@gmail.com");
        request.setPassword("pass123");
        request.setRole("VOTER");

        webTestClient.post()
                .uri("/api/auth/register")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        assertEquals(1L, userRepository.count());
    }

    @Test
    void registerWithDuplicateEmail_returnsConflictTest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@gmail.com");
        request.setPassword("pass123");
        request.setRole("VOTER");

        webTestClient.post()
                .uri("/api/auth/register")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        webTestClient.post()
                .uri("/api/auth/register")
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void login_successTest() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("john");
        registerRequest.setEmail("john@gmail.com");
        registerRequest.setPassword("pass123");
        registerRequest.setRole("VOTER");

        webTestClient.post()
                .uri("/api/auth/register")
                .bodyValue(registerRequest)
                .exchange();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john@gmail.com");
        loginRequest.setPassword("pass123");

        webTestClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void loginWithWrongPassword_returnsUnauthorizedTest() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("john");
        registerRequest.setEmail("john@gmail.com");
        registerRequest.setPassword("pass123");
        registerRequest.setRole("VOTER");

        webTestClient.post()
                .uri("/api/auth/register")
                .bodyValue(registerRequest)
                .exchange();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john@gmail.com");
        loginRequest.setPassword("wrongpass");

        webTestClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}