package electionsystem.services;

import electionsystem.data.models.Candidate;
import electionsystem.data.models.Election;
import electionsystem.data.models.ElectionScope;
import electionsystem.data.models.ElectionStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.models.Vote;
import electionsystem.data.models.VoterApprovalStatus;
import electionsystem.data.repositories.CandidateRepository;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.VoteRepository;
import electionsystem.dtos.requests.VoteRequest;
import electionsystem.dtos.responses.ElectionParticipationResponse;
import electionsystem.dtos.responses.ElectionResultResponse;
import electionsystem.dtos.responses.ParticipationSummaryResponse;
import electionsystem.exceptions.AuthorizationException;
import electionsystem.exceptions.ResourceNotFoundException;
import electionsystem.exceptions.VotingException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;
    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;

    public VoteServiceImpl(VoteRepository voteRepository,
        ElectionRepository electionRepository,
        CandidateRepository candidateRepository
    ) {
        this.voteRepository = voteRepository;
        this.electionRepository = electionRepository;
        this.candidateRepository = candidateRepository;
    }

    @Override
    public void castVote(User currentUser, VoteRequest request) {
        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.SUPER_ADMIN) {
            throw new AuthorizationException("Administrative accounts cannot cast votes");
        }

        if (currentUser.getVoterApprovalStatus() != VoterApprovalStatus.APPROVED) {
            throw new VotingException("Your account is not yet approved to vote");
        }

        Election election = electionRepository.findById(request.getElectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

        if (!candidate.getElectionId().equals(election.getId())) {
            throw new VotingException("Candidate does not belong to the selected election");
        }

        if (!isEligibleForElection(currentUser, election)) {
            throw new AuthorizationException("You are not eligible to vote in this election");
        }

        ElectionStatus status = resolveStatus(election);

        if (status != ElectionStatus.ONGOING) {
            throw new VotingException("Votes can only be cast while the election is ongoing");
        }

        Vote vote = new Vote();
        vote.setUserId(currentUser.getId());
        vote.setCandidateId(candidate.getId());
        vote.setElectionId(election.getId());
        vote.setCreatedAt(LocalDateTime.now());

        try {
            voteRepository.save(vote);
        } 
        
        catch (DuplicateKeyException e) {
            throw new VotingException("You have already voted in this election");
        }
    }

    @Override
    public ElectionResultResponse getResults(String electionId) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));

        Map<String, Long> results = new HashMap<>();
        candidateRepository.findByElectionId(electionId)
                .forEach(c -> results.put(c.getName(), voteRepository.countByCandidateId(c.getId())));

        ElectionResultResponse response = new ElectionResultResponse();
        response.setElectionId(electionId);
        response.setElectionTitle(election.getTitle());
        response.setCategory(election.getCategory() != null ? election.getCategory().name() : null);
        response.setState(election.getState());
        response.setTotalVotes(voteRepository.countByElectionId(electionId));
        response.setVotesPerCandidate(results);
        
        return response;
    }

    @Override
    public ParticipationSummaryResponse getParticipationSummary(User currentUser) {
        List<Election> elections = electionRepository.findAll().stream()
                .filter(election -> isEligibleForElection(currentUser, election) ||
                        currentUser.getRole() == Role.ADMIN ||
                        currentUser.getRole() == Role.SUPER_ADMIN)
                .toList();

        List<ElectionParticipationResponse> electionParticipation = new ArrayList<>();
        Map<String, Long> totalsByCategory = new HashMap<>();

        for (Election election : elections) {
            long participantCount = voteRepository.findByElectionId(election.getId()).stream()
                    .map(Vote::getUserId)
                    .distinct()
                    .count();

            ElectionParticipationResponse response = new ElectionParticipationResponse();
            response.setElectionId(election.getId());
            response.setElectionTitle(election.getTitle());
            response.setCategory(election.getCategory() != null ? election.getCategory().name() : "UNKNOWN");
            response.setState(election.getState());
            response.setParticipantCount(participantCount);
            electionParticipation.add(response);

            totalsByCategory.merge(response.getCategory(), participantCount, Long::sum);
        }

        ParticipationSummaryResponse summary = new ParticipationSummaryResponse();
        summary.setTotalsByCategory(totalsByCategory);
        summary.setElections(electionParticipation.stream()
                .sorted((left, right) -> left.getElectionTitle().compareToIgnoreCase(right.getElectionTitle()))
                .collect(Collectors.toList()));
        return summary;
    }

    private ElectionStatus resolveStatus(Election election) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(election.getStartTime())) {
            return ElectionStatus.UPCOMING;
        }

        if (now.isAfter(election.getEndTime())) {
            return ElectionStatus.ENDED;
        }
        
        return ElectionStatus.ONGOING;
    }

    private boolean isEligibleForElection(User user, Election election) {
        if (election.getScope() == null || election.getScope() == ElectionScope.NATIONAL) {
            return true;
        }

        return election.getState() != null &&
                election.getState().equalsIgnoreCase(user.getStateOfOrigin());
    }
}
