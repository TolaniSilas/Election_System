package electionsystem.services;

import electionsystem.data.models.Election;
import electionsystem.data.models.User;
import electionsystem.dtos.requests.ElectionRequest;

import java.util.List;

public interface ElectionService {
    Election createElection(User currentUser, ElectionRequest request);
    Election updateElection(User currentUser, String electionId, ElectionRequest request);
    void deleteElection(User currentUser, String electionId);
    List<Election> getAllElections();
    Election getElectionById(String electionId);
}
