package electionsystem.controllers;

import electionsystem.data.models.ApprovalStatus;
import electionsystem.data.models.AuthSession;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.AuthSessionRepository;
import electionsystem.data.repositories.UserRepository;
import electionsystem.dtos.requests.LoginRequest;
import electionsystem.dtos.requests.RegisterRequest;
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
public class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthSessionRepository authSessionRepository;

    private WebTestClient webTestClient;
    private User superAdmin;
    private String superAdminToken;

    @BeforeEach
    void setUp() {
        authSessionRepository.deleteAll();
        userRepository.deleteAll();
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        superAdmin = new User();
        superAdmin.setUsername("superadmin");
        superAdmin.setEmail("superadmin@gmail.com");
        superAdmin.setPasswordHash("hashed");
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setApprovalStatus(ApprovalStatus.APPROVED);
        superAdmin.setActive(true);
        superAdmin.setCreatedAt(LocalDateTime.now());
        superAdmin.setApprovedAt(LocalDateTime.now());
        superAdmin = userRepository.save(superAdmin);
        superAdminToken = createSession(superAdmin);
    }

    @Test
    void registerSuccessTest() {
        RegisterRequest request = buildRegisterRequest("john", "john@gmail.com", "pass12345", "VOTER");

        webTestClient.post()
                .uri("/api/auth/register")
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED);

        assertEquals(2L, userRepository.count());
    }

    @Test
    void registerWithDuplicateEmailReturnsConflictTest() {
        RegisterRequest request = buildRegisterRequest("john", "john@gmail.com", "pass12345", "VOTER");

        webTestClient.post().uri("/api/auth/register").bodyValue(request).exchange();

        webTestClient.post()
                .uri("/api/auth/register")
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void loginSuccessTest() {
        RegisterRequest registerRequest = buildRegisterRequest("john", "john@gmail.com", "pass12345", "VOTER");
        webTestClient.post().uri("/api/auth/register").bodyValue(registerRequest).exchange();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john@gmail.com");
        loginRequest.setPassword("pass12345");

        webTestClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void loginPendingAdminReturnsConflictTest() {
        RegisterRequest registerRequest = buildRegisterRequest("john-admin", "john-admin@gmail.com", "pass12345", "ADMIN");
        webTestClient.post().uri("/api/auth/register").bodyValue(registerRequest).exchange();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john-admin@gmail.com");
        loginRequest.setPassword("pass12345");

        webTestClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void superAdminApprovesAdminSuccessTest() {
        RegisterRequest registerRequest = buildRegisterRequest("john-admin", "john-admin@gmail.com", "pass12345", "ADMIN");
        webTestClient.post().uri("/api/auth/register").bodyValue(registerRequest).exchange();
        String adminId = userRepository.findByEmail("john-admin@gmail.com").orElseThrow().getId();

        webTestClient.post()
                .uri("/api/auth/users/" + adminId + "/approve-admin")
                .header(AuthContext.AUTH_TOKEN_HEADER, superAdminToken)
                .exchange()
                .expectStatus().isOk();

        assertEquals(ApprovalStatus.APPROVED, userRepository.findById(adminId).orElseThrow().getApprovalStatus());
    }

    private RegisterRequest buildRegisterRequest(String username, String email, String password, String role) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setRole(role);
        return request;
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
