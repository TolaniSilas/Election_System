package electionsystem.dtos.requests;

public class CandidateRequest {
    private String name;
    private String electionId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getElectionId() { return electionId; }
    public void setElectionId(String electionId) { this.electionId = electionId; }
}