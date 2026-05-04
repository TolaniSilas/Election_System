package electionsystem.services;

import electionsystem.data.models.Candidate;
import electionsystem.data.models.Election;
import electionsystem.data.models.ElectionStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.models.Vote;
import electionsystem.data.repositories.CandidateRepository;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.VoteRepository;
import electionsystem.dtos.requests.VoteRequest;
import electionsystem.dtos.responses.ElectionResultResponse;
import electionsystem.exceptions.AuthorizationException;
import electionsystem.exceptions.ResourceNotFoundException;
import electionsystem.exceptions.VotingException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;
    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;

    public VoteServiceImpl(VoteRepository voteRepository,
                           ElectionRepository electionRepository,
                           CandidateRepository candidateRepository) {
        this.voteRepository = voteRepository;
        this.electionRepository = electionRepository;
        this.candidateRepository = candidateRepository;
    }

    @Override
    public void castVote(User currentUser, VoteRequest request) {
        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.SUPER_ADMIN) {
            throw new AuthorizationException("Administrative accounts cannot cast votes");
        }

        Election election = electionRepository.findById(request.getElectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

        if (!candidate.getElectionId().equals(election.getId())) {
            throw new VotingException("Candidate does not belong to the selected election");
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
        } catch (DuplicateKeyException e) {
            throw new VotingException("You have already voted in this election");
        }
    }

    @Override
    public ElectionResultResponse getResults(String electionId) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));
        if (resolveStatus(election) != ElectionStatus.ENDED) {
            throw new VotingException("Results are only available after the election ends");
        }

        Map<String, Long> results = new HashMap<>();
        candidateRepository.findByElectionId(electionId)
                .forEach(c -> results.put(c.getName(), voteRepository.countByCandidateId(c.getId())));

        ElectionResultResponse response = new ElectionResultResponse();
        response.setElectionId(electionId);
        response.setVotesPerCandidate(results);
        return response;
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
}
