package electionsystem.services;
import electionsystem.dtos.requests.VoteRequest;
import electionsystem.dtos.responses.ElectionResultResponse;


public interface VoteService {
    void castVote(VoteRequest request);
    ElectionResultResponse getResults(String electionId);
}