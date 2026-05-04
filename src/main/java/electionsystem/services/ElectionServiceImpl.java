package electionsystem.services;

import electionsystem.data.models.Election;
import electionsystem.data.models.ElectionStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.CandidateRepository;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.VoteRepository;
import electionsystem.dtos.requests.ElectionRequest;
import electionsystem.exceptions.AuthorizationException;
import electionsystem.exceptions.InvalidStateException;
import electionsystem.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ElectionServiceImpl implements ElectionService {

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final VoteRepository voteRepository;

    public ElectionServiceImpl(ElectionRepository electionRepository,
                               CandidateRepository candidateRepository,
                               VoteRepository voteRepository) {
        this.electionRepository = electionRepository;
        this.candidateRepository = candidateRepository;
        this.voteRepository = voteRepository;
    }

    @Override
    public Election createElection(User currentUser, ElectionRequest request) {
        assertAdmin(currentUser);
        validateSchedule(request);

        Election election = new Election();
        election.setTitle(request.getTitle().trim());
        election.setDescription(request.getDescription().trim());
        election.setStartTime(request.getStartTime());
        election.setEndTime(request.getEndTime());
        election.setStatus(resolveStatus(request.getStartTime(), request.getEndTime()));
        election.setCreatedByUserId(currentUser.getId());
        election.setCreatedAt(LocalDateTime.now());
        election.setUpdatedAt(LocalDateTime.now());
        return electionRepository.save(election);
    }

    @Override
    public Election updateElection(User currentUser, String electionId, ElectionRequest request) {
        assertAdmin(currentUser);
        validateSchedule(request);
        Election election = getManagedElection(electionId);
        if (election.getStatus() == ElectionStatus.ENDED) {
            throw new InvalidStateException("Ended elections cannot be modified");
        }
        if (voteRepository.existsByElectionId(electionId)) {
            throw new InvalidStateException("Elections with recorded votes cannot be modified");
        }

        election.setTitle(request.getTitle().trim());
        election.setDescription(request.getDescription().trim());
        election.setStartTime(request.getStartTime());
        election.setEndTime(request.getEndTime());
        election.setStatus(resolveStatus(request.getStartTime(), request.getEndTime()));
        election.setUpdatedAt(LocalDateTime.now());
        return electionRepository.save(election);
    }

    @Override
    public void deleteElection(User currentUser, String electionId) {
        assertAdmin(currentUser);
        Election election = getManagedElection(electionId);
        if (election.getStatus() != ElectionStatus.UPCOMING) {
            throw new InvalidStateException("Only upcoming elections can be deleted");
        }
        if (voteRepository.existsByElectionId(electionId)) {
            throw new InvalidStateException("Elections with votes cannot be deleted");
        }

        candidateRepository.deleteByElectionId(electionId);
        electionRepository.delete(election);
    }

    @Override
    public List<Election> getAllElections() {
        return electionRepository.findAll().stream().map(this::refreshStatus).toList();
    }

    @Override
    public Election getElectionById(String electionId) {
        return refreshStatus(electionRepository.findById(electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Election not found")));
    }

    private void assertAdmin(User currentUser) {
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new AuthorizationException("Only admins can perform this action");
        }
    }

    private void validateSchedule(ElectionRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new InvalidStateException("Start time must be before end time");
        }
    }

    private Election getManagedElection(String electionId) {
        return refreshStatus(electionRepository.findById(electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Election not found")));
    }

    private Election refreshStatus(Election election) {
        ElectionStatus derivedStatus = resolveStatus(election.getStartTime(), election.getEndTime());
        if (election.getStatus() != derivedStatus) {
            election.setStatus(derivedStatus);
            election.setUpdatedAt(LocalDateTime.now());
            return electionRepository.save(election);
        }
        return election;
    }

    private ElectionStatus resolveStatus(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return ElectionStatus.UPCOMING;
        }
        if (now.isAfter(endTime)) {
            return ElectionStatus.ENDED;
        }
        return ElectionStatus.ONGOING;
    }
}
