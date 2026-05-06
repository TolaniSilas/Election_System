package electionsystem.services;

import electionsystem.data.models.User;
import electionsystem.dtos.requests.VoteRequest;
import electionsystem.dtos.responses.ElectionResultResponse;
import electionsystem.dtos.responses.ParticipationSummaryResponse;

public interface VoteService {
    void castVote(User currentUser, VoteRequest request);
    ElectionResultResponse getResults(String electionId);
    ParticipationSummaryResponse getParticipationSummary(User currentUser);
}