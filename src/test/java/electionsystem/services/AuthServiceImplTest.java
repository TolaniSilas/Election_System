package electionsystem.services;

import electionsystem.data.models.ApprovalStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.AuthSessionRepository;
import electionsystem.data.repositories.UserRepository;
import electionsystem.dtos.requests.LoginRequest;
import electionsystem.dtos.requests.RegisterRequest;
import electionsystem.exceptions.DuplicateUserException;
import electionsystem.exceptions.InvalidLoginException;
import electionsystem.exceptions.InvalidStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthServiceImplTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthSessionRepository authSessionRepository;

    private RegisterRequest voterRequest;

    @BeforeEach
    public void setUp() {
        authSessionRepository.deleteAll();
        userRepository.deleteAll();
        voterRequest = new RegisterRequest();
        voterRequest.setUsername("john");
        voterRequest.setEmail("silasosunba@gmail.com");
        voterRequest.setPassword("mypassword123");
        voterRequest.setRole("VOTER");
    }

    @Test
    public void registerSuccessTest() {
        assertEquals(0L, userRepository.count());
        authService.register(voterRequest);
        assertEquals(1L, userRepository.count());
    }

    @Test
    public void registerTwiceWithSameEmailThrowsExceptionTest() {
        authService.register(voterRequest);
        assertThrows(DuplicateUserException.class, () -> authService.register(voterRequest));
        assertEquals(1L, userRepository.count());
    }

    @Test
    public void registerUserHasCorrectRoleTest() {
        authService.register(voterRequest);
        assertEquals(Role.VOTER, userRepository.findByEmail("silasosunba@gmail.com").orElseThrow().getRole());
    }

    @Test
    public void adminRegistrationStartsPendingTest() {
        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("admin-user");
        adminRequest.setEmail("admin@gmail.com");
        adminRequest.setPassword("mypassword123");
        adminRequest.setRole("ADMIN");

        authService.register(adminRequest);

        User saved = userRepository.findByEmail("admin@gmail.com").orElseThrow();
        assertEquals(ApprovalStatus.PENDING, saved.getApprovalStatus());
    }

    @Test
    public void loginSuccessTest() {
        authService.register(voterRequest);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("silasosunba@gmail.com");
        loginRequest.setPassword("mypassword123");
        assertNotNull(authService.login(loginRequest).getToken());
    }

    @Test
    public void loginPendingAdminThrowsExceptionTest() {
        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("pending-admin");
        adminRequest.setEmail("pending@gmail.com");
        adminRequest.setPassword("mypassword123");
        adminRequest.setRole("ADMIN");
        authService.register(adminRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("pending@gmail.com");
        loginRequest.setPassword("mypassword123");

        assertThrows(InvalidStateException.class, () -> authService.login(loginRequest));
    }

    @Test
    public void loginUnregisteredUserThrowsExceptionTest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("ghost@gmail.com");
        loginRequest.setPassword("password123");
        assertThrows(InvalidLoginException.class, () -> authService.login(loginRequest));
    }

    @Test
    public void loginWithWrongPasswordThrowsExceptionTest() {
        authService.register(voterRequest);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("silasosunba@gmail.com");
        loginRequest.setPassword("wrongpassword123");
        assertThrows(InvalidLoginException.class, () -> authService.login(loginRequest));
    }
}
