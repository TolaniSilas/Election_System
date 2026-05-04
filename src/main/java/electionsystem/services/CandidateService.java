package electionsystem.services;

import electionsystem.data.models.Candidate;
import electionsystem.data.models.User;
import electionsystem.dtos.requests.CandidateRequest;

import java.util.List;

public interface CandidateService {
    Candidate addCandidate(User currentUser, CandidateRequest request);
    void removeCandidate(User currentUser, String candidateId);
    List<Candidate> getCandidatesByElection(String electionId);
}
