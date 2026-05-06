package electionsystem.services;

import electionsystem.data.models.Election;
import electionsystem.data.models.ElectionScope;
import electionsystem.data.models.ElectionStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.CandidateRepository;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.VoteRepository;
import electionsystem.dtos.requests.ElectionRequest;
import electionsystem.exceptions.AuthorizationException;
import electionsystem.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

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
        election.setCategory(request.getCategory());
        election.setScope(request.getScope());
        election.setState(normalizeState(request.getScope(), request.getState()));
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

        election.setTitle(request.getTitle().trim());
        election.setDescription(request.getDescription().trim());
        election.setStartTime(request.getStartTime());
        election.setEndTime(request.getEndTime());
        election.setCategory(request.getCategory());
        election.setScope(request.getScope());
        election.setState(normalizeState(request.getScope(), request.getState()));
        election.setStatus(resolveStatus(request.getStartTime(), request.getEndTime()));
        election.setUpdatedAt(LocalDateTime.now());
        return electionRepository.save(election);
    }

    @Override
    public void deleteElection(User currentUser, String electionId) {
        assertAdmin(currentUser);
        Election election = getManagedElection(electionId);

        voteRepository.deleteByElectionId(electionId);
        candidateRepository.deleteByElectionId(electionId);
        electionRepository.delete(election);
    }

    @Override
    public List<Election> getAllElections(User currentUser) {
        return electionRepository.findAll().stream()
                .map(this::refreshStatus)
                .filter(election -> isVisibleToUser(election, currentUser))
                .toList();
    }

    @Override
    public Election getElectionById(String electionId, User currentUser) {
        Election election = refreshStatus(electionRepository.findById(electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Election not found")));
        if (!isVisibleToUser(election, currentUser)) {
            throw new AuthorizationException("You are not eligible to access this election");
        }
        return election;
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
        if (request.getScope() == ElectionScope.STATE &&
                (request.getState() == null || request.getState().trim().isEmpty())) {
            throw new InvalidStateException("State elections require a state");
        }
        if (request.getScope() == ElectionScope.NATIONAL &&
                request.getState() != null && !request.getState().trim().isEmpty()) {
            throw new InvalidStateException("National elections cannot include a state");
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

    private boolean isVisibleToUser(Election election, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.SUPER_ADMIN) {
            return true;
        }
        if (election.getScope() == null || election.getScope() == ElectionScope.NATIONAL) {
            return true;
        }
        return election.getState() != null &&
                election.getState().equalsIgnoreCase(currentUser.getStateOfOrigin());
    }

    private String normalizeState(ElectionScope scope, String state) {
        if (scope == ElectionScope.NATIONAL) {
            return null;
        }
        return state.trim().toUpperCase(Locale.ROOT);
    }
}
