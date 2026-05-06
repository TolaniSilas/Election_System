package electionsystem.data.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@CompoundIndex(name = "unique_username", def = "{'username': 1}", unique = true)
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    @Indexed(unique = true)
    private String email;
    @Indexed(unique = true)
    private String nin;
    private String stateOfOrigin;
    private String passwordHash;
    private Role role;
    private ApprovalStatus approvalStatus;
    private VoterApprovalStatus voterApprovalStatus;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNin() { return nin; }
    public void setNin(String nin) { this.nin = nin; }
    public String getStateOfOrigin() { return stateOfOrigin; }
    public void setStateOfOrigin(String stateOfOrigin) { this.stateOfOrigin = stateOfOrigin; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }
    public VoterApprovalStatus getVoterApprovalStatus() { return voterApprovalStatus; }
    public void setVoterApprovalStatus(VoterApprovalStatus voterApprovalStatus) { this.voterApprovalStatus = voterApprovalStatus; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
}
