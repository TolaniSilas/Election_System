package electionsystem.services;

import electionsystem.data.models.Candidate;
import electionsystem.data.models.User;
import electionsystem.dtos.requests.CandidateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CandidateService {
    Candidate addCandidate(User currentUser, CandidateRequest request, MultipartFile image);
    Candidate updateCandidate(User currentUser, String candidateId, CandidateRequest request, MultipartFile image);
    void removeCandidate(User currentUser, String candidateId);
    List<Candidate> getCandidatesByElection(String electionId);
}
