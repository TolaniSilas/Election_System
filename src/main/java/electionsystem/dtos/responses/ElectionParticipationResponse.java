package electionsystem.dtos.responses;

public class ElectionParticipationResponse {
    private String electionId;
    private String electionTitle;
    private String category;
    private String state;
    private long participantCount;

    public String getElectionId() { return electionId; }
    public void setElectionId(String electionId) { this.electionId = electionId; }
    public String getElectionTitle() { return electionTitle; }
    public void setElectionTitle(String electionTitle) { this.electionTitle = electionTitle; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public long getParticipantCount() { return participantCount; }
    public void setParticipantCount(long participantCount) { this.participantCount = participantCount; }
}
