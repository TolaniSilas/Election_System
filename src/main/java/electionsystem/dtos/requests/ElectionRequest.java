package electionsystem.dtos.requests;

import electionsystem.data.models.ElectionCategory;
import electionsystem.data.models.ElectionScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ElectionRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must be at most 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotNull(message = "Election category is required")
    private ElectionCategory category;

    @NotNull(message = "Election scope is required")
    private ElectionScope scope;

    @Size(max = 50, message = "State must be at most 50 characters")
    private String state;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public ElectionCategory getCategory() { return category; }
    public void setCategory(ElectionCategory category) { this.category = category; }
    public ElectionScope getScope() { return scope; }
    public void setScope(ElectionScope scope) { this.scope = scope; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
