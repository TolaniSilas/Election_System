package electionsystem.dtos.requests;

public class VoteRequest {
    @jakarta.validation.constraints.NotBlank(message = "Candidate ID is required")
    private String candidateId;

    @jakarta.validation.constraints.NotBlank(message = "Election ID is required")
    private String electionId;

    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }
    public String getElectionId() { return electionId; }
    public void setElectionId(String electionId) { this.electionId = electionId; }
}
