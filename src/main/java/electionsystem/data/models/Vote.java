package electionsystem.data.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@CompoundIndex(name = "unique_user_vote_per_election", def = "{'userId': 1, 'electionId': 1}", unique = true)
@Document(collection = "votes")
public class Vote {
    @Id
    private String id;
    private String userId;
    private String candidateId;
    private String electionId;
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }
    public String getElectionId() { return electionId; }
    public void setElectionId(String electionId) { this.electionId = electionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
