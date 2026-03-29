package electionsystem.dtos.requests;
import lombok.Data;
import java.time.LocalDateTime;


@Data
public class ElectionRequest {
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}