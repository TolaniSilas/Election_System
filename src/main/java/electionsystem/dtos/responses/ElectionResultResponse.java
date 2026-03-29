package electionsystem.dtos.responses;
import lombok.Data;
import java.util.Map;


@Data
public class ElectionResultResponse {
    private String electionId;
    private Map<String, Long> votesPerCandidate;
}