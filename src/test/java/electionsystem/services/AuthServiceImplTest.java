package electionsystem.services;
import electionsystem.data.models.Role;
import electionsystem.data.repositories.UserRepository;
import electionsystem.dtos.requests.LoginRequest;
import electionsystem.dtos.requests.RegisterRequest;
import electionsystem.exceptions.DuplicateUserException;
import electionsystem.exceptions.InvalidLoginException;
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
    private RegisterRequest registerRequest;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("john");
        registerRequest.setEmail("silasosunba@gmail.com");
        registerRequest.setPassword("mypassword123");
        registerRequest.setRole("VOTER");
    }

    @Test
    public void registerSuccessTest() {
        assertEquals(0L, userRepository.count());
        authService.register(registerRequest);
        assertEquals(1L, userRepository.count());
    }

    @Test
    public void registerTwiceWithSameEmailThrowsExceptionTest() {
        authService.register(registerRequest);
        assertThrows(DuplicateUserException.class, () -> authService.register(registerRequest));
        assertEquals(1L, userRepository.count());
    }

    @Test
    public void registerUserHasCorrectRoleTest() {
        authService.register(registerRequest);
        assertEquals(Role.VOTER, userRepository.findByEmail("silasosunba@gmail.com").get().getRole());
    }

    @Test
    public void loginSuccessTest() {
        authService.register(registerRequest);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("silasosunba@gmail.com");
        loginRequest.setPassword("mypassword123");
        assertNotNull(authService.login(loginRequest));
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
        authService.register(registerRequest);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("silasosunba@gmail.com");
        loginRequest.setPassword("wrongpassword123");
        assertThrows(InvalidLoginException.class, () -> authService.login(loginRequest));
    }
}