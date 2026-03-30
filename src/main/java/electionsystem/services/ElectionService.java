package electionsystem.services;
import electionsystem.data.models.Election;
import electionsystem.dtos.requests.ElectionRequest;
import java.util.List;


public interface ElectionService {
    Election createElection(String userId, ElectionRequest request);
    Election updateElection(String userId, String electionId, ElectionRequest request);

    void deleteElection(String userId, String electionId);
    List<Election> getAllElections();
    Election getElectionById(String electionId);
}