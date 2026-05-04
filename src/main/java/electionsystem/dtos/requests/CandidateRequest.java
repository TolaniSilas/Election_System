package electionsystem.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CandidateRequest {
    @NotBlank(message = "Candidate name is required")
    @Size(max = 120, message = "Candidate name must be at most 120 characters")
    private String name;

    @NotBlank(message = "Election ID is required")
    private String electionId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getElectionId() { return electionId; }
    public void setElectionId(String electionId) { this.electionId = electionId; }
}
