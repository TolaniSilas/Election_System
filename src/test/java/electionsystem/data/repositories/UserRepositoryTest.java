package electionsystem.data.repositories;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void newRepositoryCountIsZeroTest() {
        assertEquals(0L, userRepository.count());
    }

    @Test
    public void saveUserCountIsOneTest() {
        User user = new User();
        user.setUsername("silas");
        user.setEmail("silasosunba@gmail.com");
        user.setPassword("password123");
        user.setRole(Role.VOTER);
        userRepository.save(user);
        assertEquals(1L, userRepository.count());
    }

    @Test
    public void saveUserFindByEmailReturnsSavedUserTest() {
        User user = new User();
        user.setUsername("silas");
        user.setEmail("silasosunba@gmail.com");
        user.setPassword("password123");
        user.setRole(Role.VOTER);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("silasosunba@gmail.com");
        assertTrue(found.isPresent());
        assertEquals("silasosunba@gmail.com", found.get().getEmail());
    }

    @Test
    public void existsByEmailReturnsTrueForExistingEmailTest() {
        User user = new User();
        user.setUsername("silas");
        user.setEmail("silasosunba@gmail.com");
        user.setPassword("password123");
        user.setRole(Role.VOTER);
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("silasosunba@gmail.com"));
    }

    @Test
    public void existsByEmailReturnsFalseForNonExistingEmailTest() {
        assertFalse(userRepository.existsByEmail("ghost@gmail.com"));
    }

    @Test
    public void deleteUserCountIsZeroTest() {
        User user = new User();
        user.setUsername("silas");
        user.setEmail("silasosunba@gmail.com");
        user.setPassword("password123");
        user.setRole(Role.VOTER);
        User saved = userRepository.save(user);

        assertEquals(1L, userRepository.count());
        userRepository.deleteById(saved.getId());

        assertEquals(0L, userRepository.count());
    }
}