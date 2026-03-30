package electionsystem.data.repositories;
import electionsystem.data.models.Election;
import electionsystem.data.models.ElectionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class ElectionRepositoryTest {

    @Autowired
    private ElectionRepository electionRepository;

    @BeforeEach
    public void setUp() {
        electionRepository.deleteAll();
    }

    @Test
    public void newRepositoryCountIsZeroTest() {
        assertEquals(0L, electionRepository.count());
    }

    @Test
    public void saveElectionCountIsOneTest() {
        Election election = new Election();
        election.setTitle("General Election");
        election.setDescription("Test Election");
        election.setStartTime(LocalDateTime.now().plusDays(1));
        election.setEndTime(LocalDateTime.now().plusDays(2));
        election.setStatus(ElectionStatus.UPCOMING);
        electionRepository.save(election);
        assertEquals(1L, electionRepository.count());
    }

    @Test
    public void saveElection_findByIdReturnsSavedElectionTest() {
        Election election = new Election();
        election.setTitle("General Election");
        election.setDescription("Test Election");
        election.setStartTime(LocalDateTime.now().plusDays(1));
        election.setEndTime(LocalDateTime.now().plusDays(2));
        election.setStatus(ElectionStatus.UPCOMING);
        Election saved = electionRepository.save(election);
        Optional<Election> found = electionRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("General Election", found.get().getTitle());
    }

    @Test
    public void deleteElectionCountIsZeroTest() {
        Election election = new Election();
        election.setTitle("General Election");
        election.setDescription("Test Election");
        election.setStartTime(LocalDateTime.now().plusDays(1));
        election.setEndTime(LocalDateTime.now().plusDays(2));
        election.setStatus(ElectionStatus.UPCOMING);
        Election saved = electionRepository.save(election);
        assertEquals(1L, electionRepository.count());
        electionRepository.deleteById(saved.getId());
        assertEquals(0L, electionRepository.count());
    }

    @Test
    public void saveMultipleElectionsFindAllReturnsAllTest() {
        Election election1 = new Election();
        election1.setTitle("Election 1");
        election1.setStartTime(LocalDateTime.now().plusDays(1));
        election1.setEndTime(LocalDateTime.now().plusDays(2));
        election1.setStatus(ElectionStatus.UPCOMING);

        Election election2 = new Election();
        election2.setTitle("Election 2");
        election2.setStartTime(LocalDateTime.now().plusDays(3));
        election2.setEndTime(LocalDateTime.now().plusDays(4));
        election2.setStatus(ElectionStatus.UPCOMING);

        electionRepository.save(election1);
        electionRepository.save(election2);
        
        assertEquals(2L, electionRepository.count());
    }
}