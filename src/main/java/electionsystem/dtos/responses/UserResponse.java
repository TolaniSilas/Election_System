package electionsystem.dtos.responses;

import electionsystem.data.models.ApprovalStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;

import java.time.LocalDateTime;

public class UserResponse {
    private String id;
    private String username;
    private String email;
    private Role role;
    private ApprovalStatus approvalStatus;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setApprovalStatus(user.getApprovalStatus());
        response.setActive(user.isActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setApprovedAt(user.getApprovedAt());
        return response;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
}
