package electionsystem.services;

import electionsystem.data.models.Candidate;
import electionsystem.data.models.Election;
import electionsystem.data.models.ElectionStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.CandidateRepository;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.VoteRepository;
import electionsystem.dtos.requests.CandidateRequest;
import electionsystem.exceptions.AuthorizationException;
import electionsystem.exceptions.ConflictException;
import electionsystem.exceptions.InvalidStateException;
import electionsystem.exceptions.ResourceNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final ElectionRepository electionRepository;
    private final VoteRepository voteRepository;

    public CandidateServiceImpl(CandidateRepository candidateRepository,
                                ElectionRepository electionRepository,
                                VoteRepository voteRepository) {
        this.candidateRepository = candidateRepository;
        this.electionRepository = electionRepository;
        this.voteRepository = voteRepository;
    }

    @Override
    public Candidate addCandidate(User currentUser, CandidateRequest request) {
        assertAdmin(currentUser);
        Election election = electionRepository.findById(request.getElectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));
        if (election.getStatus() != ElectionStatus.UPCOMING) {
            throw new InvalidStateException("Candidates can only be managed before an election starts");
        }

        Candidate candidate = new Candidate();
        candidate.setName(request.getName().trim());
        candidate.setNormalizedName(normalizeName(request.getName()));
        candidate.setElectionId(request.getElectionId());
        try {
            return candidateRepository.save(candidate);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("Candidate already exists in this election");
        }
    }

    @Override
    public void removeCandidate(User currentUser, String candidateId) {
        assertAdmin(currentUser);
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        Election election = electionRepository.findById(candidate.getElectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));
        if (election.getStatus() != ElectionStatus.UPCOMING) {
            throw new InvalidStateException("Candidates cannot be removed after an election starts");
        }
        if (voteRepository.existsByCandidateId(candidateId)) {
            throw new InvalidStateException("Candidates with recorded votes cannot be removed");
        }
        candidateRepository.delete(candidate);
    }

    @Override
    public List<Candidate> getCandidatesByElection(String electionId) {
        electionRepository.findById(electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));
        return candidateRepository.findByElectionId(electionId);
    }

    private void assertAdmin(User currentUser) {
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new AuthorizationException("Only admins can perform this action");
        }
    }

    private String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
