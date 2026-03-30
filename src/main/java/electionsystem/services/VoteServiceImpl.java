package electionsystem.services;

import electionsystem.data.models.*;
import electionsystem.data.repositories.*;
import electionsystem.dtos.requests.VoteRequest;
import electionsystem.dtos.responses.ElectionResultResponse;
import electionsystem.exceptions.ElectionSystemException;
import electionsystem.exceptions.VotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;
    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;

    @Autowired
    public VoteServiceImpl(VoteRepository voteRepository,
                           ElectionRepository electionRepository,
                           CandidateRepository candidateRepository,
                           UserRepository userRepository) {
        this.voteRepository = voteRepository;
        this.electionRepository = electionRepository;
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void castVote(VoteRequest request) {
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ElectionSystemException("User not found"));

        Election election = electionRepository.findById(request.getElectionId())
                .orElseThrow(() -> new ElectionSystemException("Election not found"));

        candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new VotingException("Candidate not found"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(election.getStartTime())) {
            throw new VotingException("Election has not started yet");
        }
        if (now.isAfter(election.getEndTime())) {
            throw new VotingException("Election has already ended");
        }
        if (voteRepository.existsByUserIdAndElectionId(request.getUserId(), request.getElectionId())) {
            throw new VotingException("You have already voted in this election");
        }

        Vote vote = new Vote();
        vote.setUserId(request.getUserId());
        vote.setCandidateId(request.getCandidateId());
        vote.setElectionId(request.getElectionId());
        voteRepository.save(vote);
    }

    @Override
    public ElectionResultResponse getResults(String electionId) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new ElectionSystemException("Election not found"));

        if (election.getStatus() != ElectionStatus.ENDED) {
            throw new ElectionSystemException("Results are only available after the election ends");
        }

        Map<String, Long> results = new HashMap<>();
        candidateRepository.findByElectionId(electionId)
                .forEach(c -> results.put(c.getName(), voteRepository.countByCandidateId(c.getId())));

        ElectionResultResponse response = new ElectionResultResponse();
        response.setElectionId(electionId);
        response.setVotesPerCandidate(results);
        return response;
    }
}