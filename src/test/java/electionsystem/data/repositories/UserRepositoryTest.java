package electionsystem.data.repositories;

import electionsystem.data.models.ApprovalStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
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
        userRepository.save(buildUser("silas", "silasosunba@gmail.com"));
        assertEquals(1L, userRepository.count());
    }

    @Test
    public void saveUserFindByEmailReturnsSavedUserTest() {
        userRepository.save(buildUser("silas", "silasosunba@gmail.com"));

        Optional<User> found = userRepository.findByEmail("silasosunba@gmail.com");
        assertTrue(found.isPresent());
        assertEquals("silasosunba@gmail.com", found.get().getEmail());
    }

    @Test
    public void existsByEmailReturnsTrueForExistingEmailTest() {
        userRepository.save(buildUser("silas", "silasosunba@gmail.com"));
        assertTrue(userRepository.existsByEmail("silasosunba@gmail.com"));
    }

    @Test
    public void existsByEmailReturnsFalseForNonExistingEmailTest() {
        assertFalse(userRepository.existsByEmail("ghost@gmail.com"));
    }

    @Test
    public void deleteUserCountIsZeroTest() {
        User saved = userRepository.save(buildUser("silas", "silasosunba@gmail.com"));
        assertEquals(1L, userRepository.count());

        userRepository.deleteById(saved.getId());

        assertEquals(0L, userRepository.count());
    }

    private User buildUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("hashed-password");
        user.setRole(Role.VOTER);
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setApprovedAt(LocalDateTime.now());
        return user;
    }
}
