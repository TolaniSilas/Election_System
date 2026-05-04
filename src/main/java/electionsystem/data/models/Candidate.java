package electionsystem.data.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@CompoundIndex(name = "unique_candidate_per_election", def = "{'electionId': 1, 'normalizedName': 1}", unique = true)
@Document(collection = "candidates")
public class Candidate {
    @Id
    private String id;
    private String name;
    private String normalizedName;
    private String electionId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }
    public String getElectionId() { return electionId; }
    public void setElectionId(String electionId) { this.electionId = electionId; }
}
