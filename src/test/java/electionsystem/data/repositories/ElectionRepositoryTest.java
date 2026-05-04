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
        electionRepository.save(buildElection("General Election"));
        assertEquals(1L, electionRepository.count());
    }

    @Test
    public void saveElectionFindByIdReturnsSavedElectionTest() {
        Election saved = electionRepository.save(buildElection("General Election"));
        Optional<Election> found = electionRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("General Election", found.get().getTitle());
    }

    @Test
    public void deleteElectionCountIsZeroTest() {
        Election saved = electionRepository.save(buildElection("General Election"));
        assertEquals(1L, electionRepository.count());
        electionRepository.deleteById(saved.getId());
        assertEquals(0L, electionRepository.count());
    }

    @Test
    public void saveMultipleElectionsFindAllReturnsAllTest() {
        electionRepository.save(buildElection("Election 1"));
        electionRepository.save(buildElection("Election 2"));
        assertEquals(2L, electionRepository.count());
    }

    private Election buildElection(String title) {
        Election election = new Election();
        election.setTitle(title);
        election.setDescription("Test Election");
        election.setStartTime(LocalDateTime.now().plusDays(1));
        election.setEndTime(LocalDateTime.now().plusDays(2));
        election.setStatus(ElectionStatus.UPCOMING);
        election.setCreatedByUserId("admin-1");
        election.setCreatedAt(LocalDateTime.now());
        election.setUpdatedAt(LocalDateTime.now());
        return election;
    }
}
