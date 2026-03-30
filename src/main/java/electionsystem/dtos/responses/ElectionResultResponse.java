package electionsystem.dtos.responses;

import java.util.Map;

public class ElectionResultResponse {
    private String electionId;
    private Map<String, Long> votesPerCandidate;

    public String getElectionId() { return electionId; }
    public void setElectionId(String electionId) { this.electionId = electionId; }
    public Map<String, Long> getVotesPerCandidate() { return votesPerCandidate; }
    public void setVotesPerCandidate(Map<String, Long> votesPerCandidate) { this.votesPerCandidate = votesPerCandidate; }
}