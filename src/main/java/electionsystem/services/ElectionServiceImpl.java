package electionsystem.services;

import electionsystem.data.models.*;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.UserRepository;
import electionsystem.dtos.requests.ElectionRequest;
import electionsystem.exceptions.ElectionSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ElectionServiceImpl implements ElectionService {

    private final ElectionRepository electionRepository;
    private final UserRepository userRepository;

    @Autowired
    public ElectionServiceImpl(ElectionRepository electionRepository, UserRepository userRepository) {
        this.electionRepository = electionRepository;
        this.userRepository = userRepository;
    }

    private void assertAdmin(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ElectionSystemException("User not found"));
        if (user.getRole() != Role.ADMIN) {
            throw new ElectionSystemException("Only admins can perform this action");
        }
    }

    @Override
    public Election createElection(String userId, ElectionRequest request) {
        assertAdmin(userId);
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new ElectionSystemException("Start time must be before end time");
        }
        Election election = new Election();
        election.setTitle(request.getTitle());
        election.setDescription(request.getDescription());
        election.setStartTime(request.getStartTime());
        election.setEndTime(request.getEndTime());
        election.setStatus(ElectionStatus.UPCOMING);
        return electionRepository.save(election);
    }

    @Override
    public Election updateElection(String userId, String electionId, ElectionRequest request) {
        assertAdmin(userId);
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new ElectionSystemException("Election not found"));
        election.setTitle(request.getTitle());
        election.setDescription(request.getDescription());
        election.setStartTime(request.getStartTime());
        election.setEndTime(request.getEndTime());
        return electionRepository.save(election);
    }

    @Override
    public void deleteElection(String userId, String electionId) {
        assertAdmin(userId);
        electionRepository.deleteById(electionId);
    }

    @Override
    public List<Election> getAllElections() {
        return electionRepository.findAll();
    }

    @Override
    public Election getElectionById(String electionId) {
        return electionRepository.findById(electionId)
                .orElseThrow(() -> new ElectionSystemException("Election not found"));
    }
}