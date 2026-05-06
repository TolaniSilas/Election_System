package electionsystem.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CandidateRequest {
    @NotBlank(message = "Candidate name is required")
    @Size(max = 120, message = "Candidate name must be at most 120 characters")
    private String name;

    @NotBlank(message = "Election ID is required")
    private String electionId;

    @NotBlank(message = "Candidate party is required")
    @Size(max = 20, message = "Candidate party must be at most 20 characters")
    private String party;

    @NotBlank(message = "Candidate biography is required")
    @Size(max = 2000, message = "Candidate biography must be at most 2000 characters")
    private String biography;

    private String imageUrl;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getElectionId() { return electionId; }
    public void setElectionId(String electionId) { this.electionId = electionId; }
    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }
    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
