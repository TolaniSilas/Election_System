package electionsystem.services;
import electionsystem.data.models.Candidate;
import electionsystem.dtos.requests.CandidateRequest;
import java.util.List;


public interface CandidateService {
    Candidate addCandidate(String userId, CandidateRequest request);
    void removeCandidate(String userId, String candidateId);
    List<Candidate> getCandidatesByElection(String electionId);
}