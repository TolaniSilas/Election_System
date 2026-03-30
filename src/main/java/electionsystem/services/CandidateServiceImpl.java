package electionsystem.services;

import electionsystem.data.models.*;
import electionsystem.data.repositories.*;
import electionsystem.dtos.requests.CandidateRequest;
import electionsystem.exceptions.ElectionSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final ElectionRepository electionRepository;
    private final UserRepository userRepository;

    @Autowired
    public CandidateServiceImpl(CandidateRepository candidateRepository,
                                ElectionRepository electionRepository,
                                UserRepository userRepository) {
        this.candidateRepository = candidateRepository;
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
    public Candidate addCandidate(String userId, CandidateRequest request) {
        assertAdmin(userId);
        electionRepository.findById(request.getElectionId())
                .orElseThrow(() -> new ElectionSystemException("Election not found"));
        if (candidateRepository.existsByNameAndElectionId(request.getName(), request.getElectionId())) {
            throw new ElectionSystemException("Candidate already exists in this election");
        }
        Candidate candidate = new Candidate();
        candidate.setName(request.getName());
        candidate.setElectionId(request.getElectionId());
        return candidateRepository.save(candidate);
    }

    @Override
    public void removeCandidate(String userId, String candidateId) {
        assertAdmin(userId);
        candidateRepository.deleteById(candidateId);
    }

    @Override
    public List<Candidate> getCandidatesByElection(String electionId) {
        return candidateRepository.findByElectionId(electionId);
    }
}