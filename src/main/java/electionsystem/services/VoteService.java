package electionsystem.services;

import electionsystem.data.models.User;
import electionsystem.dtos.requests.VoteRequest;
import electionsystem.dtos.responses.ElectionResultResponse;

public interface VoteService {
    void castVote(User currentUser, VoteRequest request);
    ElectionResultResponse getResults(String electionId);
}
